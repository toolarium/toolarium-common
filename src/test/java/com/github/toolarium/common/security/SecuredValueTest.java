/*
 * SecuredValueTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;


/**
 * Test {@link SecuredValue}.
 *  
 * @author patrick
 */
public class SecuredValueTest {

    
    /**
     * Test {@link ISecuredValue}
     */
    @Test
    public void ss() {
        assertEquals("...", new SecuredValue<String>("secureValue1").toString());
        assertEquals("*", new SecuredValue<String>("secureValue2", "*").getSecuredValue());
        assertEquals("secureValue3", new SecuredValue<String>("secureValue3").getValue());
        assertEquals(5, new SecuredValue<>(5, "...").getValue());
        
        assertEquals(new SecuredValue<String>("secureValue"), new SecuredValue<String>("secureValue"));
        assertEquals(new SecuredValue<String>("secureValue", "...."), new SecuredValue<String>("secureValue", "...."));
        assertNotEquals(new SecuredValue<String>("secureValue", "*"), new SecuredValue<String>("secureValue2", "*"));
    }
}
