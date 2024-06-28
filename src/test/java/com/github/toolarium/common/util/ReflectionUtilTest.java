/*
 * ReflectionUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import com.github.toolarium.common.statistic.StatisticCounter;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link ReflectionUtil}.
 * 
 * @author patrick
 */
public class ReflectionUtilTest {

    /**
     * Test
     *
     * @throws SecurityException In case of a security exceptiom
     * @throws InstantiationException In case the class can not be instantiated
     * @throws IllegalAccessException In case of illegal access
     */
    @Test
    public void test() throws SecurityException, InstantiationException, IllegalAccessException {
        ReflectionUtil.getInstance().disableAccessWarnings();
        ReflectionUtil.getInstance().newInstance(StatisticCounter.class);
    }
}
