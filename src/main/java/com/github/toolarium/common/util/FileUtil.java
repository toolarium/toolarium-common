/*
 * FileUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Defines a file utility.
 *  
 * @author patrick
 */
public final class FileUtil {
    public static final char SLASH = '/';
    public static final char BACKSLASH = '\\';
    public static final String SLASH_STR = "/";
    public static final String BACKSLASH_STR = "\\";

    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final FileUtil INSTANCE = new FileUtil();
    }

    
    /**
     * Constructor
     */
    private FileUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static FileUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Slashify the path
     *
     * @param path the path
     * @return the prepared path
     */
    public String slashify(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        
        String p = path;
        if (File.separatorChar != SLASH) {
            p = p.replace(File.separatorChar, SLASH);
        }
        
        if (!p.startsWith(SLASH_STR) && !p.substring(1, 2).equals(":")) {
            p = SLASH_STR + p;
        }

        File f = new File(p);
        if (f.isDirectory()) {
            if (!p.endsWith(SLASH_STR) && f.isDirectory()) {
                p = p + SLASH_STR;
            }
        }

        int end = p.length();
        if (p.length() > 0) {
            for (int i = p.length() - 1; i > 0; i--) {
                if (p.charAt(i)  == SLASH && p.charAt(i - 1)  == SLASH) {
                    end = i;
                }
            }
            
            if (p.length() > end) {
                p = p.substring(0, end);
            }
        }
        
        return p;
    }

    
    /**
     * Checks whether the given file is readable.
     * 
     * @param fileName the filename to be checked
     * @return true if the file can be read otherwise false
     */
    public boolean isReadable(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        File fileToCheck = new File(fileName);
        if (!fileToCheck.exists()) {
            return false;
        }
        
        return fileToCheck.canRead();
    }

    
    /**
     * Checks whether the given file is writable.
     * 
     * @param fileName the filename to be checked
     * @return true if the file can be written otherwise false
     */
    public boolean isWritable(String fileName) {
        if (fileName == null) {
            return false;
        }

        File fileToCheck = new File(fileName);
        if (!fileToCheck.exists()) {
            return false;
        }

        return fileToCheck.canWrite();
    }
    
    
    /**
     * Extract the path from an url
     *
     * @param url the url
     * @return the path
     */
    public String extractURLPath(URL url) {
        if (url == null) {
            return null;
        }
        
        String result = "" + new File(url.getPath());
        result = result.replace("\\", "/");
        result = result.replace("file:/", "");

        int lastIndex = result.indexOf('!');
        if (lastIndex > 0) {
            result = result.substring(0, lastIndex);
        }
        
        if (!result.startsWith("/")) {
            lastIndex = result.indexOf(':');
            if (lastIndex < 0) {
                result = "/" + result;
            }
        }

        return result;
    }

    
    /**
     * Read the file content
     *
     * @param file the file
     * @return the content
     * @throws IOException In case of an IO error
     */
    public String readFileContent(File file) throws IOException {
        return readFileContent(file, StandardCharsets.UTF_8);
    }

    
    /**
     * Read the file content
     *
     * @param file the file
     * @return the content
     * @throws IOException In case of an IO error
     */
    public String readFileContent(Path file) throws IOException {
        return readFileContent(file, StandardCharsets.UTF_8);
    }

    
    /**
     * Read the file content
     *
     * @param file the file
     * @param charset the charset
     * @return the content
     * @throws IOException In case of an IO error
     */
    public String readFileContent(File file, Charset charset) throws IOException {
        if (file == null) {
            return null;
        }
        
        return readFileContent(file.toPath(), StandardCharsets.UTF_8);
    }

    
    /**
     * Read the file content
     *
     * @param file the file
     * @param charset the charset
     * @return the content
     * @throws IOException In case of an IO error
     */
    public String readFileContent(Path file, Charset charset) throws IOException {
        return new String(Files.readAllBytes(file), charset);
    }


    /**
     * Read file content from a url
     *
     * @param url the url
     * @return the content
     * @throws IOException In case of an IO error
     */
    public String readFileContent(URL url) throws IOException {
        if (url == null) {
            return "";
        }

        InputStream src = null;
        try {
            ByteArrayOutputStream dest = new ByteArrayOutputStream();
            src = url.openStream();
            ChannelUtil.getInstance().channelCopy(src, dest);
            return dest.toString();
        } finally {
            if (src != null) {
                src.close();
            }
        }
    }
}
