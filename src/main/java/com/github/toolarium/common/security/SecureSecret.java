/*
 * SecureSecret.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import javax.security.auth.Destroyable;

/**
 * Immutable container for sensitive data (passwords, tokens, cryptographic keys) that stores
 * its content in off-heap native memory via {@link ByteBuffer#allocateDirect(int)}.
 *
 * <h2>Why off-heap?</h2>
 *
 * <p>On-heap objects ({@code byte[]}, {@code char[]}, {@code String}) are subject to GC
 * copying: during a collection the JVM may move the object to a new memory region, leaving
 * the original bytes behind unzeroed. A direct {@code ByteBuffer} is allocated outside the
 * Java heap in native memory, so the JVM never copies it, and {@link #destroy()} can
 * reliably overwrite the exact memory region that holds the secret.
 *
 * <h2>Ownership and zeroing of input arrays</h2>
 *
 * <p>All constructors <strong>take ownership</strong> of their input array: the array is
 * zeroed by the constructor before it returns, so callers must not read from it afterwards.
 * This eliminates the risk of the caller forgetting to zero their copy.
 *
 * <h2>Thread safety</h2>
 *
 * <p>This class is <em>thread-safe</em>. Concurrent reads ({@link #use}, {@link #toByteArray},
 * {@link #toCharArray}) are permitted simultaneously via a shared read lock.
 * {@link #destroy()} acquires an <em>exclusive</em> write lock in <em>fair</em> mode, so it
 * is never starved by a continuous stream of readers.
 *
 * <p>Note: the {@code fn} passed to {@link #use} or {@link #useChars} is invoked
 * <em>outside</em> the read lock. The temporary copy passed to {@code fn} is already on the
 * heap at that point, so prolonged function execution does not block {@link #destroy()}.
 *
 * <h2>GC safety net</h2>
 *
 * <p>This class registers a {@link Cleaner} action on construction. If
 * {@link #close()} or {@link #destroy()} is never called, the Cleaner will zero the
 * off-heap buffer automatically when the object becomes unreachable and is garbage
 * collected. For the strongest guarantee — including deterministic zeroing timing —
 * always use this class in a try-with-resources block.
 *
 * <h2>Preferred usage pattern</h2>
 * <pre>{@code
 * try (SecureSecret secret = new SecureSecret(passwordChars)) {
 *     byte[] derivedKey = secret.use(bytes -> PBKDF2.derive(bytes, salt, iterations));
 *     // derivedKey is the result; the temporary byte[] copy was zeroed automatically
 * }
 * // destroy() was called by close() — off-heap memory is now zeroed
 * }</pre>
 *
 * <h2>Raw array access</h2>
 *
 * <p>When the consuming API requires a longer-lived array, use {@link #toByteArray()} or
 * {@link #toCharArray()} directly. In that case <strong>the caller is responsible</strong>
 * for zeroing the returned array after use:
 * <pre>{@code
 * byte[] raw = secret.toByteArray();
 * try {
 *     externalApi.authenticate(raw);
 * } finally {
 *     Arrays.fill(raw, (byte) 0);
 * }
 * }</pre>
 *
 * @see javax.security.auth.Destroyable
 * @see java.nio.ByteBuffer#allocateDirect(int)
 */
public final class SecureSecret implements AutoCloseable, Destroyable {

    private static final Cleaner CLEANER = Cleaner.create();

    private final ByteBuffer buffer;
    private final int capacity;
    private final Cleaner.Cleanable cleanable;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private volatile boolean destroyed = false;

    /**
     * Creates a {@code SecureSecret} from a raw byte array.
     *
     * <p><strong>Ownership transfer:</strong> this constructor zeroes {@code secret} before
     * returning. Callers must not read from the array after this call.
     *
     * @param secret the sensitive bytes to protect; must not be {@code null}
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public SecureSecret(byte[] secret) {
        Objects.requireNonNull(secret, "secret must not be null");
        try {
            capacity = secret.length;
            buffer = ByteBuffer.allocateDirect(capacity);
            buffer.put(secret);
            buffer.flip();
        } finally {
            Arrays.fill(secret, (byte) 0); // wipe the input also in case of an error
        }
        cleanable = CLEANER.register(this, new ZeroAction(buffer, capacity));
    }

    /**
     * Creates a {@code SecureSecret} from a char array, encoding it as UTF-8 bytes
     * before storing it off-heap.
     *
     * <p><strong>Ownership transfer:</strong> this constructor zeroes {@code secret} before
     * returning. Callers must not read from the array after this call.
     *
     * <p>All intermediate heap buffers produced during UTF-8 encoding are zeroed before
     * this constructor returns, regardless of whether they have an accessible backing array.
     *
     * <p>Malformed input (a lone surrogate character) is rejected, since it can not be encoded as UTF-8
     * without loss. Also in this case {@code secret} is zeroed before the exception is thrown.
     *
     * @param secret the sensitive characters to protect; must not be {@code null}
     * @throws NullPointerException if {@code secret} is {@code null}
     * @throws IllegalArgumentException if {@code secret} contains a lone surrogate character or is too large
     */
    public SecureSecret(char[] secret) {
        Objects.requireNonNull(secret, "secret must not be null");
        ByteBuffer encoded = null;
        try {
            encoded = encodeUtf8(secret);
            buffer = ByteBuffer.allocateDirect(encoded.remaining());
            capacity = buffer.capacity();
            buffer.put(encoded);
            buffer.flip();
        } finally {
            Arrays.fill(secret, '\0'); // wipe the input also in case the encoding fails
            if (encoded != null) {
                wipeByteBuffer(encoded);
            }
        }
        cleanable = CLEANER.register(this, new ZeroAction(buffer, capacity));
    }

