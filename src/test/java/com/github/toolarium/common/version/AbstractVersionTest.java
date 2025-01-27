/*
 * AbstractVersionTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;


import java.util.ArrayList;
import java.util.List;

/**
 * Defines parent test class for versions
 * 
 * @author patrick
 */
public class AbstractVersionTest {
    /** Invalid version list */
    protected static final List<String> VERSION_LIST_INVALID = parseVersionList("uu, aa, bb");

    private static final List<String> VERSION_LIST_0_0 = parseVersionList("0.0.2, 0.0.3, 0.0.4, 0.0.5, 0.0.6");
    private static final List<String> VERSION_LIST_0_1 = parseVersionList("0.1.0, 0.1.1, 0.1.2, 0.1.3, 0.1.4, 0.1.5");
    private static final List<String> VERSION_LIST_0_4 = parseVersionList("0.4.0, 0.4.1, 0.4.2, 0.4.5");
    private static final List<String> VERSION_LIST_0_4_11 = parseVersionList("0.4.11");
    private static final List<String> VERSION_LIST_0_4_11B = parseVersionList("0.4.11-b");
    private static final List<String> VERSION_LIST_0_5 = parseVersionList("0.5.0, 0.5.1, 0.5.2, 0.5.33");
    private static final List<String> VERSION_LIST_0_6 = parseVersionList("0.6.0, 0.6.1, 0.6.2, 0.6.3");
    private static final List<String> VERSION_LIST_0_7 = parseVersionList("0.7.0, 0.7.1, 0.7.2, 0.7.3");
    private static final List<String> VERSION_LIST_1_0 = parseVersionList("1.0.0, 1.0.1, 1.0.2, 1.0.3");
    private static final List<String> VERSION_LIST_1_1 = parseVersionList("1.1.0, 1.1.1, 1.1.2, 1.1.3, 1.1.4");
    private static final List<String> VERSION_LIST_1_2 = parseVersionList("1.2.0, 1.2.1, 1.2.2, 1.2.3, 1.2.4");
    private static final List<String> VERSION_LIST_1_3 = parseVersionList("1.3.0, 1.3.1, 1.3.2, 1.3.3, 1.3.4");
    private static final List<String> VERSION_LIST_2_0 = parseVersionList("2.0.0, 2.0.1, 2.0.2");
    private static final List<String> VERSION_LIST_2_1 = parseVersionList("2.1.0, 2.1.1, 2.1.2, 2.2.0, 2.2.1");
    
    
    /**
     * Get version list
     *
     * @param strictSemVer true to strict semver comliance
     * @return the list
     */
    protected List<String> getVersionList(boolean strictSemVer) {
        List<String> result = new ArrayList<String>();
        result.addAll(VERSION_LIST_0_0);
        result.addAll(VERSION_LIST_0_1);
        result.addAll(VERSION_LIST_0_4);
        
        if (strictSemVer) {
            result.addAll(VERSION_LIST_0_4_11B);
            result.addAll(VERSION_LIST_0_4_11);
        } else {
            result.addAll(VERSION_LIST_0_4_11);
            result.addAll(VERSION_LIST_0_4_11B);
        }
        
        result.addAll(VERSION_LIST_0_5);
        result.addAll(VERSION_LIST_0_6);
        result.addAll(VERSION_LIST_0_7);
        result.addAll(VERSION_LIST_1_0);
        result.addAll(VERSION_LIST_1_1);
        result.addAll(VERSION_LIST_1_2);
        result.addAll(VERSION_LIST_1_3);
        result.addAll(VERSION_LIST_2_0);
        result.addAll(VERSION_LIST_2_1);
        return result;
    }
    
    
    /**
     * Parse list
     *
     * @param list the string list
     * @return the parsed list
     */
    protected static List<String> parseVersionList(String list) {
        List<String> result = new ArrayList<String>();
        String[] versionList = list.trim().split(",");
        for (int i = 0; i < versionList.length; i++) {
            result.add(versionList[i].trim());
        }
        
        return result;
    }
}
