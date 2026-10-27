/*
 * FileUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple FileUtil test class
 * 
 * @author patrick
 */
public class FileUtilTest {
    private static final String CAFE = "caf" + (char) 0xE9;
    private static final Logger LOG = LoggerFactory.getLogger(FileUtilTest.class);
    private static final String TEST = "test";
    private static final String HELLO = "hello";
    private static final String BYE = "bye";
    private static final String TREE = "tree";
    private static final String OUTSIDE = "outside";
    private static final String REAL_FILE = "real.txt";
    private static final String SECRET_FILE = "secret.txt";
    private static final String FILE_LINK = "fileLink.txt";
    private static final String DIRECTORY_LINK = "directoryLink";
    private static final String SUB = "sub";
    private static final String INNER_FILE = "inner.txt";
    private static final String INNER_FILE_LINK = "innerLink.txt";
    private static final String SUB_DIRECTORY_LINK = "subDirectoryLink";

    
    /**
     * Test slashify directory
     */
    @Test
    void testSimplifyPath() {
        assertEquals(null, FileUtil.getInstance().simplifyPath(null));
        assertEquals("", FileUtil.getInstance().simplifyPath(""));
        assertEquals("", FileUtil.getInstance().simplifyPath("      "));

        assertEquals("a:test", FileUtil.getInstance().simplifyPath("a:test"));
        assertEquals("a:test/", FileUtil.getInstance().simplifyPath("a:test/"));
        assertEquals("a:/test", FileUtil.getInstance().simplifyPath("a:/test"));
        assertEquals("a:/test/", FileUtil.getInstance().simplifyPath("a:/test/"));
        assertEquals("a:/test/name", FileUtil.getInstance().simplifyPath("a:/test/name"));
        assertEquals("a:/test/name/", FileUtil.getInstance().simplifyPath("a:/test/name/"));
        assertEquals("a:test/name/", FileUtil.getInstance().simplifyPath("a:test/name/"));
        assertEquals("a:test/name", FileUtil.getInstance().simplifyPath("a:test/name"));

        assertEquals(TEST, FileUtil.getInstance().simplifyPath(TEST));
        assertEquals("test/", FileUtil.getInstance().simplifyPath("test/"));
        assertEquals("/test", FileUtil.getInstance().simplifyPath("/test"));
        assertEquals("/test/", FileUtil.getInstance().simplifyPath("/test/"));
        assertEquals("/test/name", FileUtil.getInstance().simplifyPath("/test/name"));
        assertEquals("/test/name/", FileUtil.getInstance().simplifyPath("/test/name/"));
        assertEquals("test/name/", FileUtil.getInstance().simplifyPath("test/name/"));
        assertEquals("test/name", FileUtil.getInstance().simplifyPath("test/name"));

        assertEquals(TEST, FileUtil.getInstance().simplifyPath(TEST + "/name/.."));
        assertEquals("test/", FileUtil.getInstance().simplifyPath("test/name/../"));
        assertEquals("test/tt", FileUtil.getInstance().simplifyPath("test/name/../tt"));
        assertEquals("test/tt/yy/", FileUtil.getInstance().simplifyPath("test/name/../tt/yy/oo/../"));
        assertEquals("test/tt/", FileUtil.getInstance().simplifyPath("test/name/../tt/yy/oo/../../"));
    }

    
    /**
     * Test simplify path never leaves the root of an absolute path
     */
    @Test
    void testSimplifyPathRoot() {
        assertEquals("/etc/passwd", FileUtil.getInstance().simplifyPath("/a/../../etc/passwd"));
        assertEquals("/etc/passwd", FileUtil.getInstance().simplifyPath("\\a\\..\\..\\etc\\passwd"));
        assertEquals("/", FileUtil.getInstance().simplifyPath("/.."));
        assertEquals("/", FileUtil.getInstance().simplifyPath("/"));
        assertEquals("/a/b/", FileUtil.getInstance().simplifyPath("//a//b//"));
        assertEquals("C:/Windows", FileUtil.getInstance().simplifyPath("C:/../Windows"));
        assertEquals("C:/Windows", FileUtil.getInstance().simplifyPath("C:\\..\\..\\Windows"));
        assertEquals("C:/", FileUtil.getInstance().simplifyPath("C:/a/../.."));
        assertEquals("C:Windows", FileUtil.getInstance().simplifyPath("C:../Windows"));
        assertEquals("C:", FileUtil.getInstance().simplifyPath("C:"));

        // relative paths keep leading ".." references
        assertEquals("../a", FileUtil.getInstance().simplifyPath("../a"));
        assertEquals("../b", FileUtil.getInstance().simplifyPath("a/../../b"));
        assertEquals("a/b", FileUtil.getInstance().simplifyPath("a//b"));
        assertEquals("", FileUtil.getInstance().simplifyPath("a/../"));

        // current directory elements
        assertEquals("/etc/passwd", FileUtil.getInstance().simplifyPath("/safe/./../etc/passwd"));
        assertEquals("a/b", FileUtil.getInstance().simplifyPath("a/./b"));
        assertEquals("../secret", FileUtil.getInstance().simplifyPath("./../secret"));
        assertEquals("", FileUtil.getInstance().simplifyPath("."));
        assertEquals("/", FileUtil.getInstance().simplifyPath("/./"));

        // network path (UNC): the root //server/share/ is kept
        assertEquals("//server/share/dir", FileUtil.getInstance().simplifyPath("\\\\server\\share\\dir"));
        assertEquals("//server/share/", FileUtil.getInstance().simplifyPath("\\\\server\\share\\dir\\..\\.."));
        assertEquals("//server/share/b", FileUtil.getInstance().simplifyPath("//server/share/a/../../b"));
        assertEquals("//server/share", FileUtil.getInstance().simplifyPath("//server/share"));
        assertEquals("/server/", FileUtil.getInstance().simplifyPath("//server/"));
    }