    /**
     * Passes a temporary {@code byte[]} copy of the secret to the given function, then
     * zeros the copy immediately after the function returns — even if it throws.
     *
     * <p>This is the preferred way to consume the secret when a {@code byte[]} is required,
     * because the on-heap copy never outlives the function call:
     * <pre>{@code
     * SecretKeySpec key = secret.use(bytes -> new SecretKeySpec(bytes, "AES"));
     * }</pre>
     *
     * <p>The read lock is held only while copying the buffer. The function {@code fn} is
     * invoked outside the lock so prolonged execution does not block {@link #destroy()}.
     *
     * @param <T> the return type of {@code fn}
     * @param fn  the function to receive the temporary copy; must not be {@code null}
     * @return the value returned by {@code fn}
     * @throws NullPointerException  if {@code fn} is {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     */
    public <T> T use(Function<byte[], T> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        byte[] copy;
        lock.readLock().lock();
        try {
            checkNotDestroyed();
            copy = copyBuffer();
        } finally {
            lock.readLock().unlock();
        }
        try {
            return fn.apply(copy);
        } finally {
            Arrays.fill(copy, (byte) 0);
        }
    }

    /**
     * Passes a temporary {@code char[]} copy of the secret to the given function, then
     * zeros the copy immediately after the function returns — even if it throws.
     *
     * <p>This is the preferred way to consume the secret when a {@code char[]} is required,
     * for example when constructing a {@link javax.crypto.spec.PBEKeySpec}:
     * <pre>{@code
     * PBEKeySpec spec = secret.useChars(chars -> new PBEKeySpec(chars, salt, 65536, 256));
     * }</pre>
     *
     * <p>The read lock is held while copying and UTF-8 decoding the buffer. The function
     * {@code fn} is invoked outside the lock so prolonged execution does not block
     * {@link #destroy()}.
     *
     * @param <T> the return type of {@code fn}
     * @param fn  the function to receive the temporary copy; must not be {@code null}
     * @return the value returned by {@code fn}
     * @throws NullPointerException  if {@code fn} is {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     */
    public <T> T useChars(Function<char[], T> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        char[] copy;
        lock.readLock().lock();
        try {
            checkNotDestroyed();
            copy = decodeBuffer();
        } finally {
            lock.readLock().unlock();
        }
        try {
            return fn.apply(copy);
        } finally {
            Arrays.fill(copy, '\0');
        }
    }

