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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.statistic.IStatisticCounter;
import com.github.toolarium.common.statistic.PercentileHistogram;
import com.github.toolarium.common.statistic.PercentileHistogramTest;
import com.github.toolarium.common.statistic.StatisticCounter;
import com.github.toolarium.common.statistic.StatisticCounterTest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private static final String LIST_START = "[";
    private static final String LIST_END = "]";
    
    /**
     * Test
     */
    @Test
    public void simpleTest() {
        assertEquals(ClassPathUtil.class.getName(), ClassPathUtil.getInstance().searchClassByName(ClassPathUtil.class.getName()).get(0));
        assertNull(ClassPathUtil.getInstance().searchClassByName("com.github.toolarium.common.util.ClassPathUtil2"));
        assertFalse(ClassPathUtil.getInstance().searchClassByName("org.slf4j.Logger").isEmpty());
        assertEquals(LIST_START + Logger.class.getName() + LIST_END, ClassPathUtil.getInstance().searchClassByName(Logger.class.getName()).toString());
    }


    /**
     * Test read package
     */
    @Test
    public void testReadFromPackage() {
        assertNull(ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.utility"));
        assertFalse(ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.util").isEmpty());
        assertEquals(statisticPackageClasses(), ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic"));
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
        assertEquals(LIST_START + StringUtil.class.getName() + LIST_END, "" + ClassPathUtil.getInstance().searchClassByName(StringUtil.class.getName()));
        assertEquals(LIST_START + ByteUtil.class.getName() + LIST_END, "" + ClassPathUtil.getInstance().searchClassByName(ByteUtil.class.getName()));
        assertEquals("[java.lang.Integer]", "" + ClassPathUtil.getInstance().searchClassByName("java.lang.Integer"));
        assertEquals("[java.lang.Integer]", "" + ClassPathUtil.getInstance().searchClassByName("Integer"));
        assertNull(ClassPathUtil.getInstance().searchClassByName("Integerf"));

        List<String> list = ClassPathUtil.getInstance().searchClassByName("Object");
        list.remove("org.omg.CORBA.Object");
        assertEquals("[java.lang.Object]", "" + list);
    }


    /**
     * The returned collections must not modify the internal state
     */
    @Test
    public void testReturnedCollectionsAreDetached() {
        List<String> list = ClassPathUtil.getInstance().searchClassByName(StringUtil.class.getName());
        list.clear();
        assertEquals(LIST_START + StringUtil.class.getName() + LIST_END, "" + ClassPathUtil.getInstance().searchClassByName(StringUtil.class.getName()));

        list = ClassPathUtil.getInstance().searchClassByName("StringUtil");
        list.clear();
        assertTrue(ClassPathUtil.getInstance().checkClassByName("StringUtil"));

        list = ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic");
        list.clear();
        assertEquals(statisticPackageClasses(), ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic"));

        Set<String> classPaths = ClassPathUtil.getInstance().getClassPaths();
        assertThrows(UnsupportedOperationException.class, () -> classPaths.clear());
    }


    /**
     * Search a class by the simple name with file extension
     */
    @Test
    public void testSearchClassBySimpleNameWithExtension() {
        assertEquals("[java.lang.Integer]", "" + ClassPathUtil.getInstance().searchClassByName("Integer.class"));
        assertTrue(ClassPathUtil.getInstance().searchClassByName("StringUtil.java").contains(StringUtil.class.getName()));
        assertTrue(ClassPathUtil.getInstance().searchClassByName("/StringUtil.class").contains(StringUtil.class.getName()));
    }


    /**
     * The class paths resolved from the class loader must not duplicate entries
     */
    @Test
    public void testClassPathsWithoutDuplicates() {
        Set<String> normalizedClassPaths = new HashSet<String>();
        for (String classPath : ClassPathUtil.getInstance().getClassPaths()) {
            String normalized = new File(classPath).getAbsoluteFile().toPath().normalize().toString();
            if (File.separatorChar == FileUtil.BACKSLASH) {
                normalized = normalized.toLowerCase(Locale.ROOT);
            }

            assertTrue(normalizedClassPaths.add(normalized), "Duplicate class path entry: " + classPath);
        }
    }

    
    /**
     * Test the searchClass
     */
    @Test
    public void testSearchClassByRegExp() {
        String pkg = "com.github.toolarium.common.statistic";
        String expected = LIST_START + pkg + ".IStatisticCounter, " + pkg + ".StatisticCounter, " + pkg + ".StatisticCounterTest, " + pkg + ".StatisticCounterTest$1" + LIST_END;
        assertEquals(expected, ClassPathUtil.getInstance().searchClassByRegExp("^com.github.toolarium.common.statistic\\.*[a-zA-Z]*Statistic.*").toString());
    }

    
    /**
     * Test the searchClassByPackageName
     */
    @Test
    public void testSearchClassByPackageName() {
        assertEquals(statisticPackageClasses(), ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic"));
        assertNull(ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.statistic2"));

        String fileNames = "[java.lang.annotation.Annotation, java.lang.annotation.AnnotationFormatError, java.lang.annotation.AnnotationTypeMismatchException, java.lang.annotation.Documented, java.lang.annotation.ElementType, "
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
        assertEquals(ClassPathUtil.getInstance().searchFileByRegExp(".*/TestResourceFile.*").toString(), LIST_START + testResourceFile + LIST_END);

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
     * Returns the sorted list of all classes in the statistic package.
     * Update this list whenever a class is added to or removed from the package.
     *
     * @return sorted class names
     */
    private List<String> statisticPackageClasses() {
        List<String> classes = Arrays.asList(
                IStatisticCounter.class.getName(),
                PercentileHistogram.class.getName(),
                PercentileHistogramTest.class.getName(),
                PercentileHistogramTest.class.getName() + "$1",
                StatisticCounter.class.getName(),
                StatisticCounterTest.class.getName(),
                StatisticCounterTest.class.getName() + "$1"
        );
        Collections.sort(classes);
        return classes;
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


    /**
     * The classes of a package are sorted
     */
    @Test
    public void testPackageClassesSorted() {
        List<String> classes = ClassPathUtil.getInstance().searchClassByPackageName("com.github.toolarium.common.util");
        assertNotNull(classes);
        List<String> sorted = new ArrayList<String>(classes);
        Collections.sort(sorted);
        assertEquals(sorted, classes);
    }

    /**
     * Classes of multi-release jars are registered with their real name
     */
    @Test
    public void testNoMultiReleaseClassNames() {
        List<String> classes = ClassPathUtil.getInstance().searchClassByRegExp("META-INF.*");
        assertTrue(classes == null || classes.isEmpty(), "Unexpected classes: " + classes);
    }


    /**
     * The entries of an archive (java.base of the jdk and the jars of the class path) are indexed with their archive url
     */
    @Test
    public void testArchiveEntriesIndexed() {
        assertEquals("[java.util.zip.ZipFile]", "" + ClassPathUtil.getInstance().searchClassByName("java.util.zip.ZipFile"));
        for (URL url : ClassPathUtil.getInstance().searchFileByRegExpAsURLList(".*/TestResourceFile.*")) {
            if ("jar".equals(url.getProtocol())) {
                assertTrue(url.toString().matches("^jar:file:.*toolarium-classpath-test[12]\\.jar!/.*/TestResourceFile\\.txt$"), url.toString());
            }
        }
    }


    /**
     * The archive name has to match the file name of a class path entry exactly, a part of a file name does not match
     */
    @Test
    public void testSearchArchiveByNameExactMatch() {
        final String entry = ClassPathUtil.getInstance().searchArchiveByRegExp("^.*.slf4j\\-api\\-.*.jar$").get(0);
        final String slashifiedEntry = FileUtil.getInstance().slashify(entry);
        final String fileName = slashifiedEntry.substring(slashifiedEntry.lastIndexOf('/') + 1);
        assertEquals(entry, ClassPathUtil.getInstance().searchArchiveByName(fileName));
        assertNull(ClassPathUtil.getInstance().searchArchiveByName(fileName.substring(1)));
        assertNull(ClassPathUtil.getInstance().searchArchiveByName(fileName.substring(0, fileName.length() - 1)));
    }


    /**
     * The class path is scanned for classes on the first call which needs the classes, not on creation
     *
     * @throws ReflectiveOperationException in case of error
     */
    @Test
    public void testLazyClassListInitialization() throws ReflectiveOperationException {
        Constructor<ClassPathUtil> constructor = ClassPathUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ClassPathUtil classPathUtil = constructor.newInstance();
        Field classesField = ClassPathUtil.class.getDeclaredField("classes");
        classesField.setAccessible(true);

        assertFalse(classPathUtil.getClassPaths().isEmpty());
        assertNull(classesField.get(classPathUtil));

        assertTrue(classPathUtil.checkClassByName(ClassPathUtil.class.getName()));
        Object classList = classesField.get(classPathUtil);
        assertNotNull(classList);
        assertTrue(classPathUtil.checkClassByName(ClassPathUtilTest.class.getName()));
        assertSame(classList, classesField.get(classPathUtil));
    }
}
