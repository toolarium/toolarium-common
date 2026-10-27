/*
 * EnumUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link EnumUtil}.
 * 
 * @author patrick
 */
public class EnumUtilTest {

    
    /**
     * Test
     */
    @Test
    public void test() {
        assertEquals(EnumUtil.getInstance().valueOf(TestEnum.class, "A"), TestEnum.A);
        assertEquals(EnumUtil.getInstance().valueOf(TestEnum.class, "B"), TestEnum.B);
        assertEquals(EnumUtil.getInstance().valueOf(TestEnum.class, "c"), TestEnum.C);
    }
    
    
    /**
     * Constants which differ only in case
     */
    @Test
    public void testCaseOnlyDifference() {
        assertEquals(CaseEnum.a, EnumUtil.getInstance().valueOf(CaseEnum.class, "a"));
        assertEquals(CaseEnum.A, EnumUtil.getInstance().valueOf(CaseEnum.class, "A"));
        assertEquals(CaseEnum.b, EnumUtil.getInstance().valueOf(CaseEnum.class, " B "));
    }


    /**
     * Enums of the JDK and unknown or empty names
     */
    @Test
    public void testJdkEnumAndUnknownName() {
        assertEquals(TimeUnit.SECONDS, EnumUtil.getInstance().valueOf(TimeUnit.class, "seconds"));
        assertEquals(TimeUnit.SECONDS, EnumUtil.getInstance().valueOf(TimeUnit.class, "SECONDS"));
        assertNull(EnumUtil.getInstance().valueOf(TimeUnit.class, "unknown"));
        assertNull(EnumUtil.getInstance().valueOf(TimeUnit.class, " "));
        assertNull(EnumUtil.getInstance().valueOf(TimeUnit.class, null));
    }


    /**
     * Test enum with constants which differ only in case
     */
    enum CaseEnum {
        /** a */
        a,

        /** A */
        A,

        /** b */
        b;
    }


    /**
     * Test enum
     */
    enum TestEnum {
        A,
        B,
        C;
    }
}

