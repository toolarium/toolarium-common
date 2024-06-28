/*
 * ChannelUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
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

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
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
     * @param src the source channel
     * @param dest the destination channel
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
     * @param src the source channel
     * @param dest the destination channel
     * @return the copied bytes
     * @exception IOException in case of error
     */
    public long channelCopy(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        long size = 0;

        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();

            // write to the channel, may block
            size += dest.write(buffer);

            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }

        // EOF will leave buffer in fill state
        buffer.flip();

        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            size += dest.write(buffer);
        }

        return size;
    }

    
    /**
     * This method copies data from the src channel and writes it to the dest channel until EOF on src. 
     * This implementation makes use of compact() on the temp buffer to pack down the data if the buffer wasn't fully drained.  
     * This may result in data copying, but minimizes system calls. It also requires a cleanup loop to make sure all the data gets sent.
     * 
     * @param src the source channel
     * @param dest the destination channel
     * @param messageDigest the message digest
     * @return the copied bytes
     * @exception IOException in case of error
     */
    public long channelCopy(InputStream src, OutputStream dest, MessageDigest messageDigest) throws IOException {
        ReadableByteChannel inChannel = null;
        if (messageDigest != null) {
            inChannel = Channels.newChannel(new DigestInputStream(src, messageDigest));
        } else {
            inChannel = Channels.newChannel(src);
        }

        WritableByteChannel outChannel = Channels.newChannel(dest);
        return channelCopy(inChannel, outChannel);
    }
}