    /**
     * Slashify unix test
     */
    @Test
    void slashifyUnix() {
        assertEquals("", FileUtil.getInstance().slashify(null));
        assertEquals("", FileUtil.getInstance().slashify(""));
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR));
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST, FileUtil.getInstance().slashify(TEST));
        assertEquals(FileUtil.SLASH_STR + TEST, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR));
    }

    
    /**
     * Slashify short path and multiple slashes in the middle
     */
    @Test
    void slashifyMultipleSlashes() {
        assertEquals("/x", FileUtil.getInstance().slashify("x"));
        assertEquals("/x/y/z", FileUtil.getInstance().slashify("/x//y/z"));
        assertEquals("/x/y/z", FileUtil.getInstance().slashify("x///y//z"));

        // the leading double slash of a network path (UNC) is kept
        assertEquals("//server/share/dir/", FileUtil.getInstance().slashify("//server/share//dir/"));
    }


    /**
     * Slashify windows test
     */
    @Test
    void slashifyWindows() {
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + TEST));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + TEST + FileUtil.BACKSLASH_STR));
        assertEquals("c:" + FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify("c:" + FileUtil.BACKSLASH_STR + TEST + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR));
    }
    

    /**
     * Test slashify directory
     */
    @Test
    void testSlashifyDirectory() {
        assertEquals(FileUtil.SLASH_STR + System.getProperty("user.dir").replace('\\', '/') + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + System.getProperty("user.dir")));
        assertEquals(FileUtil.SLASH_STR + System.getProperty("user.dir").replace('\\', '/') + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + System.getProperty("user.dir") + FileUtil.BACKSLASH_STR));
        assertEquals("C:/Users/default/.gradle/caches/x.y/workerMain/gradle-worker.jar", FileUtil.getInstance().slashify("C:\\Users\\default\\.gradle\\caches\\x.y\\workerMain\\gradle-worker.jar"));
    }
    
    
    /**
     * Test extract url path
     *
     * @throws MalformedURLException in case of error
     * @throws URISyntaxException in case of error
     */
    @Test
    public void testExtractURLPath() throws MalformedURLException, URISyntaxException {
        assertNull(FileUtil.getInstance().extractURLPath(null));
        assertEquals("/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/test1/test2.jar")));
        assertEquals("/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:///test1/test2.jar")));

        String header = "/";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("windows")) {
            header = "";
        }
        
        assertEquals(header + "D:/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/D:/test1/test2.jar!/META-INF")));
        assertEquals(header + "D:/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:\\D:\\test1\\test2.jar")));
        assertEquals(header + "D:/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:\\D:\\test1\\test2.jar!\\META-INF")));
        assertEquals("/opt/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/test1/test2.jar!/META-INF")));
        assertEquals("/opt/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/test1/test2.jar!/META-INF")));
        assertEquals("/opt/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("jar:file:/opt/test1/test2.jar!/META-INF")));
        assertEquals("/opt/Program Files/täst+1.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/Program%20Files/t%C3%A4st+1.jar")));

        // an encoded ! (%21) in a directory name is part of the path, only !/ separates the archive entry
        assertEquals("/opt/my!dir/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("jar:file:/opt/my%21dir/test2.jar!/META-INF")));
        assertEquals("/opt/my!dir/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/my!dir/test2.jar")));
        
        String jarFileName = "D:/workspaces/private/jpTools/lib/test/jptools-classpath-test1.jar";
        assertEquals(jarFileName, FileUtil.getInstance().extractURLPath(createUrl("jar:file:/" + jarFileName + "!/jptools/util/tests/ClassPathSearchFileTest.txt")));
        assertEquals(header + "D:/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("jar:file:\\\\D:\\test1\\test2.jar!/META-INF")));
    }


    /**
     * Test extract url path: a ! in a plain file url is only an archive separator after an archive file name
     *
     * @throws MalformedURLException in case of error
     * @throws URISyntaxException in case of error
     */
    @Test
    public void testExtractURLPathExclamationMark() throws MalformedURLException, URISyntaxException {
        assertEquals("/opt/x!/y.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/x!/y.jar")));
        assertEquals("/opt/x!", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/x!")));
        assertEquals("/opt/x!/y.jar", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/x!/y.jar!/META-INF")));
        assertEquals("/opt/y.ZIP", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/y.ZIP!/META-INF")));
        assertEquals("/opt/y.war", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/y.war!")));
        assertEquals("/opt/y.ear", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/y.ear!/a")));
        assertEquals("/opt/y.jmod", FileUtil.getInstance().extractURLPath(createUrl("file:/opt/y.jmod!/a")));
        assertEquals("/opt/y.txt", FileUtil.getInstance().extractURLPath(createUrl("jar:file:/opt/y.txt!/META-INF")));
    }


    /**
     * Test extract url path: the host of a UNC file url is kept
     *
     * @throws MalformedURLException in case of error
     * @throws URISyntaxException in case of error
     */
    @Test
    public void testExtractURLPathUNC() throws MalformedURLException, URISyntaxException {
        assertEquals("//server/share/x.jar", FileUtil.getInstance().extractURLPath(createUrl("file://server/share/x.jar")));
        assertEquals("//server/share/x.jar", FileUtil.getInstance().extractURLPath(createUrl("jar:file://server/share/x.jar!/META-INF")));
        assertEquals("/share/x.jar", FileUtil.getInstance().extractURLPath(createUrl("file://localhost/share/x.jar")));
    }


    /**
     * Search files test
     *
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchFiles() throws IOException {
        List<Path> files = FileUtil.getInstance().searchFiles(new File("./src/").toPath(), TEST);
        for (Path file : files) {
            LOG.debug("Search file => " + file);
        }
        assertEquals(49, files.size());

        files = FileUtil.getInstance().searchFiles(new File("./src/").toPath(), "", TEST);
        assertEquals(49, files.size());

        files = FileUtil.getInstance().searchFiles(new File("./src/").toPath(), "java", TEST);
        for (Path file : files) {
            LOG.debug("Search file => " + file);
        }
        assertEquals(49, files.size());
    }


    /**
     * Remove directory must not follow symbolic links
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void removeDirectoryDoesNotFollowSymbolicLinks(@TempDir Path tempDir) throws IOException {
        Path target = Files.createDirectories(tempDir.resolve("target"));
        final Path targetFile = Files.write(target.resolve("keep.txt"), "keep".getBytes(StandardCharsets.UTF_8));
        Path tree = Files.createDirectories(tempDir.resolve("tree"));
        Files.write(tree.resolve("file.txt"), "delete".getBytes(StandardCharsets.UTF_8));

        Path link = tree.resolve("link");
        try {
            Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Assumptions.assumeTrue(false, "Symbolic links are not supported: " + e.getMessage());
        }

        assertTrue(FileUtil.getInstance().removeDirectory(tree));
        assertFalse(Files.exists(tree));
        assertTrue(Files.exists(targetFile));
        assertEquals("keep", new String(Files.readAllBytes(targetFile), StandardCharsets.UTF_8));
    }

    
    /**
     * Search and replace must not modify non UTF-8 files and inserts the replacement literally
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchAndReplaceFiles(@TempDir Path tempDir) throws IOException {
        Path utf8File = Files.write(tempDir.resolve("utf8.txt"), (CAFE + " hello world").getBytes(StandardCharsets.UTF_8));
        byte[] latin1Content = (CAFE + " hello world").getBytes(StandardCharsets.ISO_8859_1);
        final Path latin1File = Files.write(tempDir.resolve("latin1.txt"), latin1Content);
        byte[] binaryContent = new byte[] {(byte) 0xFF, (byte) 0xFE, 0x00, 'h', 'e', 'l', 'l', 'o', (byte) 0xC3};
        final Path binaryFile = Files.write(tempDir.resolve("binary.bin"), binaryContent);

        List<Path> changedFiles = FileUtil.getInstance().searchAndReplaceFiles(tempDir, "hello", "$HOME\\1");
        assertEquals(1, changedFiles.size());
        assertEquals(utf8File, changedFiles.get(0));
        assertEquals(CAFE + " $HOME\\1 world", new String(Files.readAllBytes(utf8File), StandardCharsets.UTF_8));
        assertArrayEquals(latin1Content, Files.readAllBytes(latin1File));
        assertArrayEquals(binaryContent, Files.readAllBytes(binaryFile));
    }

    
    /**
     * Create URL 
     *
     * @param input the string input
     * @return the URL
     * @throws MalformedURLException in case of error
     * @throws URISyntaxException in case of error
     */
    private URL createUrl(String input) throws MalformedURLException, URISyntaxException {
        return new URI(input.replace(FileUtil.BACKSLASH_STR, FileUtil.SLASH_STR)).toURL();
        //return new URL(input);
    }


    /**
     * Read file content with null input
     *
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void readFileContentNull() throws IOException {
        assertNull(FileUtil.getInstance().readFileContent((File) null));
        assertNull(FileUtil.getInstance().readFileContent((Path) null));
        assertNull(FileUtil.getInstance().readFileContent((Path) null, StandardCharsets.UTF_8));
        assertNull(FileUtil.getInstance().readFileContent((URL) null));
    }


    /**
     * Searching in a missing root directory throws a {@link NoSuchFileException} instead of returning an empty list (L05)
     *
     * @param tempDir the temp directory
     */
    @Test
    public void searchFilesMissingRootDirectory(@TempDir Path tempDir) {
        final Path missing = tempDir.resolve(OUTSIDE);
        assertThrows(NoSuchFileException.class, () -> FileUtil.getInstance().searchFiles(missing, HELLO));
        assertThrows(NoSuchFileException.class, () -> FileUtil.getInstance().searchFiles(missing, null, HELLO, LinkFollowMode.NONE));
        assertThrows(NoSuchFileException.class, () -> FileUtil.getInstance().searchAndReplaceFiles(missing, HELLO, BYE));
    }


    /**
     * Removing a directory with content fails immediately, without the delayed retry (L08)
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void removeFileOfDirectoryWithContentIsNotRetried(@TempDir Path tempDir) throws IOException {
        Path dir = Files.createDirectories(tempDir.resolve(TREE));
        Files.write(dir.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));

        final long start = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            assertFalse(FileUtil.getInstance().removeFile(dir.toFile()));
        }

        // the delayed retry would take at least 5 x 100 ms
        assertTrue(System.currentTimeMillis() - start < 400L);
        assertTrue(Files.exists(dir.resolve(REAL_FILE)));
    }


    /**
     * Remove a file which does not exist
     *
     * @param tempDir the temp directory
     */
    @Test
    public void removeNotExistingFile(@TempDir Path tempDir) {
        assertFalse(FileUtil.getInstance().removeFile(tempDir.resolve("missing.txt").toFile()));
    }


    /**
     * Remove a file with a name which is not valid on every platform (e.g. on Windows) returns false
     */
    @Test
    public void removeFileWithInvalidName() {
        assertFalse(FileUtil.getInstance().removeFile("a*b"));
    }


    /**
     * Reading by url uses UTF-8
     *
     * @param tempDir the temp directory
     * @throws IOException in case of error
     */
    @Test
    public void readFileContentByUrlIsUtf8(@TempDir Path tempDir) throws IOException {
        Path file = Files.write(tempDir.resolve("url.txt"), CAFE.getBytes(StandardCharsets.UTF_8));
        assertEquals(CAFE, FileUtil.getInstance().readFileContent(file.toUri().toURL()));
    }


    /**
     * Removing a null directory returns false
     */
    @Test
    public void removeDirectoryNull() {
        assertFalse(FileUtil.getInstance().removeDirectory((Path) null));
        assertFalse(FileUtil.getInstance().removeDirectory((File) null));
    }


    /**
     * Remove a nested directory tree, a plain file and a not existing directory
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void removeDirectoryNested(@TempDir Path tempDir) throws IOException {
        Path tree = Files.createDirectories(tempDir.resolve(TREE).resolve("a").resolve("b"));
        Files.write(tree.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(tempDir.resolve(TREE).resolve("empty"));
        final Path plainFile = Files.write(tempDir.resolve(SECRET_FILE), HELLO.getBytes(StandardCharsets.UTF_8));

        assertTrue(FileUtil.getInstance().removeDirectory(tempDir.resolve(TREE)));
        assertFalse(Files.exists(tempDir.resolve(TREE)));
        assertTrue(FileUtil.getInstance().removeDirectory(plainFile));
        assertFalse(Files.exists(plainFile));
        assertFalse(FileUtil.getInstance().removeDirectory(tempDir.resolve("missing")));
    }


    /**
     * Remove directory with a symbolic link as root: only the link is removed, never the content of its target
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void removeDirectoryRootIsSymbolicLink(@TempDir Path tempDir) throws IOException {
        Path outside = Files.createDirectories(tempDir.resolve(OUTSIDE));
        final Path secretFile = Files.write(outside.resolve(SECRET_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Path link = createSymbolicLinkOrSkip(tempDir.resolve("rootLink"), outside);

        assertTrue(FileUtil.getInstance().removeDirectory(link.toFile()));
        assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
        assertEquals(HELLO, new String(Files.readAllBytes(secretFile), StandardCharsets.UTF_8));
    }


    /**
     * Search files with the different link follow modes
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchFilesLinkFollowMode(@TempDir Path tempDir) throws IOException {
        Path tree = createLinkTree(tempDir);
        final Path realFile = tree.resolve(REAL_FILE);
        final Path innerFile = tree.resolve(SUB).resolve(INNER_FILE);
        final Path fileLink = tree.resolve(FILE_LINK);
        final Path innerFileLink = tree.resolve(INNER_FILE_LINK);
        final Path subDirectoryLinkFile = tree.resolve(SUB_DIRECTORY_LINK).resolve(INNER_FILE);
        final Path directoryLinkFile = tree.resolve(DIRECTORY_LINK).resolve(SECRET_FILE);
        FileUtil fileUtil = FileUtil.getInstance();

        // a file which is reachable by several paths is found only once (first path)
        final Set<Path> innerPaths = Set.of(innerFile, innerFileLink, subDirectoryLinkFile);
        final Set<Path> secretPaths = Set.of(fileLink, directoryLinkFile);

        // without a mode all links are followed
        assertFoundOnce(fileUtil.searchFiles(tree, HELLO), realFile, innerPaths, secretPaths);
        assertEquals(Set.of(realFile, innerFile), Set.copyOf(fileUtil.searchFiles(tree, null, HELLO, LinkFollowMode.NONE)));
        assertEquals(Set.of(realFile, innerFile), Set.copyOf(fileUtil.searchFiles(tree, null, HELLO, null)));
        assertFoundOnce(fileUtil.searchFiles(tree, null, HELLO, LinkFollowMode.WITHIN_ROOT), realFile, innerPaths);
        assertFoundOnce(fileUtil.searchFiles(tree, null, HELLO, LinkFollowMode.ALL), realFile, innerPaths, secretPaths);
    }


    /**
     * Assert that the found files contain the given file and exactly one path of each group of paths of the same file
     *
     * @param foundFiles the found files
     * @param file the file which must be found
     * @param samePaths the groups of paths which point to the same file
     */
    @SafeVarargs
    private static void assertFoundOnce(List<Path> foundFiles, Path file, Set<Path>... samePaths) {
        assertEquals(1 + samePaths.length, foundFiles.size(), foundFiles.toString());
        assertTrue(foundFiles.contains(file));
        for (Set<Path> paths : samePaths) {
            assertEquals(1, foundFiles.stream().filter(paths::contains).count(), foundFiles.toString());
        }
    }


    /**
     * Search and replace with the different link follow modes: a followed link modifies the target, the link itself is kept
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchAndReplaceFilesLinkFollowMode(@TempDir Path tempDir) throws IOException {
        Path tree = createLinkTree(tempDir);
        final Path secretFile = tempDir.resolve(OUTSIDE).resolve(SECRET_FILE);
        final Path fileLink = tree.resolve(FILE_LINK);

        FileUtil.getInstance().searchAndReplaceFiles(tree, HELLO, BYE, LinkFollowMode.NONE);
        assertEquals(BYE, readString(tree.resolve(REAL_FILE)));
        assertEquals(HELLO, readString(secretFile));

        FileUtil.getInstance().searchAndReplaceFiles(tree, BYE, HELLO, LinkFollowMode.WITHIN_ROOT);
        assertEquals(HELLO, readString(tree.resolve(REAL_FILE)));
        assertEquals(HELLO, readString(secretFile));

        // without a mode all links are followed: the target of a file link is modified, also outside of the tree
        final List<Path> changedFiles = FileUtil.getInstance().searchAndReplaceFiles(tree, HELLO, BYE);
        assertTrue(changedFiles.contains(fileLink) || changedFiles.contains(tree.resolve(DIRECTORY_LINK).resolve(SECRET_FILE)));
        assertEquals(BYE, readString(secretFile));
        assertTrue(Files.isSymbolicLink(fileLink));
    }


    /**
     * Create a directory tree with symbolic links:
     * <pre>
     * outside/secret.txt
     * tree/real.txt
     * tree/sub/inner.txt
     * tree/fileLink.txt         -&gt; outside/secret.txt
     * tree/innerLink.txt        -&gt; tree/sub/inner.txt
     * tree/directoryLink        -&gt; outside
     * tree/subDirectoryLink     -&gt; tree/sub
     * </pre>
     * The test is skipped if symbolic links are not supported.
     *
     * @param tempDir the temp directory
     * @return the tree directory
     * @throws IOException in case of an I/O exception
     */
    private Path createLinkTree(Path tempDir) throws IOException {
        Path outside = Files.createDirectories(tempDir.resolve(OUTSIDE));
        final Path secretFile = Files.write(outside.resolve(SECRET_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Path tree = Files.createDirectories(tempDir.resolve(TREE));
        Files.write(tree.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Path sub = Files.createDirectories(tree.resolve(SUB));
        final Path innerFile = Files.write(sub.resolve(INNER_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        createSymbolicLinkOrSkip(tree.resolve(FILE_LINK), secretFile);
        createSymbolicLinkOrSkip(tree.resolve(INNER_FILE_LINK), innerFile);
        createSymbolicLinkOrSkip(tree.resolve(DIRECTORY_LINK), outside);
        createSymbolicLinkOrSkip(tree.resolve(SUB_DIRECTORY_LINK), sub);
        return tree;
    }


    /**
     * Read a file as UTF-8 string
     *
     * @param file the file
     * @return the content
     * @throws IOException in case of an I/O exception
     */
    private String readString(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }


    /**
     * Search and replace replaces the file content in place without leaving a file behind and keeps the POSIX permissions
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchAndReplaceFilesLeavesNoTemporaryFile(@TempDir Path tempDir) throws IOException {
        Path realFile = Files.write(tempDir.resolve(REAL_FILE), (HELLO + " world").getBytes(StandardCharsets.UTF_8));
        Set<PosixFilePermission> permissions = null;
        if (Files.getFileAttributeView(realFile, PosixFileAttributeView.class) != null) {
            permissions = PosixFilePermissions.fromString("rw-r-----");
            Files.setPosixFilePermissions(realFile, permissions);
        }

        List<Path> changedFiles = FileUtil.getInstance().searchAndReplaceFiles(tempDir, HELLO, BYE);
        assertEquals(List.of(realFile), changedFiles);
        assertEquals(BYE + " world", new String(Files.readAllBytes(realFile), StandardCharsets.UTF_8));
        try (Stream<Path> stream = Files.list(tempDir)) {
            assertEquals(List.of(realFile), stream.collect(Collectors.toList()));
        }

        if (permissions != null) {
            assertEquals(permissions, Files.getPosixFilePermissions(realFile));
        }
    }


    /**
     * Search and replace also replaces a file with a very long name (M03)
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchAndReplaceFilesWithLongFileName(@TempDir Path tempDir) throws IOException {
        Path file;
        try {
            file = Files.write(tempDir.resolve("x".repeat(240) + ".txt"), HELLO.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | java.nio.file.InvalidPathException e) {
            Assumptions.abort("Long file names are not supported: " + e.getMessage());
            return;
        }

        assertEquals(List.of(file), FileUtil.getInstance().searchAndReplaceFiles(tempDir, HELLO, BYE));
        assertEquals(BYE, readString(file));
    }


    /**
     * Search and replace writes the file in place: a hard link to the file sees the new content
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchAndReplaceFilesKeepsHardLink(@TempDir Path tempDir) throws IOException {
        Path tree = Files.createDirectories(tempDir.resolve(TREE));
        Path realFile = Files.write(tree.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Path hardLink = tempDir.resolve(FILE_LINK);
        try {
            Files.createLink(hardLink, realFile);
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.abort("Hard links are not supported: " + e.getMessage());
        }

        assertEquals(List.of(realFile), FileUtil.getInstance().searchAndReplaceFiles(tree, HELLO, BYE));
        assertEquals(BYE, readString(realFile));
        assertEquals(BYE, readString(hardLink));
    }


    /**
     * Search and replace skips a read only file and keeps it untouched
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     */
    @Test
    public void searchAndReplaceFilesSkipsReadOnlyFile(@TempDir Path tempDir) throws IOException {
        Path realFile = Files.write(tempDir.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        File file = realFile.toFile();
        Assumptions.assumeTrue(file.setWritable(false), "Could not set the file read only");
        try {
            Assumptions.assumeFalse(Files.isWritable(realFile), "Read only file is still writable (e.g. running as root)");

            List<Path> changedFiles = FileUtil.getInstance().searchAndReplaceFiles(tempDir, HELLO, BYE);
            assertTrue(changedFiles.isEmpty());
            assertEquals(HELLO, new String(Files.readAllBytes(realFile), StandardCharsets.UTF_8));
            try (Stream<Path> stream = Files.list(tempDir)) {
                assertEquals(List.of(realFile), stream.collect(Collectors.toList()));
            }
        } finally {
            file.setWritable(true);
        }
    }


    /**
     * Files which are too large to be read into memory are detected
     *
     * @param tempDir the temp directory
     */
    @Test
    public void isFileTooLarge(@TempDir Path tempDir) {
        Path file = tempDir.resolve(REAL_FILE);
        assertFalse(FileUtil.getInstance().isFileTooLarge(file, 0));
        assertFalse(FileUtil.getInstance().isFileTooLarge(file, FileUtil.MAX_FILE_SIZE));
        assertTrue(FileUtil.getInstance().isFileTooLarge(file, FileUtil.MAX_FILE_SIZE + 1));
        assertTrue(FileUtil.getInstance().isFileTooLarge(file, Integer.MAX_VALUE));
        assertTrue(FileUtil.getInstance().isFileTooLarge(file, Long.MAX_VALUE));
    }


    /**
     * A Windows junction is handled like a symbolic link to a directory (H01)
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     * @throws InterruptedException in case the junction command is interrupted
     */
    @Test
    public void searchFilesJunctionLinkFollowMode(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path outside = Files.createDirectories(tempDir.resolve(OUTSIDE));
        Files.write(outside.resolve(SECRET_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Path tree = Files.createDirectories(tempDir.resolve(TREE));
        Path realFile = Files.write(tree.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        Path junction = createJunctionOrSkip(tree.resolve(DIRECTORY_LINK), outside);
        Path junctionFile = junction.resolve(SECRET_FILE);
        FileUtil fileUtil = FileUtil.getInstance();

        assertEquals(Set.of(realFile), Set.copyOf(fileUtil.searchFiles(tree, null, HELLO, LinkFollowMode.NONE)));
        assertEquals(Set.of(realFile), Set.copyOf(fileUtil.searchFiles(tree, null, HELLO, LinkFollowMode.WITHIN_ROOT)));
        assertEquals(Set.of(realFile, junctionFile), Set.copyOf(fileUtil.searchFiles(tree, null, HELLO, LinkFollowMode.ALL)));

        fileUtil.searchAndReplaceFiles(tree, HELLO, BYE, LinkFollowMode.NONE);
        assertEquals(BYE, readString(realFile));
        assertEquals(HELLO, readString(outside.resolve(SECRET_FILE)));
        Files.delete(junction);
    }


    /**
     * A file which is reachable by two paths (a junction inside the root) is replaced only once (M01)
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     * @throws InterruptedException in case the junction command is interrupted
     */
    @Test
    public void searchAndReplaceFilesReplacesOnlyOnce(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path tree = Files.createDirectories(tempDir.resolve(TREE));
        Path sub = Files.createDirectories(tree.resolve(SUB));
        Path innerFile = Files.write(sub.resolve(INNER_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        final Path junction = createJunctionOrSkip(tree.resolve(SUB_DIRECTORY_LINK), sub);

        List<Path> changedFiles = FileUtil.getInstance().searchAndReplaceFiles(tree, HELLO, HELLO + HELLO, LinkFollowMode.ALL);
        assertEquals(1, changedFiles.size(), changedFiles.toString());
        assertEquals(HELLO + HELLO, readString(innerFile));
        assertEquals(1, FileUtil.getInstance().searchFiles(tree, null, HELLO, LinkFollowMode.ALL).size());
        Files.delete(junction);
    }


    /**
     * A directory with a junction whose target was deleted is removed completely, the junction itself is removed (M02)
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O exception
     * @throws InterruptedException in case the junction command is interrupted
     */
    @Test
    public void removeDirectoryWithDanglingJunction(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path target = Files.createDirectories(tempDir.resolve(OUTSIDE));
        Path dir = Files.createDirectories(tempDir.resolve(TREE));
        Files.write(dir.resolve(REAL_FILE), HELLO.getBytes(StandardCharsets.UTF_8));
        createJunctionOrSkip(dir.resolve(DIRECTORY_LINK), target);
        Files.delete(target);

        assertTrue(FileUtil.getInstance().removeDirectory(dir));
        assertFalse(Files.exists(dir, LinkOption.NOFOLLOW_LINKS));
    }


    /**
     * Create a Windows junction or skip the test if it is not Windows or the junction can not be created
     *
     * @param junction the junction to create
     * @param target the target directory
     * @return the created junction
     * @throws IOException in case of an I/O exception
     * @throws InterruptedException in case the command is interrupted
     */
    private Path createJunctionOrSkip(Path junction, Path target) throws IOException, InterruptedException {
        Assumptions.assumeTrue(File.separatorChar == '\\', "Junctions are only supported on Windows");
        Process process = new ProcessBuilder("cmd", "/c", "mklink", "/J", junction.toString(), target.toString()).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        Assumptions.assumeTrue(process.waitFor() == 0 && Files.isDirectory(junction), "Junction could not be created");
        return junction;
    }


    /**
     * Create a symbolic link or skip the test in case symbolic links are not supported or not permitted
     *
     * @param link the link to create
     * @param target the target of the link
     * @return the created link
     */
    private Path createSymbolicLinkOrSkip(Path link, Path target) {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Assumptions.assumeTrue(false, "Symbolic links are not supported: " + e.getMessage());
            return null;
        }
    }
}
