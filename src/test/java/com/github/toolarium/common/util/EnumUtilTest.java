/*
 * EnumUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


/**
 * Tesz zje {@link EnumUtil}.
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
     * Test enum
     */
    enum TestEnum {
        A,
        B,
        C;
    }
}

