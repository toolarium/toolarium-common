/*
 * PropertyExpanderTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;
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
