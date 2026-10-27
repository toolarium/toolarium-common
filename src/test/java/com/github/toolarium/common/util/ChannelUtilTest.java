/*
 * ChannelUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.ByteArray;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link ChannelUtil}.
 *
 * @author patrick
 */
public class ChannelUtilTest {

    /**
     * Test that the caller's streams are not closed
     *
     * @throws IOException in case of error
     */
    @Test
    public void testStreamsNotClosed() throws IOException {
        byte[] data = "Hello channel copy".getBytes(StandardCharsets.UTF_8);
        TrackingInputStream in = new TrackingInputStream(data);
        TrackingOutputStream out = new TrackingOutputStream();

        assertEquals(data.length, ChannelUtil.getInstance().channelCopy(in, out));
        assertArrayEquals(data, out.toByteArray());
        assertFalse(in.closed);
        assertFalse(out.closed);

        // the output stream is still usable
        out.write('!');
        assertEquals(data.length + 1, out.toByteArray().length);
    }


    /**
     * Test the copy with message digest
     *
     * @throws IOException in case of error
     * @throws NoSuchAlgorithmException in case of error
     */
    @Test
    public void testCopyWithDigest() throws IOException, NoSuchAlgorithmException {
        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        TrackingOutputStream out = new TrackingOutputStream();
        assertEquals(data.length, ChannelUtil.getInstance().channelCopy(new ByteArrayInputStream(data), out, digest));
        assertArrayEquals(data, out.toByteArray());
        assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(data), digest.digest());
        assertFalse(out.closed);
    }


    /**
     * Test that the copy into a closed byte array fails instead of spinning forever
     */
    @Test
    @SuppressWarnings("resource")
    public void testCopyToClosedByteArray() {
        ByteArray dest = new ByteArray();
        dest.close();
        assertTimeoutPreemptively(Duration.ofSeconds(5),
            () -> assertThrows(ClosedChannelException.class, () -> ChannelUtil.getInstance().channelCopy(new ByteArray("data"), dest)));
        assertEquals(0, dest.length());
    }


    /**
     * Test the copy from and to non-blocking channels which read or write no data for a while
     *
     * @throws IOException in case of error
     */
    @Test
    public void testCopyNonBlockingChannel() throws IOException {
        byte[] data = "non-blocking channel copy".getBytes(StandardCharsets.UTF_8);
        IdleReadableChannel src = new IdleReadableChannel(data, 40);
        IdleWritableChannel dest = new IdleWritableChannel(40);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertEquals(data.length, ChannelUtil.getInstance().channelCopy(src, dest)));
        assertArrayEquals(data, dest.toByteArray());
    }


    /**
     * Test that an interrupt stops the copy from a non-blocking channel which reads no data
     */
    @Test
    public void testCopyNonBlockingChannelInterrupted() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            IdleReadableChannel src = new IdleReadableChannel(new byte[0], Integer.MAX_VALUE);
            Thread.currentThread().interrupt();
            try {
                assertThrows(InterruptedIOException.class, () -> ChannelUtil.getInstance().channelCopy(src, new IdleWritableChannel(0)));
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        });
    }


    /**
     * Non-blocking readable channel which reads no data for a number of calls, then the data and then EOF
     */
    private static final class IdleReadableChannel implements ReadableByteChannel {
        private final ByteBuffer data;
        private int idleReads;

        /**
         * Constructor
         *
         * @param data the data
         * @param idleReads the number of calls which read no data
         */
        IdleReadableChannel(byte[] data, int idleReads) {
            this.data = ByteBuffer.wrap(data);
            this.idleReads = idleReads;
        }

        /**
         * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
         */
        @Override
        public int read(ByteBuffer dst) {
            if (idleReads > 0) {
                idleReads--;
                return 0;
            }

            if (!data.hasRemaining()) {
                return -1;
            }

            int count = Math.min(dst.remaining(), data.remaining());
            for (int i = 0; i < count; i++) {
                dst.put(data.get());
            }

            return count;
        }

        /**
         * @see java.nio.channels.Channel#isOpen()
         */
        @Override
        public boolean isOpen() {
            return true;
        }

        /**
         * @see java.nio.channels.Channel#close()
         */
        @Override
        public void close() {
            // NOP
        }
    }


    /**
     * Non-blocking writable channel which writes no data for a number of calls
     */
    private static final class IdleWritableChannel implements WritableByteChannel {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private int idleWrites;

        /**
         * Constructor
         *
         * @param idleWrites the number of calls which write no data
         */
        IdleWritableChannel(int idleWrites) {
            this.idleWrites = idleWrites;
        }

        /**
         * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
         */
        @Override
        public int write(ByteBuffer src) {
            if (idleWrites > 0 && src.hasRemaining()) {
                idleWrites--;
                return 0;
            }

            int count = src.remaining();
            while (src.hasRemaining()) {
                out.write(src.get());
            }

            return count;
        }

        /**
         * Get the written data
         *
         * @return the written data
         */
        byte[] toByteArray() {
            return out.toByteArray();
        }

        /**
         * @see java.nio.channels.Channel#isOpen()
         */
        @Override
        public boolean isOpen() {
            return true;
        }

        /**
         * @see java.nio.channels.Channel#close()
         */
        @Override
        public void close() {
            // NOP
        }
    }


    /**
     * Input stream which tracks the close
     */
    private static class TrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        /**
         * Constructor
         *
         * @param data the data
         */
        TrackingInputStream(byte[] data) {
            super(data);
        }

        /**
         * @see java.io.ByteArrayInputStream#close()
         */
        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }


    /**
     * Output stream which tracks the close
     */
    private static final class TrackingOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        /**
         * @see java.io.ByteArrayOutputStream#close()
         */
        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
