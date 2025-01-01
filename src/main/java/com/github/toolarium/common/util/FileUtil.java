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
import java.util.List;


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
     * Simplify a given path
     *
     * @param path the path to simplify
     * @return the path
     */
    public String simplifyPath(String path) {
        if (path == null) {
            return null;
        }

        String result = path.trim();
        if (result.length() == 0) {
            return result;
        }

        result = StringUtil.getInstance().trimRight(result.replace(BACKSLASH, SLASH), SLASH);
        List<String> pathList = StringUtil.getInstance().splitAsList(result, SLASH_STR);

        // simplify path
        String pathElement;
        int i = 0;
        while (i >= 0 && i < pathList.size()) {
            pathElement = pathList.get(i);

            if ("..".equals(pathElement)) {
                if ((i - 1) >= 0) {
                    pathList.remove(i - 1);
                }

                if ((i - 1) >= 0) {
                    pathList.remove(i - 1);
                }
                
                i--;
            } else {
                i++;
            }
        }

        // put string together
        result = "";
        for (String p : pathList) {
            if (result.length() == 0) {
                result = p;
            } else {
                result += SLASH_STR + p;
            }
        }

        if (path.endsWith(SLASH_STR) && !result.endsWith(SLASH_STR)) {
            result += SLASH_STR;
        }

        if (path.startsWith(SLASH_STR) && !result.startsWith(SLASH_STR)) {
            result = SLASH_STR + result;
        }

        return result;
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
     * Check if a given file exist
     *
     * @param fileName the filename
     * @return true if the file exist otherwise false
     */
    public boolean existFile(String fileName) {
        if (fileName == null) {
            return false;
        }

        return new File(fileName).exists();
    }

    
    /**
     * Checks whether the given file or directory is readable.
     * 
     * @param fileName the filename to be checked
     * @return true if the file can be read otherwise false
     */
    public boolean isReadable(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        final File fileToCheck = new File(fileName);
        return fileToCheck.exists() && fileToCheck.canRead();
    }

    
    /**
     * Checks whether the given file or directory is writable.
     * 
     * @param fileName the filename to be checked
     * @return true if the file can be written otherwise false
     */
    public boolean isWritable(String fileName) {
        if (fileName == null) {
            return false;
        }

        final File fileToCheck = new File(fileName);
        return fileToCheck.exists() && fileToCheck.canWrite();
    }

    
    /**
     * Removes a file
     *
     * @param fileName the file to remove
     * @return returns true if all deletions were successful.
     */
    public boolean removeFile(String fileName) {
        if (fileName == null) {
            return false;
        }

        return removeFile(new File(fileName));
    }

    
    /**
     * Removes a file
     *
     * @param file the file to remove
     * @return removes a file
     */
    public boolean removeFile(File file) {
        if (file == null) {
            return false;
        }
        
        if (file.delete()) {
            return true;
        }
        
        // try again
        System.gc(); // CHECKSTYLE IGNORE THIS LINE

        try {
            Thread.sleep(100L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        return file.delete();
    }

    
    /**
     * Deletes all files and sub directories under dir. If a deletion fails, the method stops attempting to delete and returns false.
     * 
     * @param dir the directory to delete
     * @return returns true if all deletions were successful.
     */
    public boolean removeDirectory(Path dir) {
        return removeDirectory(dir.toFile());
    }

    /**
     * Deletes all files and sub directories under dir. If a deletion fails, the method stops attempting to delete and returns false.
     * 
     * @param dir the directory to delete
     * @return returns true if all deletions were successful.
     */
    public boolean removeDirectory(File dir) {
        if (dir == null) {
            return false;
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();

            for (int i = 0; i < children.length; i++) {
                boolean success = removeDirectory(new File(dir, children[i]));

                if (!success) {
                    return false;
                }
            }
        }

        // the directory is now empty so delete it
        return removeFile(dir);
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

    
    /**
     * Write the file content
     *
     * @param file the file
     * @param content the content
     * @throws IOException In case of an IO error
     */
    public void writeFileContent(Path file, String content) throws IOException {
        writeFileContent(file, StandardCharsets.UTF_8, content); 
    }

    
    /**
     * Write the file content
     *
     * @param file the file
     * @param content the content
     * @throws IOException In case of an IO error
     */
    public void writeFileContent(File file, String content) throws IOException {
        writeFileContent(file.toPath(), StandardCharsets.UTF_8, content); 
    }

    
    /**
     * Write the file content
     *
     * @param file the file
     * @param charset the charset
     * @param content the content
     * @throws IOException In case of an IO error
     */
    public void writeFileContent(File file, Charset charset, String content) throws IOException {
        writeFileContent(file.toPath(), charset, content); 
    }

    
    /**
     * Write the file content
     *
     * @param file the file
     * @param charset the charset
     * @param content the content
     * @throws IOException In case of an IO error
     */
    public void writeFileContent(Path file, Charset charset, String content) throws IOException {
        Files.write(file, content.getBytes(charset)); 
    }
}
