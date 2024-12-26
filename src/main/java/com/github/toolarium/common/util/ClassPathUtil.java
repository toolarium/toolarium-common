/*
 * ClassPathUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class path util
 * 
 * @author patrick
 */
public final class ClassPathUtil {
    private static final String JAVA_LANG = "java.lang";
    private static final Logger LOG = LoggerFactory.getLogger(ClassPathUtil.class);
    private final char[] fileSeparators;
    private final Map<String, String> classPaths;
    private final ClassList classes;
    private Map<String, String> jarCache;
    

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final ClassPathUtil INSTANCE = new ClassPathUtil();
    }

    
    /**
     * Constructor
     */
    private ClassPathUtil() {
        fileSeparators = new char[] {FileUtil.SLASH, FileUtil.BACKSLASH, (char) System.getProperty("file.separator").getBytes()[0]};
        classPaths = initClassPaths();
        classes = initClassList();
        jarCache = null;
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ClassPathUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Search an archive
     *
     * @param archiveName the archive to search
     * @return the full archive path ot null if it does not exist
     */
    public String searchArchiveByName(String archiveName) {
        String result = null;
        if (jarCache == null) {
            jarCache = new HashMap<String, String>();
        } else {
            result = jarCache.get(archiveName);
        }
        
        if (result != null) {
            return result;
        }

        // search
        Set<String> c = getClassPaths();
        for (Iterator<String> it = c.iterator(); it.hasNext();) {
            result = it.next();
            if (result.indexOf(archiveName) > -1) {
                jarCache.put(archiveName, result);
                return result;
            }
        }

        return null;
    }

    
    /**
     * Search archive by regular expression
     *
     * @param regExp the search regular expression
     * @return a list with full qualified class names or null if it does not exist
     */
    public List<String> searchArchiveByRegExp(String regExp) {
        if (regExp == null || regExp.trim().length() == 0) {
            return null;
        }

        Pattern pattern = Pattern.compile(regExp.trim());
        List<String> resolvedArchoves = new ArrayList<String>();
        for (String name : getClassPaths()) {
            if (pattern.matcher(name).matches()) {
                resolvedArchoves.add(name);
            }
        }

        return resolvedArchoves;

    }

    
    /**
     * Search a specific class
     *
     * @param className the name of the class with or without package name
     * @return a list with full qualified class names or null if it does not
     *         exist
     */
    public List<String> searchClassByName(String className) {
        return classes.getClasses(className);
    }

    
    /**
     * Search class by regular expression
     *
     * @param regExp the search regular expression
     * @return a list with full qualified class names or null if it does not
     *         exist
     */
    public List<String> searchClassByRegExp(String regExp) {
        return classes.searchClasses(regExp);
    }

    
    /**
     * Search classes from a specific package
     *
     * @param packageName the name of the package
     * @return a list with full qualified class names or null if it does not
     *         exist
     */
    public List<String> searchClassByPackageName(String packageName) {
        return classes.getClassesFromPackage(packageName);
    }

    
    /**
     * Check if a given class is in the class path
     * 
     * @param className the name of the class with or without package name
     * @return true if the class exists; otherwise false
     */
    public boolean checkClassByName(String className) {
        List<String> l = searchClassByName(className);
        if (l == null || l.isEmpty()) {
            return false;
        }
        
        return true;
    }

    
    /**
     * Search file by regular expression
     *
     * @param regExp the search regular expression
     * @return a list with full qualified class names or null if it does not
     *         exist
     */
    public List<String> searchFileByRegExp(String regExp) {
        return classes.searchFiles(regExp);
    }

    
    /**
     * Search file by regular expression
     *
     * @param regExp the search regular expression
     * @return a list with full qualified class names or null if it does not
     *         exist
     */
    public List<URL> searchFileByRegExpAsURLList(String regExp) {
        return classes.searchFilesAsURL(regExp);
    }

    
    /**
     * Returns a list of all the class paths in the current VM.
     * 
     * @return the class paths in current vm
     */
    public Set<String> getClassPaths() {
        return classPaths.keySet();
    }
    
    
    /**
     * Returns a list of all the class paths in the current VM.
     * 
     * @return the class paths in current vm
     */
    private Map<String, String> initClassPaths() {
        Map<String, String> classPaths = new LinkedHashMap<String, String>();

        String pathSeparator = System.getProperty("path.separator");
        addSystemPropertyToClassPaths(classPaths, "sun.boot.class.path", pathSeparator);
        addSystemPropertyToClassPaths(classPaths, "java.class.path", pathSeparator);
        addSystemPropertyToClassPaths(classPaths, "java.ext.dirs", pathSeparator);
        addSystemEnvironmentToClassPaths(classPaths, "CLASSPATH", pathSeparator);
        //addSystemPropertyToClassPaths( "java.library.path", pathSeparator );

        ClassLoader cl = Thread.currentThread().getContextClassLoader().getClass().getClassLoader();
        if (cl == null) {
            /*
            if( classes.getClassesFromPackage( JAVA_LANG ) == null )
            {
              //Object.class.getResource( "Object.class" );
                try
                {
                    String javaHome = System.getProperty( "java.home" );
                    if( javaHome !=null && !javaHome.isBlank() )
                    {
                        String javaBase = javaHome + "/" + "jmods" + "/" + "java.base.jmod";
                        
                    
                        URL url = new File(javaBase).toURL();
                        //   Object.class.getClassLoader()
                        cl = new URLClassLoader( new URL[] { url }, ClassLoader.getPlatformClassLoader() );
                    }
                }
                catch( MalformedURLException e )
                {
                } 
            } 
            */
        }
        
        while (cl != null) {
            if (URLClassLoader.class.isAssignableFrom(cl.getClass())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Read from URLclassloader " + cl.getClass().getName() + "!");
                }

                try {
                    for (URL url : ((URLClassLoader) cl).getURLs()) {
                        String s = FileUtil.getInstance().extractURLPath(url);
                        if (s != null && !classPaths.containsKey(s)) {
                            classPaths.put(s, s);
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Could not read classes from url classloader " + cl.getClass().getName() + ": " + e.getMessage(), e);
                }
            } else if (cl != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Read from classloader " + cl.getClass().getName() + "!");
                }

                try {
                    Enumeration<URL> e = cl.getResources("META-INF");
                    if (e != null) {
                        for (URL url : Collections.list(e)) {
                            String s = FileUtil.getInstance().extractURLPath(url);
                            if (s != null && !classPaths.containsKey(s)) {
                                classPaths.put(s, s);
                            }
                        }

                        cl = null;
                    }
                } catch (Exception e) {
                    LOG.warn("Could not read classes from classloader " + cl.getClass().getName() + ": " + e.getMessage(), e);
                }
            }

            if (cl != null) {
                cl = cl.getParent();
            }
        }

        return classPaths;
    }


    /**
     * Returns a list of all the classes in your classpath
     * 
     * @return the classes in the classpath
     */
    private ClassList initClassList() {
        ClassList classes = new ClassList();
        Set<String> c = getClassPaths();

        for (String className : c) {
            addClassesInPath(className, classes);
        }
        
        if (classes.getClassesFromPackage(JAVA_LANG) == null) {
            /* don't work
        
            //java.lang.Object.class.get;
            Class<?>[] cc = java.lang.Object.class.getClasses();
            for( Class< ? > clazz : java.lang.Object.class.getClasses() )
                classes.addClass( clazz.getName() );
                //URL url = Object.class.getResource("Object.class");
            */

            String javaHome = System.getProperty("java.home");
            if (javaHome != null && !javaHome.trim().isEmpty()) {
                // FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
                // fs.getPath("java.base", "java/lang/String.class"));
                String javaBase = javaHome + "/" + "jmods" + "/" + "java.base.jmod";
                File javaBaseFile = new File(javaBase);

                if (javaBaseFile.exists()) {
                    addClassesInArchive(new File(javaBase), classes, "classes/");
                }
            }
        }

        return classes;
    }

    
    /**
     * Adds a path to the classpath
     *
     * @param path the path to add
     * @param c the classes
     */
    private void addClassesInPath(String path, ClassList c) {
        File file = new File(path);

        if (file.exists()) {
            if (file.isDirectory()) {
                addClassesInFileSystem(file, file.getPath(), c);
            } else {
                addClassesInArchive(file, c, null);
            }
        }
    }

    /**
     * Add all classes in the file system if it is a class
     *
     * @param file the files
     * @param basePath the base path
     * @param c the classes
     */
    private void addClassesInFileSystem(File file, String basePath, ClassList c) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    addClassesInFileSystem(files[i], basePath, c);
                }
            }
        } else {
            String name = file.getPath().substring(basePath.length() + 1);
            // URL url = new URL("jar", "", "file:/" + file.getAbsolutePath());

            URL url = null;
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                // NOP
            }

            addIfClass(url, name, c, null);
        }
    }

    
    /**
     * Add all classes in the archive if it is a class
     *
     * @param archive the archive to test
     * @param c the classes
     * @param prefixFilter the prefix filter
     */
    @SuppressWarnings("deprecation")
    private void addClassesInArchive(File archive, ClassList c, String prefixFilter) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(archive);
            Enumeration<? extends ZipEntry> e = zip.entries();

            while (e.hasMoreElements()) {
                ZipEntry zipE = e.nextElement();

                // TODO:
                // load jar archive.toURI().toASCIIString() and get module-info -> modulename
                // url = new URL( "jrt", "", moduleName + "/" + zipE.getName() );
                String urlPath = archive.toURI().toASCIIString() + "!/" + zipE.getName();

                URL url = null;
                try {
                    url = new URL("jar", "", urlPath);
                } catch (MalformedURLException ex) {
                    LOG.warn("Invalid url " + urlPath + "!");
                }

                addIfClass(url, zipE.getName(), c, prefixFilter);
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not add classes from path or archive (" + archive + "): " + e.getMessage());
            }
            String name = archive.getPath();

            String basePath = FileUtil.getInstance().slashify(System.getProperty("user.dir"));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Name: " + name + ", basePath:" + basePath);
            }

            if (FileUtil.getInstance().slashify(name).startsWith(basePath)) {
                name = name.substring(basePath.length());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Name: " + name + " | " + basePath + " (" + archive.getName() + ")");
            }

            URL url = null;
            try {
                url = archive.toURI().toURL();
            } catch (MalformedURLException ex) {
                // NOP
            }

            addIfClass(url, name, c, null);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception e) {
                     // NOP
                }
            }
        }
    }


    /**
     * Add the entry if it is a class
     * 
     * @param url the url
     * @param e the entry to test
     * @param c the classes
     * @param prefixFilter the prefix filter
     */
    private void addIfClass(URL url, String e, ClassList c, String prefixFilter) {
        String entry = e;
        if (prefixFilter != null && !prefixFilter.trim().isEmpty() && entry.startsWith(prefixFilter)) {
            entry = entry.substring(prefixFilter.length());
        }

        int length = entry.length();
        int minusSix = length - 6;
        entry = entry.replace('\\', '/');
        
        if ((minusSix > 0) && (entry.indexOf(".class") == minusSix)) {
            entry = entry.substring(0, minusSix);
            for (int i = 0; i < fileSeparators.length; i++) {
                entry = entry.replace(fileSeparators[i], '.');
            }

            c.addClass(entry);
        } else {
            c.addFile(url, entry);
        }
    }

    
    /**
     * Add path elements
     * 
     * @param classPaths the class paths
     * @param propertyKey the property key
     * @param pathSeparator the path separator
     */
    private void addSystemPropertyToClassPaths(Map<String, String> classPaths, String propertyKey, String pathSeparator) {
        String path = System.getProperty(propertyKey);
        if (path == null) {
            return;
        }

        StringTokenizer toker = new StringTokenizer(path, pathSeparator);
        while (toker.hasMoreElements()) {
            String s = toker.nextToken();

            if (!classPaths.containsKey(s)) {
                classPaths.put(s, s);
            }
        }
    }

    
    /**
     * Add path elements
     * 
     * @param classPaths the class paths
     * @param envKey the environment key
     * @param pathSeparator the path separator
     */
    private void addSystemEnvironmentToClassPaths(Map<String, String> classPaths, String envKey, String pathSeparator) {
        String path = System.getenv(envKey);
        if (path == null) {
            return;
        }

        StringTokenizer toker = new StringTokenizer(path, pathSeparator);
        while (toker.hasMoreElements()) {
            String s = toker.nextToken();

            if (!classPaths.containsKey(s)) {
                classPaths.put(s, s);
            }
        }
    }

    
    /**
     * This class holds the class names
     */
    public final class ClassList {
        private Map<String, List<String>> classNameMap;
        private Map<String, List<String>> packageNameMap;
        private Set<String> list;
        private Set<String> fileList;
        private Map<String, List<URL>> urlFileMap;

        
        /**
         * Default constructor for ClassList
         */
        ClassList() {
            list = new TreeSet<String>();
            fileList = new TreeSet<String>();
            classNameMap = new HashMap<String, List<String>>();
            packageNameMap = new HashMap<String, List<String>>();
            urlFileMap = new HashMap<String, List<URL>>();
        }


        /**
         * Adds a class
         *
         * @param name the class
         */
        void addClass(String name) {
            if (name == null || name.length() == 0) {
                return;
            }
            
            if (!list.contains(name)) {
                list.add(name);
            }

            String className = name;
            String packageName = "";
            int index = name.lastIndexOf('.');
            if (index > 0) {
                className = name.substring(index + 1);
                packageName = name.substring(0, index);
            }

            // store package name access
            if (packageNameMap.containsKey(packageName)) {
                List<String> l = packageNameMap.get(packageName);
                if (!l.contains(name)) {
                    l.add(name);
                    Collections.sort(l);
                }
            } else {
                List<String> l = new ArrayList<String>();
                l.add(name);
                packageNameMap.put(packageName, l);
            }

            // store class name access
            if (classNameMap.containsKey(className)) {
                List<String> l = classNameMap.get(className);
                if (!l.contains(name)) {
                    l.add(name);
                }
            } else {
                List<String> l = new ArrayList<String>();
                l.add(name);
                classNameMap.put(className, l);
            }
        }


        /**
         * Adds a file
         * 
         * @param url the url
         * @param name the file name
         */
        void addFile(URL url, String name) {
            if (name == null || name.length() == 0 || name.endsWith("/")) {
                return;
            }

            List<URL> urlFileList = urlFileMap.get(name);
            if (urlFileList == null) {
                urlFileList = new ArrayList<URL>();
            }
            
            if (!fileList.contains(name)) {
                fileList.add(name);
            }

            if (url != null && !urlFileList.contains(url)) {
                urlFileList.add(url);
            }

            urlFileMap.put(name, urlFileList);
        }

        
        /**
         * Gets a list of class names back
         *
         * @param name the name of the class with or without package name
         * @return a list with full qualified class names
         */
        public List<String> getClasses(String name) {
            if (name == null || name.trim().length() == 0) {
                return null;
            }

            String ref = name;
            ref = ref.replace(FileUtil.BACKSLASH, FileUtil.SLASH);

            if (ref.endsWith(".java")) {
                ref = ref.substring(0, ref.length() - ".java".length());
            }
            
            if (ref.endsWith(".class")) {
                ref = ref.substring(0, ref.length() - ".class".length());
            }
            
            ref = ref.replace(FileUtil.SLASH, '.');
            ref = StringUtil.getInstance().trimLeft(ref, '.');

            if (ref.indexOf('.') >= 0) {
                String className = ref;
                int index = ref.lastIndexOf('.');
                if (index > 0) {
                    className = ref.substring(index + 1);
                }
                
                List<String> nameList = classNameMap.get(className);
                if (nameList != null && nameList.contains(ref)) {
                    List<String> l = new ArrayList<String>();
                    l.add(ref);
                    return l;
                }
            }

            return classNameMap.get(name);
        }

        
        /**
         * Search class which are mapping to the given regular expression
         *
         * @param regExp the search regular expression
         * @return a list with full qualified class names
         */
        public List<String> searchClasses(String regExp) {
            if (regExp == null || regExp.trim().length() == 0) {
                return null;
            }

            Pattern pattern = Pattern.compile(regExp.trim());
            List<String> resolvedClasses = new ArrayList<String>();
            for (String name : list) {

                if (pattern.matcher(name).matches()) {
                    resolvedClasses.add(name);
                }
            }

            return resolvedClasses;
        }

        
        /**
         * Search file which are mapping to the given regular expression
         *
         * @param regExp the search regular expression
         * @return a list with full qualified class names
         */
        public List<String> searchFiles(String regExp) {
            if (regExp == null || regExp.trim().length() == 0) {
                return null;
            }

            Pattern pattern = Pattern.compile(regExp.trim());
            List<String> resolvedFiles = new ArrayList<String>();

            for (String name : fileList) {
                if (pattern.matcher(name).matches()) {
                    resolvedFiles.add(name);
                }
            }

            return resolvedFiles;
        }
        
        
        /**
         * Search file which are mapping to the given regular expression
         *
         * @param regExp the search regular expression
         * @return a list with full qualified class names
         */
        public List<URL> searchFilesAsURL(String regExp) {
            if (regExp == null || regExp.trim().length() == 0) {
                return null;
            }

            Pattern pattern = Pattern.compile(regExp.trim());
            List<URL> resolvedFiles = new ArrayList<URL>();

            for (String name : fileList) {
                if (pattern.matcher(name).matches()) {
                    List<URL> urlFileList = urlFileMap.get(name);
                    for (URL url : urlFileList) {
                        resolvedFiles.add(url);
                    }
                }
            }

            Collections.sort(resolvedFiles, new Comparator<URL>() {
                /**
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(URL url1, URL url2) {
                    return url1.toString().compareToIgnoreCase(url2.toString());
                }
            });
            
            return resolvedFiles;
        }


        /**
         * Gets a list of class names back
         *
         * @param packageName the name of the package
         * @return a list with full qualified class names
         */
        public List<String> getClassesFromPackage(String packageName) {
            if (packageName == null || packageName.trim().length() == 0) {
                return null;
            }

            return packageNameMap.get(StringUtil.getInstance().trimRight(packageName, '.'));
        }

        
        /**
         * Gets an iterator to go through the class names
         *
         * @return the iterator
         */
        public Iterator<String> iterator() {
            return list.iterator();
        }        

        
        /**
         * toString 
         */
        @Override
        public String toString() {
            return "" + urlFileMap.values();
        }
    }
}
