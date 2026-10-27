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
        PropertyExpanderContextBasedProperties.unset();
    }


    /**
     * Test runWith sets and cleans up properties
     */
    @Test
    public void testRunWith() {
        Properties prop = new Properties();
        prop.setProperty("RW_ATTR", "runWith value");
        AtomicReference<String> captured = new AtomicReference<>();
        PropertyExpanderContextBasedProperties.unset();

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
        PropertyExpanderContextBasedProperties.unset();

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
     * Test that a reference which is not well-formed is kept unchanged, that a reference without braces ends at the
     * first character which is not part of a name and that an empty value is a valid value.
     */
    @Test
    public void testReferenceSyntaxAndEmptyValue() {
        Properties prop = new Properties();
        prop.setProperty("PE_HOME", "/opt/app");
        prop.setProperty("app.name", "myapp");
        prop.setProperty("PE_EMPTY", "");
        PropertyExpanderContextBasedProperties.set(prop);
        System.setProperty("PE_EMPTY", "system value");
        try {
            PropertyExpander expander = PropertyExpander.getInstance();

            // not well-formed references are kept as they are
            assertEquals("${PE_HOME bar}", expander.expand("${PE_HOME bar}"));
            assertEquals("a ${PE_HOME   b", expander.expand("a ${PE_HOME   b"));
            assertEquals("x ${  ", expander.expand("x ${  "));

            // reference without braces
            assertEquals("/opt/app/bin", expander.expand("$PE_HOME/bin"));
            assertEquals("myapp:/opt/app", expander.expand("$app.name:$PE_HOME"));
            assertEquals("/opt/app}", expander.expand("$PE_HOME}"));
            assertEquals("/opt/app/bin", expander.expand("${PE_HOME}/bin"));

            // an empty context property overrides the system property
            assertEquals("[]", expander.expand("[$PE_EMPTY]"));
            assertEquals("[]", expander.expand("[${PE_EMPTY}]"));
        } finally {
            PropertyExpanderContextBasedProperties.unset();
            System.clearProperty("PE_EMPTY");
        }
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
