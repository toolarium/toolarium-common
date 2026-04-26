/*
 * PropertyExpanderTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;


/**
 * Property expander test
 *
 * @author patrick
 */
public class PropertyExpanderTest {
    
    /**
     * Test property expander
     */
    @Test
    public void attributeTest() {
        System.setProperty("TEST_ATTR1", "test attribute");

        Properties prop = new Properties();
        prop.setProperty("TEST_ATTR2", "test context based attribute");
        PropertyExpanderContextBasedProperties.set(prop);

        assertNull(PropertyExpander.getInstance().expand(null));
        assertEquals(PropertyExpander.getInstance().expand(""), "");
        assertEquals(PropertyExpander.getInstance().expand("   "), "   ");

        assertEquals(PropertyExpander.getInstance().expand("this is a text"), "this is a text");
        assertEquals(PropertyExpander.getInstance().expand("this is a $text"), "this is a $text");
        assertEquals(PropertyExpander.getInstance().expand("this is a {text"), "this is a {text");
        assertEquals(PropertyExpander.getInstance().expand("this is a }text"), "this is a }text");
        assertEquals(PropertyExpander.getInstance().expand("  this is a text    "), "  this is a text    ");

        assertEquals(PropertyExpander.getInstance().expand("  this $MY is a text    "), "  this $MY is a text    ");
        assertEquals(PropertyExpander.getInstance().expand("  this ${MY} is a text    "), "  this ${MY} is a text    ");
        assertEquals(PropertyExpander.getInstance().expand("  this ${MY  } is a text    "), "  this ${MY} is a text    ");
        assertEquals(PropertyExpander.getInstance().expand("  this ${   MY  } is a text    "), "  this ${MY} is a text    ");

        assertEquals(PropertyExpander.getInstance().expand("  this is a ${TEST_ATTR1} and a text    "), "  this is a test attribute and a text    ");
        assertEquals(PropertyExpander.getInstance().expand("  this is a $TEST_ATTR1 and a text    "), "  this is a test attribute and a text    ");
        assertEquals(PropertyExpander.getInstance().expand("  this is a $TEST_ATTR1$TEST_ATTR1 and a text    "), "  this is a test attributetest attribute and a text    ");

        assertEquals(PropertyExpander.getInstance().expand("  this is a ${TEST_ATTR2} and a text    "), "  this is a test context based attribute and a text    ");
        assertEquals(PropertyExpander.getInstance().expand("  this is a $TEST_ATTR2 and a text    "), "  this is a test context based attribute and a text    ");

        assertEquals(PropertyExpander.getInstance().expand("  this is a {TEST_ATTR2} and a text    "), "  this is a {TEST_ATTR2} and a text    ");

        assertEquals(PropertyExpander.getInstance().expand("my$$test"), "my$$test");
        assertEquals(PropertyExpander.getInstance().expand("my $$ test"), "my $$ test");
        assertEquals(PropertyExpander.getInstance().expand("my${$}test"), "my${$}test");
    }


    /**
     * Test runWith sets and cleans up properties
     */
    @Test
    public void testRunWith() {
        Properties prop = new Properties();
        prop.setProperty("RW_ATTR", "runWith value");
        AtomicReference<String> captured = new AtomicReference<>();

        PropertyExpanderContextBasedProperties.runWith(prop, () -> {
            captured.set(PropertyExpander.getInstance().expand("${RW_ATTR}"));
        });

        assertEquals("runWith value", captured.get());
        // after runWith, properties should be cleaned up
        assertNull(PropertyExpanderContextBasedProperties.get());
    }


    /**
     * Test runWith cleans up even on exception
     */
    @Test
    public void testRunWithCleanupOnException() {
        Properties prop = new Properties();
        prop.setProperty("RW_ATTR2", "value");

        try {
            PropertyExpanderContextBasedProperties.runWith(prop, () -> {
                throw new RuntimeException("test exception");
            });
        } catch (RuntimeException e) {
            // expected
        }

        assertNull(PropertyExpanderContextBasedProperties.get());
    }


    /**
     * Test property expander
     */
    @Test
    public void invalidReference() {
        try {
            PropertyExpander.getInstance().expand("  this ${MY is a text    ");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Start but no end character for environment reference found for tag [MY], [  this ${MY is a text    ]");
        }
    }
}
