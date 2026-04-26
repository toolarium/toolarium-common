/*
 * FileUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple FileUtil test class
 * 
 * @author patrick
 */
public class FileUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtilTest.class);
    private static final String TEST = "test";

    
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
        
        String jarFileName = "D:/workspaces/private/jpTools/lib/test/jptools-classpath-test1.jar";
        assertEquals(jarFileName, FileUtil.getInstance().extractURLPath(createUrl("jar:file:/" + jarFileName + "!/jptools/util/tests/ClassPathSearchFileTest.txt")));
        assertEquals(header + "D:/test1/test2.jar", FileUtil.getInstance().extractURLPath(createUrl("jar:file:\\\\D:\\test1\\test2.jar!/META-INF")));
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
        assertEquals(35, files.size());

        files = FileUtil.getInstance().searchFiles(new File("./src/").toPath(), "", TEST);
        assertEquals(35, files.size());

        files = FileUtil.getInstance().searchFiles(new File("./src/").toPath(), "java", TEST);
        for (Path file : files) {
            LOG.debug("Search file => " + file);
        }
        assertEquals(35, files.size());
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
}
