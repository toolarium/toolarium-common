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
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines a file utility.
 *  
 * @author patrick
 */
public final class FileUtil {
    /** The slash character. */
    public static final char SLASH = '/';

    /** The backslash character. */
    public static final char BACKSLASH = '\\';

    /** The slash as string. */
    public static final String SLASH_STR = "/";

    /** The backslash as string. */
    public static final String BACKSLASH_STR = "\\";

    /** The maximum size in bytes of a file which is read into memory by the search methods. */
    static final long MAX_FILE_SIZE = Integer.MAX_VALUE - 8L;

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);
    private static final String MESSAGE_SEPARATOR = ": ";
    private static final Pattern MULTIPLE_SLASHES = Pattern.compile("/{2,}");
    private static final String ARCHIVE_ENTRY_SEPARATOR = "!/";
    private static final String ARCHIVE_ENTRY_MARKER = "!";
    private static final String JAR_PROTOCOL = "jar";
    private static final String FILE_PROTOCOL = "file";
    private static final String FILE_URL_AUTHORITY_PREFIX = "file://";
    private static final String UNC_PREFIX = "//";
    private static final String LOCALHOST = "localhost";
    private static final String[] ARCHIVE_EXTENSIONS = {".jar", ".zip", ".war", ".ear", ".jmod"};
    private final RegularExpressionUtil regexUtil = new RegularExpressionUtil();

    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
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
     * Simplify a given path: backslashes are converted to slashes, <code>..</code> elements are resolved
     * and empty (double slashes) or <code>.</code> elements are removed. The root of an absolute path (<code>/</code>, a drive
     * like <code>C:</code> or <code>C:/</code>, a network path like <code>//server/share/</code>) is always kept:
     * <code>..</code> elements which would leave the root are discarded. Leading <code>..</code> elements of a relative path are kept.
     *
     * @param path the path to simplify
     * @return the path
     */
    public String simplifyPath(String path) {
        if (path == null) {
            return null;
        }

        String input = path.trim();
        if (input.length() == 0) {
            return input;
        }

        input = input.replace(BACKSLASH, SLASH);

        // determine the root, which can never be removed: a network path (UNC) like //server/share/, a drive or a slash
        String root = getNetworkPathRoot(input);
        if (root == null) {
            root = "";
            if (input.length() >= 2 && input.charAt(1) == ':') {
                root = input.substring(0, 2);
                if (input.length() > 2 && input.charAt(2) == SLASH) {
                    root += SLASH_STR;
                }
            } else if (input.charAt(0) == SLASH) {
                root = SLASH_STR;
            }
        }

        boolean isAbsolute = !root.isEmpty();
        List<String> pathList = StringUtil.getInstance().splitAsList(input.substring(root.length()), SLASH_STR);

        // simplify path using a stack-based approach
        List<String> resolvedParts = new ArrayList<>();
        for (String pathElement : pathList) {
            if (pathElement.isEmpty() || ".".equals(pathElement)) {
                // empty and current directory elements are dropped
                continue;
            }

            if ("..".equals(pathElement)) {
                if (!resolvedParts.isEmpty() && !"..".equals(resolvedParts.get(resolvedParts.size() - 1))) {
                    resolvedParts.remove(resolvedParts.size() - 1);
                } else if (!isAbsolute) {
                    // for relative paths, keep leading ".." references
                    resolvedParts.add(pathElement);
                }
                // for absolute paths, silently discard ".." that would escape root
            } else {
                resolvedParts.add(pathElement);
            }
        }

        // put string together
        StringBuilder sb = new StringBuilder();
        for (String p : resolvedParts) {
            if (sb.length() > 0) {
                sb.append(SLASH);
            }
            sb.append(p);
        }

        if (input.endsWith(SLASH_STR) && sb.length() > 0) {
            sb.append(SLASH);
        }

        return root + sb.toString();
    }

    
    /**
     * Get the root of a network path (UNC) with slashes, e.g. <code>//server/share/</code> of <code>//server/share/dir</code>.
     * The server and the share name must not be empty.
     *
     * @param path the path with slashes
     * @return the root including a following slash or null if it is no network path
     */
    private String getNetworkPathRoot(String path) {
        if (!path.startsWith("//") || path.length() < 3 || path.charAt(2) == SLASH) {
            return null;
        }

        final int serverEnd = path.indexOf(SLASH, 2);
        if (serverEnd < 0 || serverEnd + 1 >= path.length() || path.charAt(serverEnd + 1) == SLASH) {
            return null;
        }

        final int shareEnd = path.indexOf(SLASH, serverEnd + 1);
        if (shareEnd < 0) {
            return path;
        }

        return path.substring(0, shareEnd + 1);
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
        
        if (!p.startsWith(SLASH_STR) && (p.length() < 2 || p.charAt(1) != ':')) {
            p = SLASH_STR + p;
        }

        // collapse multiple slashes to one, the leading double slash of a network path (UNC) is kept
        if (getNetworkPathRoot(p) != null) {
            p = SLASH_STR + MULTIPLE_SLASHES.matcher(p.substring(1)).replaceAll(SLASH_STR);
        } else {
            p = MULTIPLE_SLASHES.matcher(p).replaceAll(SLASH_STR);
        }

        if (!p.endsWith(SLASH_STR) && new File(p).isDirectory()) {
            p = p + SLASH_STR;
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
     * Removes a file. In case the first attempt fails because the file may still be in use (e.g. held open on Windows),
     * it is retried once after a short delay. A missing file or a directory with content is not retried.
     *
     * @param fileName the file to remove
     * @return true if the file was deleted; false if the name is null, the file does not exist or could not be deleted
     */
    public boolean removeFile(String fileName) {
        if (fileName == null) {
            return false;
        }

        return removeFile(new File(fileName));
    }

    
    /**
     * Removes a file. In case the first attempt fails because the file may still be in use (e.g. held open on Windows),
     * it is retried once after a short delay. A missing file or a directory with content is not retried.
     *
     * @param file the file to remove
     * @return true if the file was deleted; false if the file is null, does not exist or could not be deleted
     */
    public boolean removeFile(File file) {
        if (file == null) {
            return false;
        }
        
        final Path path;
        try {
            path = file.toPath();
        } catch (InvalidPathException e) {
            return false; // e.g. a file name which is not valid on this platform
        }

        try {
            Files.delete(path); // a link is removed itself, it is not followed
            return true;
        } catch (NoSuchFileException | DirectoryNotEmptyException e) {
            return false; // nothing to delete or a directory with content: a retry can not help
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not remove " + file + ", try again" + MESSAGE_SEPARATOR + e.getMessage(), e);
            }
        }

        // try again: a file which is still held open (e.g. an unclosed stream on Windows) may be released by the garbage collection
        System.gc(); // CHECKSTYLE IGNORE THIS LINE

        try {
            Thread.sleep(100L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        return file.delete();
    }


    
    /**
     * Deletes the given directory including all files and sub directories. If the given path is a plain file, only the file is deleted.
     * If a deletion fails, the method stops attempting to delete and returns false.
     * Symbolic links (and other links such as Windows junctions) are not followed: only the link itself is removed, never the content of its target.
     *
     * @param dir the directory to delete
     * @return true if all deletions were successful; false if dir is null or a deletion failed
     */
    public boolean removeDirectory(Path dir) {
        if (dir == null) {
            return false;
        }

        return removeDirectory(dir.toFile());
    }

    /**
     * Deletes the given directory including all files and sub directories. If the given file is a plain file, only the file is deleted.
     * If a deletion fails, the method stops attempting to delete and returns false.
     * Symbolic links (and other links such as Windows junctions) are not followed: only the link itself is removed, never the content of its target.
     *
     * @param dir the directory to delete
     * @return true if all deletions were successful; false if dir is null or a deletion failed
     */
    public boolean removeDirectory(File dir) {
        if (dir == null) {
            return false;
        }

        final Path start;
        try {
            start = dir.toPath();
        } catch (InvalidPathException e) {
            return false;
        }

        // the walk does not follow links: the attributes of every entry are read without following links,
        // a link is reported as file (or as directory with the other flag in case of a Windows junction) and removed as link
        final AtomicBoolean success = new AtomicBoolean(true);
        try {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                /**
                 * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
                 */
                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
                    if (attrs.isSymbolicLink() || attrs.isOther()) {
                        // a link such as a Windows junction: remove only the link itself, never descend into it
                        return removeEntry(directory, success, FileVisitResult.SKIP_SUBTREE);
                    }

                    return FileVisitResult.CONTINUE;
                }

                /**
                 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
                 */
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    return removeEntry(file, success, FileVisitResult.CONTINUE);
                }

                /**
                 * @see java.nio.file.SimpleFileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
                 */
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    if (isLinkEntry(file)) {
                        // a link which can not be opened, e.g. a Windows junction with a deleted target: remove the link itself
                        return removeEntry(file, success, FileVisitResult.CONTINUE);
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not access " + file + MESSAGE_SEPARATOR + e.getMessage(), e);
                    }

                    success.set(false);
                    return FileVisitResult.TERMINATE;
                }

                /**
                 * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
                 */
                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException e) {
                    if (e != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Could not list directory " + directory + MESSAGE_SEPARATOR + e.getMessage(), e);
                        }

                        success.set(false);
                        return FileVisitResult.TERMINATE;
                    }

                    // the directory is now empty so delete it
                    return removeEntry(directory, success, FileVisitResult.CONTINUE);
                }
            });
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not remove directory " + dir + MESSAGE_SEPARATOR + e.getMessage(), e);
            }

            LOG.warn("Could not remove directory " + dir + MESSAGE_SEPARATOR + e.getMessage());
            return false;
        }

        return success.get();
    }


    /**
     * Checks if an entry is a link (a symbolic link or a Windows junction) without following it.
     *
     * @param path the path
     * @return true if the entry is a link; false if not or if its attributes can not be read
     */
    private boolean isLinkEntry(Path path) {
        try {
            final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return attrs.isSymbolicLink() || attrs.isOther();
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Remove a single entry of a directory tree (a file, an empty directory or a link) without following links.
     *
     * @param path the path to remove
     * @param success the success flag, which is set to false in case the entry could not be removed
     * @param next the visit result in case the entry was removed
     * @return the given next visit result if the entry was removed, otherwise {@link FileVisitResult#TERMINATE}
     */
    private FileVisitResult removeEntry(Path path, AtomicBoolean success, FileVisitResult next) {
        if (removeFile(path.toFile())) {
            return next;
        }

        success.set(false);
        return FileVisitResult.TERMINATE;
    }
    
    
    /**
     * Extract the path from an url, percent-encoded characters (e.g. {@code %20}) are decoded.
     * The entry of an archive (e.g. {@code app.jar!/META-INF}) is cut in case of a {@code jar:} url or in case the part
     * before the {@code !} ends with an archive extension ({@code .jar}, {@code .zip}, {@code .war}, {@code .ear} or
     * {@code .jmod}, case-insensitive); otherwise the {@code !} is part of the path. The host of a UNC file url
     * (e.g. {@code file://server/share/app.jar}) is kept: the result is {@code //server/share/app.jar}.
     *
     * @param url the url
     * @return the path
     */
    public String extractURLPath(URL url) {
        if (url == null) {
            return null;
        }

        // cut the entry of an archive (e.g. app.jar!/META-INF) before decoding, so that an encoded ! (%21) stays part of the path
        final boolean isJarUrl = JAR_PROTOCOL.equalsIgnoreCase(url.getProtocol());
        String path = cutArchiveEntry(url.getPath(), isJarUrl);

        // the host of a UNC file url (e.g. file://server/share/app.jar), also inside of a jar url
        String host = null;
        if (FILE_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
            host = url.getHost();
        } else if (isJarUrl && path.startsWith(FILE_URL_AUTHORITY_PREFIX)) {
            final int authorityStart = FILE_URL_AUTHORITY_PREFIX.length();
            final int authorityEnd = path.indexOf(SLASH, authorityStart);
            if (authorityEnd > authorityStart) {
                final String authority = path.substring(authorityStart, authorityEnd);

                // a drive letter (e.g. file://D:/) is not a host
                if (authority.indexOf(':') < 0) {
                    host = authority;
                    path = path.substring(authorityEnd);
                }
            }
        }

        String result = "" + new File(decodeURLPath(path));
        result = result.replace("\\", "/");
        result = result.replace("file:/", "");

        if (!result.startsWith("/") && result.indexOf(':') < 0) {
            result = "/" + result;
        }

        if (host != null && !host.isEmpty() && !LOCALHOST.equalsIgnoreCase(host)) {
            result = UNC_PREFIX + host + result;
        }

        return result;
    }


    /**
     * Cut the entry of an archive from an url path (e.g. {@code /opt/app.jar!/META-INF} gives {@code /opt/app.jar}).
     * The entry is cut at the first {@code !/} in case of a jar url, otherwise at the first {@code !/} where the part
     * before ends with an archive extension.
     *
     * @param path the url path
     * @param isJarUrl true in case the url is a jar url
     * @return the path without the archive entry or the path itself if it contains no archive entry
     */
    private String cutArchiveEntry(String path, boolean isJarUrl) {
        int index = path.indexOf(ARCHIVE_ENTRY_SEPARATOR);
        while (index > 0) {
            final String archivePath = path.substring(0, index);
            if (isJarUrl || isArchiveFileName(archivePath)) {
                return archivePath;
            }

            index = path.indexOf(ARCHIVE_ENTRY_SEPARATOR, index + 1);
        }

        if (path.length() > 1 && path.endsWith(ARCHIVE_ENTRY_MARKER)) {
            final String archivePath = path.substring(0, path.length() - 1);
            if (isJarUrl || isArchiveFileName(archivePath)) {
                return archivePath;
            }
        }

        return path;
    }


    /**
     * Check if a path ends with an archive extension ({@code .jar}, {@code .zip}, {@code .war}, {@code .ear} or {@code .jmod}), case-insensitive.
     *
     * @param path the path
     * @return true if the path ends with an archive extension
     */
    private boolean isArchiveFileName(String path) {
        final String lowerCasePath = path.toLowerCase(Locale.ROOT);
        for (String extension : ARCHIVE_EXTENSIONS) {
            if (lowerCasePath.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Decode the percent-encoded characters of an url path. A plus sign stays a plus sign.
     *
     * @param path the url path
     * @return the decoded path or the path itself in case it is not properly encoded
     */
    private String decodeURLPath(String path) {
        if (path == null || path.indexOf('%') < 0) {
            return path;
        }

        try {
            return URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return path;
        }
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
        
        return readFileContent(file.toPath(), charset);
    }


    /**
     * Read the file content
     *
     * @param file the file
     * @param charset the charset
     * @return the content or null if the file is null
     * @throws IOException In case of an IO error
     */
    public String readFileContent(Path file, Charset charset) throws IOException {
        if (file == null) {
            return null;
        }

        return new String(Files.readAllBytes(file), charset);
    }

    
    /**
     * Read file content from a url (UTF-8)
     *
     * @param url the url
     * @return the content or null if the url is null
     * @throws IOException In case of an IO error
     */
    public String readFileContent(URL url) throws IOException {
        if (url == null) {
            return null;
        }

        try (InputStream src = url.openStream()) {
            ByteArrayOutputStream dest = new ByteArrayOutputStream();
            ChannelUtil.getInstance().channelCopy(src, dest);
            return dest.toString(StandardCharsets.UTF_8);
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

    
    /**
     * Search files. Symbolic links are followed, also if their target is outside the root directory
     * ({@link LinkFollowMode#ALL}). Use {@link #searchFiles(Path, String, String, LinkFollowMode)} to define
     * how symbolic links are handled.
     *
     * @param rootDir the root path
     * @param searchContentPattern the search content pattern
     * @return the found files, files which can not be read or which are too large to be read into memory are skipped (logged as warning)
     * @throws IOException if an I/O error is thrown by a visitor method, e.g. a {@link NoSuchFileException} if the root directory does not exist
     * @throws IllegalArgumentException if the search pattern is invalid
     */
    public List<Path> searchFiles(Path rootDir, String searchContentPattern) throws IOException {
        return searchFiles(rootDir, null, searchContentPattern);
    }


    /**
     * Search files. Symbolic links are followed, also if their target is outside the root directory
     * ({@link LinkFollowMode#ALL}). Use {@link #searchFiles(Path, String, String, LinkFollowMode)} to define
     * how symbolic links are handled.
     *
     * @param rootDir the root path
     * @param fileExtension the file extension
     * @param searchContentPattern the search content pattern
     * @return the found files, files which can not be read or which are too large to be read into memory are skipped (logged as warning)
     * @throws IOException if an I/O error is thrown by a visitor method, e.g. a {@link NoSuchFileException} if the root directory does not exist
     * @throws IllegalArgumentException if the search pattern is invalid
     */
    public List<Path> searchFiles(Path rootDir, String fileExtension, String searchContentPattern) throws IOException {
        return searchFiles(rootDir, fileExtension, searchContentPattern, LinkFollowMode.ALL);
    }


    /**
     * Search files.
     *
     * @param rootDir the root path
     * @param fileExtension the file extension or null for all files
     * @param searchContentPattern the search content pattern
     * @param linkFollowMode defines how symbolic links (files and directories) are handled, null is handled as {@link LinkFollowMode#NONE}
     * @return the found files, files which can not be read or which are too large to be read into memory are skipped (logged as warning)
     * @throws IOException if an I/O error is thrown by a visitor method, e.g. a {@link NoSuchFileException} if the root directory does not exist
     * @throws IllegalArgumentException if the search pattern is invalid
     */
    public List<Path> searchFiles(Path rootDir, String fileExtension, String searchContentPattern, LinkFollowMode linkFollowMode) throws IOException {
        List<Path> foundFiles = new ArrayList<Path>();
        Pattern pattern = regexUtil.compile(searchContentPattern);
        walkFiles(rootDir, linkFollowMode, (file, target) -> {
            if (fileExtension == null || fileExtension.isEmpty() || file.getFileName().toString().endsWith(fileExtension)) {
                final byte[] bytes = readFileNoFollow(target);
                if (bytes == null) {
                    // file is too large, already logged
                    return;
                }

                final String content = new String(bytes, StandardCharsets.UTF_8);
                if (pattern.matcher(content).find()) {
                    foundFiles.add(file);
                }
            }
        });

        return foundFiles;
    }


    /**
     * Search and replace files. The files are read and written as UTF-8; files which are not valid UTF-8
     * (e.g. binary files or files in another encoding) are skipped and never modified.
     * The replacement is inserted literally: characters like <code>$</code> or <code>\</code> have no special
     * meaning, group references (e.g. <code>$1</code>) are not supported.
     * Symbolic links are followed (the target is modified), also if their target is outside the root directory
     * ({@link LinkFollowMode#ALL}). Use {@link #searchAndReplaceFiles(Path, String, String, LinkFollowMode)} to
     * define how symbolic links are handled.
     * A changed file is written in place, so that the file itself (hard links, owner, permissions) is kept. Before, its
     * original content is copied to a backup file in the temporary directory of the system ({@code java.io.tmpdir}),
     * which is used to restore the original content in case of a write error and removed afterwards. Files which are
     * too large to be read into memory are skipped.
     *
     * @param rootDir the root path
     * @param searchContentPattern the search content pattern (regular expression)
     * @param replacement the literal replacement
     * @return the changed files, files which can not be read or written or which are too large are skipped (logged as warning)
     * @throws IOException if an I/O error is thrown by a visitor method, e.g. a {@link NoSuchFileException} if the root directory does not exist
     * @throws IllegalArgumentException if the search pattern is invalid
     */
    public List<Path> searchAndReplaceFiles(Path rootDir, String searchContentPattern, String replacement) throws IOException {
        return searchAndReplaceFiles(rootDir, searchContentPattern, replacement, LinkFollowMode.ALL);
    }


    /**
     * Search and replace files. The files are read and written as UTF-8; files which are not valid UTF-8
     * (e.g. binary files or files in another encoding) are skipped and never modified.
     * The replacement is inserted literally: characters like <code>$</code> or <code>\</code> have no special
     * meaning, group references (e.g. <code>$1</code>) are not supported.
     * A followed symbolic link to a file modifies the target file.
     * A changed file is written in place, so that the file itself (hard links, owner, permissions) is kept. Before, its
     * original content is copied to a backup file in the temporary directory of the system ({@code java.io.tmpdir}),
     * which is used to restore the original content in case of a write error and removed afterwards. Files which are
     * too large to be read into memory are skipped.
     *
     * @param rootDir the root path
     * @param searchContentPattern the search content pattern (regular expression)
     * @param replacement the literal replacement
     * @param linkFollowMode defines how symbolic links (files and directories) are handled, null is handled as {@link LinkFollowMode#NONE}
     * @return the changed files, files which can not be read or written or which are too large are skipped (logged as warning)
     * @throws IOException if an I/O error is thrown by a visitor method, e.g. a {@link NoSuchFileException} if the root directory does not exist
     * @throws IllegalArgumentException if the search pattern is invalid
     */
    public List<Path> searchAndReplaceFiles(Path rootDir, String searchContentPattern, String replacement, LinkFollowMode linkFollowMode) throws IOException {
        List<Path> changedFiles = new ArrayList<Path>();
        Pattern pattern = regexUtil.compile(searchContentPattern);
        final String quotedReplacement = Matcher.quoteReplacement(replacement);
        walkFiles(rootDir, linkFollowMode, (file, target) -> {
            final byte[] bytes = readFileNoFollow(target);
            if (bytes == null) {
                // file is too large, already logged
                return;
            }

            final String content = decodeStrict(bytes, StandardCharsets.UTF_8);
            if (content == null) {
                // not a valid UTF-8 file: skip it to avoid corrupting it
                return;
            }

            final String updatedContent = pattern.matcher(content).replaceAll(quotedReplacement);
            if (!content.equals(updatedContent)) {
                replaceFileContent(target, updatedContent.getBytes(StandardCharsets.UTF_8));
                changedFiles.add(file);
            }
        });

        return changedFiles;
    }


    /**
     * Walk through all regular files of a directory tree. Files which can not be read or written are skipped (logged as warning).
     *
     * @param rootDir the root path
     * @param linkFollowMode defines how symbolic links are handled, null is handled as {@link LinkFollowMode#NONE}
     * @param fileProcessor the processor of the files
     * @throws IOException if an I/O error is thrown by a visitor method, e.g. a {@link NoSuchFileException} if the root directory does not exist
     */
    private void walkFiles(Path rootDir, LinkFollowMode linkFollowMode, FileProcessor fileProcessor) throws IOException {
        LinkFollowMode mode = LinkFollowMode.NONE;
        if (linkFollowMode != null) {
            mode = linkFollowMode;
        }

        Set<FileVisitOption> options = EnumSet.noneOf(FileVisitOption.class);
        if (mode != LinkFollowMode.NONE) {
            options.add(FileVisitOption.FOLLOW_LINKS);
        }

        final LinkFollowMode currentMode = mode;
        Files.walkFileTree(rootDir, options, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            private Path realRootDir;

            // real paths of the visited directories (link and junction detection) and of the processed files (process once)
            private final Deque<Path> realDirectories = new ArrayDeque<Path>();
            private final Set<Path> processedFiles = new HashSet<Path>();

            /**
             * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final Path realDir = toRealPathOrSkip(dir);
                if (realDir == null || (isLinkedDirectory(dir, realDir, realDirectories.peek())
                        && (currentMode == LinkFollowMode.NONE || (currentMode == LinkFollowMode.WITHIN_ROOT && !isInsideRoot(dir))))) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip directory " + dir + " (link follow mode " + currentMode + ")");
                    }

                    return FileVisitResult.SKIP_SUBTREE;
                }

                realDirectories.push(realDir);
                return FileVisitResult.CONTINUE;
            }

            /**
             * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
             */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                realDirectories.poll();
                if (e != null) {
                    throw e;
                }

                return FileVisitResult.CONTINUE;
            }

            /**
             * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    final Path target = resolveFile(file, attrs);
                    if (target != null && processedFiles.add(toRealFile(file, target, realDirectories.peek()))) {
                        fileProcessor.process(file, target);
                    }
                } catch (IOException e) {
                    return skipFile(file, e);
                }

                return FileVisitResult.CONTINUE;
            }

            /**
             * @see java.nio.file.SimpleFileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                if (file.equals(rootDir)) { // a missing or unreadable root directory is an error of the caller
                    throw e;
                }
                if (e instanceof NoSuchFileException) {
                    // the file vanished during the walk (e.g. deleted by another process), nothing to do
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip vanished file " + file + MESSAGE_SEPARATOR + e.getMessage(), e);
                    }

                    return FileVisitResult.CONTINUE;
                }

                return skipFile(file, e);
            }

            /**
             * Resolve the file to process: a regular file is processed itself, a followed symbolic link is resolved to its target.
             *
             * @param file the file
             * @param attrs the attributes of the file
             * @return the file to process or null if the file is skipped
             * @throws IOException In case the link target can not be resolved
             */
            private Path resolveFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isSymbolicLink() && !Files.isSymbolicLink(file)) {
                    if (attrs.isRegularFile() && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                        return file;
                    }

                    return null;
                }

                if (currentMode == LinkFollowMode.NONE) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip symbolic link " + file + ".");
                    }

                    return null;
                }

                final Path target = file.toRealPath();
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                    // e.g. a linked directory which is not searched
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip symbolic link " + file + ", the target is not a regular file.");
                    }

                    return null;
                }

                if (currentMode == LinkFollowMode.WITHIN_ROOT && !isInsideRoot(target)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip symbolic link " + file + ", the target is outside of " + rootDir + ".");
                    }

                    return null;
                }

                return target;
            }

            /**
             * Check if the real path of a file is inside the root directory.
             *
             * @param file the file
             * @return true if the file is inside the root directory
             * @throws IOException In case the real path can not be resolved
             */
            private boolean isInsideRoot(Path file) throws IOException {
                if (realRootDir == null) {
                    realRootDir = rootDir.toRealPath();
                }

                return file.toRealPath().startsWith(realRootDir);
            }
        });
    }



    /**
     * Get the real path of a file to process: a resolved link target is already a real path, a regular file is resolved
     * by the real path of its directory.
     *
     * @param file the file
     * @param target the file to process (the file itself or the target of a link)
     * @param realDirectory the real path of the directory of the file, null if unknown
     * @return the real path of the file
     * @throws IOException In case the real path can not be resolved
     */
    private Path toRealFile(Path file, Path target, Path realDirectory) throws IOException {
        if (!target.equals(file)) {
            return target;
        }

        if (realDirectory == null) {
            return file.toRealPath();
        }

        return realDirectory.resolve(file.getFileName().toString());
    }


    /**
     * Get the real path of a directory, an error is logged as warning.
     *
     * @param dir the directory
     * @return the real path or null if it can not be resolved
     */
    private Path toRealPathOrSkip(Path dir) {
        try {
            return dir.toRealPath();
        } catch (IOException e) {
            skipFile(dir, e);
            return null;
        }
    }


    /**
     * Checks if a directory is linked: a symbolic link or a Windows junction, its real path is not inside its parent.
     *
     * @param dir the directory
     * @param realDir the real path of the directory
     * @param realParent the real path of the parent directory, null for the root directory
     * @return true if the directory is linked
     */
    private boolean isLinkedDirectory(Path dir, Path realDir, Path realParent) {
        if (realParent == null) {
            return false;
        }

        return Files.isSymbolicLink(dir) || !realDir.equals(realParent.resolve(dir.getFileName().toString()));
    }


    /**
     * Read the content of a file without following symbolic links. Files larger than {@link #MAX_FILE_SIZE}
     * are not read (logged as warning).
     *
     * @param file the file
     * @return the content or null if the file is too large
     * @throws IOException In case of an IO error, e.g. if the file is a symbolic link
     */
    private byte[] readFileNoFollow(Path file) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            if (isFileTooLarge(file, channel.size())) {
                return null;
            }

            try (InputStream inputStream = Channels.newInputStream(channel)) {
                return inputStream.readAllBytes();
            }
        }
    }


    /**
     * Check if a file is too large to be read into memory. A too large file is logged as warning.
     *
     * @param file the file
     * @param size the size of the file in bytes
     * @return true if the file is larger than {@link #MAX_FILE_SIZE}
     */
    boolean isFileTooLarge(Path file, long size) {
        if (size > MAX_FILE_SIZE) {
            LOG.warn("Skip file " + file + ": the size of " + size + " bytes exceeds the maximum of " + MAX_FILE_SIZE + " bytes.");
            return true;
        }

        return false;
    }


    /**
     * Replace the content of a file in place, so that the file itself (hard links, owner, permissions) is kept.
     * Before, the original content is copied to a backup file in the temporary directory of the system. In case
     * of an error the original content is restored from the backup. The backup file is removed afterwards, only if
     * the restore fails it is kept and its path is part of the exception.
     *
     * @param file the file
     * @param content the new content
     * @throws AccessDeniedException if the file is not writable
     * @throws IOException In case of an IO error
     */
    private void replaceFileContent(Path file, byte[] content) throws IOException {
        if (!Files.isWritable(file)) {
            throw new AccessDeniedException(file.toString(), null, "file is not writable");
        }

        // the backup is created by createTempFile, which restricts the access to the owner where supported;
        // the name does not contain the file name, so that also files with a very long name can be backed up
        final Path backupFile = Files.createTempFile("toolarium-", ".bak");
        boolean keepBackup = false;
        try {
            copyFileContent(file, backupFile);

            try {
                // the file is written without following links: a file which was replaced by a link in the meantime is not written
                Files.write(file, content, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                try {
                    copyFileContent(backupFile, file);
                } catch (IOException restoreException) {
                    keepBackup = true;
                    e.addSuppressed(new IOException("Could not restore " + file + ", the original content is kept in " + backupFile + MESSAGE_SEPARATOR + restoreException.getMessage(), restoreException));
                }

                throw e;
            }
        } finally {
            if (!keepBackup) {
                deleteBackupFile(backupFile);
            }
        }
    }


    /**
     * Copy the content of a file into an existing file, the target file itself is kept (it is not replaced).
     * Links are not followed: if the source or the target is a link, the copy fails.
     *
     * @param source the source file
     * @param target the target file
     * @throws IOException In case of an IO error
     */
    private void copyFileContent(Path source, Path target) throws IOException {
        try (InputStream inputStream = Files.newInputStream(source, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
             OutputStream outputStream = Files.newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)) {
            inputStream.transferTo(outputStream);
        }
    }


    /**
     * Delete a backup file, an error is logged as warning.
     *
     * @param backupFile the backup file
     */
    private void deleteBackupFile(Path backupFile) {
        try {
            Files.deleteIfExists(backupFile);
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not delete backup file " + backupFile + MESSAGE_SEPARATOR + e.getMessage(), e);
            }

            LOG.warn("Could not delete backup file " + backupFile + MESSAGE_SEPARATOR + e.getMessage());
        }
    }


    /**
     * Skip a file which can not be read or written: a warning is logged and the walk continues.
     *
     * @param file the file
     * @param e the exception
     * @return {@link FileVisitResult#CONTINUE}
     */
    private FileVisitResult skipFile(Path file, IOException e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Skip file " + file + MESSAGE_SEPARATOR + e.getMessage(), e);
        }

        LOG.warn("Skip file " + file + MESSAGE_SEPARATOR + e.getMessage());
        return FileVisitResult.CONTINUE;
    }


    /**
     * Decode the given bytes strictly: malformed or unmappable input is not replaced.
     *
     * @param content the content to decode
     * @param charset the charset
     * @return the decoded content or null if the content can not be decoded with the given charset
     */
    private String decodeStrict(byte[] content, Charset charset) {
        try {
            return charset.newDecoder()
                          .onMalformedInput(CodingErrorAction.REPORT)
                          .onUnmappableCharacter(CodingErrorAction.REPORT)
                          .decode(ByteBuffer.wrap(content))
                          .toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }


    /**
     * Processes a file of a directory tree.
     */
    @FunctionalInterface
    private interface FileProcessor {

        /**
         * Process a file
         *
         * @param file the file as found in the directory tree
         * @param target the file to read or write: the file itself or the target of a followed symbolic link
         * @throws IOException In case of an IO error
         */
        void process(Path file, Path target) throws IOException;
    }
}