    /**
     * Returns a copy of the secret as a {@code byte[]}.
     *
     * <p><strong>The caller is responsible for zeroing the returned array after use:</strong>
     * <pre>{@code Arrays.fill(result, (byte) 0); }</pre>
     * Prefer {@link #use(Function)} when possible — it zeros the copy automatically and
     * the array never outlives the function call.
     *
     * @return a new byte array containing the secret; never {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     * @deprecated Use {@link #use(Function)} instead. The returned array lives on the heap
     *             and may be GC-copied or forgotten to be zeroed by the caller.
     */
    @Deprecated
    public byte[] toByteArray() {
        lock.readLock().lock();
        try {
            checkNotDestroyed();
            return copyBuffer();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns a copy of the secret decoded from UTF-8 as a {@code char[]}.
     *
     * <p><strong>The caller is responsible for zeroing the returned array after use:</strong>
     * <pre>{@code Arrays.fill(result, '\0'); }</pre>
     * Prefer {@link #useChars(Function)} when possible — it zeros the copy automatically
     * and the array never outlives the function call.
     *
     * <p>All intermediate buffers produced during UTF-8 decoding are zeroed before this
     * method returns, regardless of whether they have an accessible backing array.
     *
     * @return a new char array containing the decoded secret; never {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     * @deprecated Use {@link #useChars(Function)} instead. The returned array lives on the
     *             heap and may be GC-copied or forgotten to be zeroed by the caller.
     */
    @Deprecated
    public char[] toCharArray() {
        lock.readLock().lock();
        try {
            checkNotDestroyed();
            return decodeBuffer();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the secret as a {@code String}.
     *
     * <p><strong>Security warning:</strong> the returned {@code String} is immutable and
     * cannot be zeroed. It may remain in heap memory until GC collects it. Only call this
     * method when interoperating with a legacy API that strictly requires a {@code String}
     * and cannot be changed.
     *
     * @return a {@code String} representation of the secret
     * @throws IllegalStateException if this secret has already been destroyed
     * @deprecated {@code String} cannot be zeroed after use. Prefer {@link #use(Function)}
     *             or {@link #useChars(Function)} to limit the secret's exposure window.
     */
    @Deprecated
    public String toInsecureString() {
        return useChars(String::new);
    }

    /**
     * Zeros the off-heap memory region holding the secret, then marks this instance as destroyed.
     *
     * <p>The buffer is fully zeroed <em>before</em> the destroyed flag is set, so
     * {@link #isDestroyed()} returns {@code true} only after the wipe is complete.
     *
     * <p>Acquires an exclusive write lock in fair mode, so it is never starved by readers
     * and will wait for any in-progress reads to finish before zeroing the buffer. After
     * this method returns, any further call to {@link #use}, {@link #toByteArray},
     * {@link #toCharArray}, or {@link #toInsecureString} will throw
     * {@link IllegalStateException}.
     *
     * <p>Calling {@code destroy()} more than once is safe and has no effect after the
     * first call.
     */
    @Override
    public void destroy() {
        lock.writeLock().lock();
        try {
            if (!destroyed) {
                cleanable.clean();
                destroyed = true;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns {@code true} if {@link #destroy()} has been called and the buffer has been
     * fully zeroed.
     *
     * @return {@code true} if destroyed, {@code false} otherwise
     */
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Zeros and releases the secret by delegating to {@link #destroy()}.
     *
     * <p>Enables use in try-with-resources blocks:
     * <pre>{@code
     * try (SecureSecret secret = new SecureSecret(chars)) {
     *     // use secret
     * } // destroy() called automatically here
     * }</pre>
     */
    @Override
    public void close() {
        destroy();
    }

    /**
     * Returns a fixed placeholder string and never the actual secret value.
     *
     * <p>This prevents accidental secret disclosure through logging, debugging output,
     * or any code that calls {@code toString()} implicitly (e.g. string concatenation,
     * print statements).
     *
     * @return the literal string {@code "[SecureSecret]"}
     */
    @Override
    public String toString() {
        return "[SecureSecret]";
    }

    /**
     * Returns {@code true} only if {@code o} is the exact same instance as this secret.
     *
     * <p>Equality is identity-based, consistent with {@link #hashCode()}.
     * To compare secret content use {@link #contentEquals(SecureSecret)}.
     *
     * @param o the object to compare
     * @return {@code true} if {@code o} is this instance
     */
    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    /**
     * Returns an identity-based hash code that does not depend on the secret's content,
     * preventing any partial leakage of the secret value through hash bucket distribution.
     *
     * <p>Consistent with {@link #equals(Object)}: the same instance always produces the
     * same hash code, and no two distinct instances share a hash code.
     *
     * @return the identity hash code of this instance
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Compares the content of this secret to {@code other} using constant-time comparison
     * via {@link MessageDigest#isEqual}, preventing timing side-channel attacks.
     *
     * <p>Returns {@code false} immediately if {@code other} is {@code null} or if either
     * instance has been destroyed. Temporary byte copies are zeroed in all cases.
     *
     * @param other the secret to compare with; may be {@code null}
     * @return {@code true} if both secrets hold identical byte content and neither is destroyed
     */
    public boolean contentEquals(SecureSecret other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return !destroyed;
        }
        byte[] a;
        byte[] b;
        lock.readLock().lock();
        try {
            checkNotDestroyed();
            a = copyBuffer();
        } catch (IllegalStateException e) {
            return false;
        } finally {
            lock.readLock().unlock();
        }
        other.lock.readLock().lock();
        try {
            other.checkNotDestroyed();
            b = other.copyBuffer();
        } catch (IllegalStateException e) {
            Arrays.fill(a, (byte) 0);
            return false;
        } finally {
            other.lock.readLock().unlock();
        }
        try {
            return MessageDigest.isEqual(a, b);
        } finally {
            Arrays.fill(a, (byte) 0);
            Arrays.fill(b, (byte) 0);
        }
    }

    /**
     * Copies the off-heap buffer into a new on-heap {@code byte[]} using a bulk read via
     * {@link ByteBuffer#duplicate()}, which avoids mutating the original buffer's position.
     *
     * <p>Must be called while holding at least the read lock.
     *
     * @return a fresh byte array containing the current buffer contents
     */
    private byte[] copyBuffer() {
        byte[] copy = new byte[capacity];
        buffer.duplicate().get(copy, 0, capacity);
        return copy;
    }

    /**
     * Copies and UTF-8 decodes the off-heap buffer into a new {@code char[]}.
     * All intermediate byte and char buffers are zeroed before returning.
     *
     * <p>Must be called while holding at least the read lock.
     *
     * @return a fresh char array containing the decoded secret
     */
    private char[] decodeBuffer() {
        byte[] bytes = copyBuffer();
        CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        try {
            char[] chars = new char[decoded.remaining()];
            decoded.get(chars);
            return chars;
        } finally {
            wipeCharBuffer(decoded);
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Encodes the given characters as UTF-8 into a single heap buffer which is sized up front, so that no
     * intermediate (not wiped) copies are created during the encoding. Malformed or unmappable characters
     * (lone surrogates) are rejected; the partially encoded buffer is wiped before the exception is thrown.
     *
     * @param secret the characters to encode
     * @return the encoded bytes (position 0, limit at the end of the data); the caller must wipe it
     * @throws IllegalArgumentException in case the secret is too large or contains malformed characters
     */
    private static ByteBuffer encodeUtf8(char[] secret) {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        long maxSize = (long) Math.ceil((double) encoder.maxBytesPerChar() * secret.length);
        if (maxSize > Integer.MAX_VALUE - 8) {
            throw new IllegalArgumentException("Secret is too large!");
        }

        ByteBuffer encoded = ByteBuffer.allocate((int) maxSize);
        CharBuffer input = CharBuffer.wrap(secret);
        CoderResult result = encoder.encode(input, encoded, true);
        if (!result.isError()) {
            result = encoder.flush(encoded);
        }

        if (result.isError()) {
            wipeByteBuffer(encoded);
            throw new IllegalArgumentException("Secret contains malformed characters (lone surrogate) at position " + input.position() + "!");
        }

        encoded.flip();
        return encoded;
    }

    /**
     * Zeros all bytes in {@code buf}, using the backing array if available and absolute
     * index puts otherwise, to handle both heap and direct buffers.
     *
     * @param buf the byte buffer to wipe
     */
    private static void wipeByteBuffer(ByteBuffer buf) {
        if (buf.hasArray()) {
            Arrays.fill(buf.array(), (byte) 0);
        } else {
            for (int i = 0; i < buf.capacity(); i++) {
                buf.put(i, (byte) 0);
            }
        }
    }

    /**
     * Zeros all chars in {@code buf}, using the backing array if available and absolute
     * index puts otherwise, to handle both heap and direct buffers.
     *
     * @param buf the char buffer to wipe
     */
    private static void wipeCharBuffer(CharBuffer buf) {
        if (buf.hasArray()) {
            Arrays.fill(buf.array(), '\0');
        } else {
            for (int i = 0; i < buf.capacity(); i++) {
                buf.put(i, '\0');
            }
        }
    }

    /**
     * @throws IllegalStateException if {@link #destroy()} has already been called
     */
    private void checkNotDestroyed() {
        if (destroyed) {
            throw new IllegalStateException("SecureSecret has been destroyed");
        }
    }

    /**
     * Safety-net zeroing action registered with {@link Cleaner}.
     *
     * <p>Holds only the direct buffer and its capacity — no reference to the enclosing
     * {@link SecureSecret} — so the Cleaner's reference does not prevent GC of the secret.
     * Invoked by {@link #destroy()} for explicit zeroing and, as a fallback, when the
     * secret becomes unreachable without {@link #destroy()} having been called.
     */
    private static final class ZeroAction implements Runnable {

        private final ByteBuffer buffer;
        private final int capacity;

        /**
         * Constructor for ZeroAction
         *
         * @param buffer the direct byte buffer to zero
         * @param capacity the buffer capacity
         */
        ZeroAction(ByteBuffer buffer, int capacity) {
            this.buffer = buffer;
            this.capacity = capacity;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (int i = 0; i < capacity; i++) {
                buffer.put(i, (byte) 0);
            }
        }
    }
}
