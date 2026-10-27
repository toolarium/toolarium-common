/*
 * PropertyExpanderContextBasedPropertiesTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link PropertyExpanderContextBasedProperties}.
 *
 * @author patrick
 */
public class PropertyExpanderContextBasedPropertiesTest {

    /**
     * Cleanup the thread local
     */
    @AfterEach
    public void cleanup() {
        PropertyExpanderContextBasedProperties.unset();
    }


    /**
     * Test nested runWith restores the outer context
     */
    @Test
    public void testNestedRunWith() {
        PropertyExpanderContextBasedProperties.unset();
        Properties outer = new Properties();
        Properties inner = new Properties();

        PropertyExpanderContextBasedProperties.runWith(outer, () -> {
            assertSame(outer, PropertyExpanderContextBasedProperties.get());
            PropertyExpanderContextBasedProperties.runWith(inner, () -> assertSame(inner, PropertyExpanderContextBasedProperties.get()));
            assertSame(outer, PropertyExpanderContextBasedProperties.get());
        });

        assertNull(PropertyExpanderContextBasedProperties.get());
    }


    /**
     * Test runWith restores a previously set context, also in case of an exception
     */
    @Test
    public void testRunWithRestoresPreviousOnException() {
        Properties previous = new Properties();
        PropertyExpanderContextBasedProperties.set(previous);

        assertThrows(IllegalStateException.class, () -> PropertyExpanderContextBasedProperties.runWith(new Properties(), () -> {
            throw new IllegalStateException("test");
        }));

        assertSame(previous, PropertyExpanderContextBasedProperties.get());
    }
}
