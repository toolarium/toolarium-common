/*
 * ChannelUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * The channel util class
 *  
 * @author patrick
 */
public final class ChannelUtil {
    private static final int MAX_YIELD_COUNT = 16;
    private static final long IDLE_SLEEP_MILLIS = 1;
    private static final String INTERRUPTED_MESSAGE = "Channel copy interrupted while waiting for a non-blocking channel";

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChannelUtil INSTANCE = new ChannelUtil();
    }

    
    /**
     * Constructor
     */
    private ChannelUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ChannelUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * This method copies data from the src channel and writes it to the dest channel until EOF on src. 
     * This implementation makes use of compact() on the temp buffer to pack down the data if the buffer wasn't fully drained.  
     * This may result in data copying, but minimizes system calls. It also requires a cleanup loop to make sure all the data gets sent.
     * 
     * <p>The given streams are not closed (the destination stream is flushed), closing them is the responsibility of the caller.
     * 
     * @param src the source stream
     * @param dest the destination stream
     * @return the copied bytes
     * @exception IOException in case of error
     */
    public long channelCopy(InputStream src, OutputStream dest) throws IOException {
        return channelCopy(src, dest, null);
    }
    
    
    /**
     * This method copies data from the src channel and writes it to the dest channel until EOF on src. 
     * This implementation makes use of compact() on the temp buffer to pack down the data if the buffer wasn't fully drained.  
     * This may result in data copying, but minimizes system calls. It also requires a cleanup loop to make sure all the data gets sent.
     * 
     * <p>If the destination channel is (or gets) closed, the copy fails with a {@link ClosedChannelException} instead of waiting forever.
     * A non-blocking channel which reads or writes no data is polled with a short pause, the copy fails with an
     * {@link InterruptedIOException} if the current thread is interrupted while it waits for data.
     * 
     * @param src the source channel
     * @param dest the destination channel
     * @return the copied bytes
     * @exception IOException in case of error, a {@link ClosedChannelException} if the destination channel is closed or an
     *            {@link InterruptedIOException} if the thread is interrupted while it waits for a non-blocking channel
     */
    public long channelCopy(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        long size = 0;
        int idleCount = 0;

        int readBytes = src.read(buffer);
        while (readBytes != -1) {
            // prepare the buffer to be drained
            buffer.flip();

            // write to the channel, may block
            int writtenBytes = write(dest, buffer);
            size += writtenBytes;

            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();

            // a non-blocking channel may read or write nothing: wait a little bit instead of spinning
            if (readBytes > 0 || writtenBytes > 0) {
                idleCount = 0;
            } else {
                idleCount = waitForProgress(idleCount);
            }

            readBytes = src.read(buffer);
        }

        // EOF will leave buffer in fill state
        buffer.flip();

        // make sure the buffer is fully drained.
        idleCount = 0;
        while (buffer.hasRemaining()) {
            int writtenBytes = write(dest, buffer);
            size += writtenBytes;

            if (writtenBytes > 0) {
                idleCount = 0;
            } else {
                idleCount = waitForProgress(idleCount);
            }
        }

        return size;
    }


    
    /**
     * This method copies data from the src channel and writes it to the dest channel until EOF on src. 
     * This implementation makes use of compact() on the temp buffer to pack down the data if the buffer wasn't fully drained.  
     * This may result in data copying, but minimizes system calls. It also requires a cleanup loop to make sure all the data gets sent.
     * 
     * <p>The given streams are not closed (the destination stream is flushed), closing them is the responsibility of the caller.
     * 
     * @param src the source stream
     * @param dest the destination stream
     * @param messageDigest the message digest which is updated with the read data, can be null
     * @return the copied bytes
     * @exception IOException in case of error
     */
    public long channelCopy(InputStream src, OutputStream dest, MessageDigest messageDigest) throws IOException {
        ReadableByteChannel inChannel;
        if (messageDigest != null) {
            inChannel = Channels.newChannel(new DigestInputStream(src, messageDigest));
        } else {
            inChannel = Channels.newChannel(src);
        }

        // the channels are intentionally not closed: closing them would close the caller's streams
        WritableByteChannel outChannel = Channels.newChannel(dest);
        long size = channelCopy(inChannel, outChannel);
        dest.flush();
        return size;
    }


    /**
     * Wait for a non-blocking channel which read or wrote no data: first the processor is yielded a few times,
     * then the current thread sleeps for a short time, so that the copy does not use a full processor.
     *
     * @param idleCount the number of the previous attempts without any progress
     * @return the new number of attempts without any progress
     * @throws InterruptedIOException if the current thread is interrupted, the interrupt flag stays set
     */
    private int waitForProgress(int idleCount) throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException(INTERRUPTED_MESSAGE);
        }

        if (idleCount < MAX_YIELD_COUNT) {
            Thread.yield();
            return idleCount + 1;
        }

        try {
            Thread.sleep(IDLE_SLEEP_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            InterruptedIOException ex = new InterruptedIOException(INTERRUPTED_MESSAGE);
            ex.initCause(e);
            throw ex;
        }

        return idleCount;
    }


    /**
     * Writes the buffer to the destination channel. A closed channel (e.g. a closed
     * {@link com.github.toolarium.common.ByteArray} which accepts no data) is reported
     * by an exception, otherwise the copy loop would never terminate.
     *
     * @param dest the destination channel
     * @param buffer the buffer to write
     * @return the number of written bytes
     * @throws ClosedChannelException if the destination channel is closed
     * @throws IOException in case of error
     */
    private int write(WritableByteChannel dest, ByteBuffer buffer) throws IOException {
        if (!dest.isOpen()) {
            throw new ClosedChannelException();
        }

        return dest.write(buffer);
    }
}
