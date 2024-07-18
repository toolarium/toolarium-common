/*
 * ClassPathUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the ClassPathUtil.
 * 
 * @author patrick
 */
public class ClassPathUtilTest {
    private static final Logger LOG = LoggerFactory.getLogger(ClassPathUtilTest.class);
    
    /**
     * Test
     */
    @Test
    public void simpleTest() {
        assertEquals(ClassPathUtil.class.getName(), ClassPathUtil.getInstance().searchClassByName(ClassPathUtil.class.getName()).get(0));
        assertNull(ClassPathUtil.getInstance().searchClassByName("com.github.toolarium.common.util.ClassPathUtil2"));
        assertFalse(ClassPathUtil.getInstance().searchClassByName("org.slf4j.Logger").isEmpty());
        assertEquals("[" + Logger.class.getName() + "]", ClassPathUtil.getInstance().searchClassByName(Logger.class.getName()).toString());
    }


    /**
     * Test read package 
     */
    @Test
    public void testReadFromPackage() {
        assertNull(ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.utility"));
        assertFalse(ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.util").isEmpty());
        assertEquals("[com.github.toolarium.common.statistic.IStatisticCounter, com.github.toolarium.common.statistic.StatisticCounter, com.github.toolarium.common.statistic.StatisticCounterTest]", 
                     ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic").toString());
    }


    /**
     * Test
     * @throws Exception in case of error
     */
    @Test
    public void testVersion() throws Exception {
        assertNotNull(ClassPathUtil.getInstance().getClassPaths());
    }

    
    /**
     * Test the searchClass
     */
    @Test
    public void testSearchClass() {
        assertTrue(ClassPathUtil.getInstance().checkClassByName(StringUtil.class.getName()));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("StringUtil"));
        assertEquals("[" + StringUtil.class.getName() + "]", "" + ClassPathUtil.getInstance().searchClassByName(StringUtil.class.getName()));
        assertEquals("[" + ByteUtil.class.getName() + "]", "" + ClassPathUtil.getInstance().searchClassByName(ByteUtil.class.getName()));
        assertEquals("[java.lang.Integer]", "" + ClassPathUtil.getInstance().searchClassByName("java.lang.Integer"));
        assertEquals("[java.lang.Integer]", "" + ClassPathUtil.getInstance().searchClassByName("Integer"));
        assertNull(ClassPathUtil.getInstance().searchClassByName("Integerf"));

        List<String> list = ClassPathUtil.getInstance().searchClassByName("Object");
        list.remove("org.omg.CORBA.Object");
        assertEquals("[java.lang.Object]", "" + ClassPathUtil.getInstance().searchClassByName("Object"));
    }

    
    /**
     * Test the searchClass
     */
    @Test
    public void testSearchClassByRegExp() {
        assertEquals("[com.github.toolarium.common.statistic.IStatisticCounter, com.github.toolarium.common.statistic.StatisticCounter, com.github.toolarium.common.statistic.StatisticCounterTest]",
                      ClassPathUtil.getInstance().searchClassByRegExp("^com.github.toolarium.common.statistic\\.*[a-zA-Z]*Statistic.*").toString());
    }

    
    /**
     * Test the searchClassByPackageName
     */
    @Test
    public void testSearchClassByPackageName() {
        String fileNames = "[com.github.toolarium.common.statistic.IStatisticCounter, com.github.toolarium.common.statistic.StatisticCounter, com.github.toolarium.common.statistic.StatisticCounterTest]";
        String classPathResult = "" + ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic");
        assertEquals(fileNames, classPathResult);
        assertNull(ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic2"));

        fileNames = "[java.lang.annotation.Annotation, java.lang.annotation.AnnotationFormatError, java.lang.annotation.AnnotationTypeMismatchException, java.lang.annotation.Documented, java.lang.annotation.ElementType, "
                  + "java.lang.annotation.IncompleteAnnotationException, java.lang.annotation.Inherited, java.lang.annotation.Native, java.lang.annotation.Repeatable, java.lang.annotation.Retention, java.lang.annotation.RetentionPolicy, "
                  + "java.lang.annotation.Target]";

        List<String> list = ClassPathUtil.getInstance().searchClassByPackageName("java.lang.annotation");
        Collections.sort(list);
        assertEquals(fileNames, "" + list);
    }

    
    /**
     * Test the searchClass
     */
    @Test
    public void testSearchJar() {
        assertNotNull(ClassPathUtil.getInstance().searchArchiveByRegExp("toolarium-classpath-test1.jar"));
        assertNotNull(ClassPathUtil.getInstance().searchArchiveByRegExp("toolarium-classpath-test2.jar"));
        assertNull(ClassPathUtil.getInstance().searchArchiveByName("toolarium-classpath-test3.jar"));
        String file = FileUtil.getInstance().slashify(ClassPathUtil.getInstance().searchArchiveByRegExp("^.*.slf4j\\-api\\-.*.jar$").get(0));
        assertNotNull(file);
    }

    
    /**
     * Test the searchClass
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testSearchFileByRegExp() throws IOException {
        final String testResourceFile = "com/github/toolarium/common/util/TestResourceFile.txt";
        assertNotNull(ClassPathUtil.getInstance().searchFileByRegExp(".*/TestResourceFile.*"));
        assertEquals(ClassPathUtil.getInstance().searchFileByRegExp(".*/TestResourceFile.*").toString(), "[" + testResourceFile + "]");

        List<URL> urlList = new ArrayList<URL>();
        for (URL url : ClassPathUtil.getInstance().searchFileByRegExpAsURLList(".*/TestResourceFile.*")) {
            LOG.debug("URL " + url);
            if (!url.toString().contains("/bin/test/")) {
                urlList.add(url);
            }
        }
        
        int idx = 0;
        LOG.debug("Load file [" + idx + "]: " + urlList.get(idx));
        assertEquals(FileUtil.getInstance().readFileContent(urlList.get(idx)), "Test");
        idx++;
        LOG.debug("Load file [" + idx + "]: " + urlList.get(idx));
        assertEquals(FileUtil.getInstance().readFileContent(urlList.get(idx)), "Test 1");
        idx++;
        LOG.debug("Load file [" + idx + "]: " + urlList.get(idx));
        assertEquals(FileUtil.getInstance().readFileContent(urlList.get(idx)), "Test 2");
        
        String workingPath = FileUtil.getInstance().slashify(getWorkingPath());
        String classPath = getClasspath(workingPath, true, false).get(0);
        if (!classPath.startsWith(FileUtil.SLASH_STR)) {
            classPath = FileUtil.SLASH_STR + classPath;
        }

        if (!workingPath.startsWith(FileUtil.SLASH_STR)) {
            workingPath = FileUtil.SLASH_STR + workingPath;
        }

        if (classPath.indexOf("/bin/test/") > 0) {
            classPath = classPath.replace("/bin/test/", "/src/test/java/");
        }
        if (classPath.indexOf("/build/classes/java/test/") > 0) {
            classPath = classPath.replace("/build/classes/java/test/", "/src/test/java/");
        }
        
        assertEquals(urlList.toString(),
                "[file:" + classPath + testResourceFile + ", " 
                        + "jar:file:" + workingPath + "lib-test/toolarium-classpath-test1.jar!/" + testResourceFile + ", " 
                        + "jar:file:" + workingPath + "lib-test/toolarium-classpath-test2.jar!/" + testResourceFile + "]");
    }

    
    /**
     * Test simple qualified
     */
    @Test
    public void testSimpleQualified() {
        ClassPathUtil s = ClassPathUtil.getInstance();
        assertTrue(s.checkClassByName("ByteArray"));

        List<String> list = new ArrayList<String>();
        for (String entity : s.searchClassByName("ByteArray")) {
            if (!entity.startsWith("jdk.internal.")) {
                list.add(entity);
            }
        }

        assertEquals("[com.github.toolarium.common.ByteArray]", list.toString());
    }

    /**
     * Test full qualified
     */
    @Test
    public void testFullQualified() {
        assertTrue(ClassPathUtil.getInstance().checkClassByName("com/github/toolarium/common/ByteArray.java"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("com/github/toolarium/common/ByteArray"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("com.github.toolarium.common.ByteArray"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("com.github.toolarium.common.ByteArray.java"));
        assertEquals("[com.github.toolarium.common.ByteArray]", ClassPathUtil.getInstance().searchClassByName("com.github.toolarium.common.ByteArray.java").toString());

        assertTrue(ClassPathUtil.getInstance().checkClassByName("/com/github/toolarium/common/ByteArray.java"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("/com/github/toolarium/common/ByteArray"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("/com.github.toolarium.common.ByteArray"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName("/com.github.toolarium.common.ByteArray.java"));

        assertTrue(ClassPathUtil.getInstance().checkClassByName(".com/github/toolarium/common/ByteArray.java"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName(".com/github/toolarium/common/ByteArray"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName(".com.github.toolarium.common.ByteArray"));
        assertTrue(ClassPathUtil.getInstance().checkClassByName(".com.github.toolarium.common.ByteArray.java"));
    }

    
    /**
     * Invalid
     */
    @Test
    public void testInvalildSourceFile() {
        assertFalse(ClassPathUtil.getInstance().checkClassByName(null));
        assertFalse(ClassPathUtil.getInstance().checkClassByName(""));
        assertFalse(ClassPathUtil.getInstance().checkClassByName("com/github/toolarium/common/util/ByteArray"));
    }

    
    /**
     * Get the working path
     *
     * @return the working path
     */
    private String getWorkingPath() {
        return FileUtil.getInstance().slashify(System.getProperty("user.dir"));
    }

    
    /**
     * Get the classpath
     *
     * @param filter the filter
     * @param useTest true to use test path
     * @param includeResources true to include resource path
     * @return the classpath
     */
    private List<String> getClasspath(String filter, boolean useTest, boolean includeResources) {
        List<String> classPathList = new ArrayList<String>();
        String selectPath = "main";
        String ignorePath = "test";
        String ignoreResourcePath = "resources/test";
        
        if (useTest) {
            selectPath = "test";
            ignorePath = "main";
        }
        
        if (includeResources) {
            ignoreResourcePath = ignorePath;
        }

        String[] classPathEntries = System.getProperties().getProperty("java.class.path").split(System.getProperty("path.separator"));
        for (String s : classPathEntries) {
            String p = s.replace("\\\\", FileUtil.SLASH_STR).replace("\\", FileUtil.SLASH_STR);
            if (filter == null || filter.isEmpty() 
                    || (classPathList.isEmpty() && p.startsWith(filter) && p.endsWith(FileUtil.SLASH_STR + selectPath))
                    || (!classPathList.isEmpty() && p.startsWith(filter) && !p.endsWith(FileUtil.SLASH_STR + ignorePath) && !p.endsWith(FileUtil.SLASH_STR + ignoreResourcePath))) {
                classPathList.add(FileUtil.getInstance().slashify(p));
            }
        }

        return classPathList;
    }
}
