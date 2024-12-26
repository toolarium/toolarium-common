/*
 * StringUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link StringUtil}.
 *  
 * @author patrick
 */
public class StringUtilTest {
    private static final String EMPTY_ARRAY_STRING = "[]";
    private static final String SLASH = "/";
    private static final String NUMBERS = "12345678901234567890";
    private static final String A_SIMPLE_EXAMPLE = "a simple example";
    private static final String A_SIMPLE_EXAMPLE_CAMEL_CASE = "aSimpleExample";
    private static final String A_SIMPLE_EXAMPLE_SNAKE_CASE = "a_simple_example";
    private static final String A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE = "A_SIMPLE_EXAMPLE";
    
    private static final String THIS_IS_AN_EXAMPLE = "This is an example";
    private static final String THIS_IS_AN_EXAMPLE2 = "This.is.an.example";
    private static final String THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE = "This_is_an_example";
    private static final String THIS_IS_AN_EXAMPLE_CAMEL_CASE = "thisIsAnExample";
    private static final String THIS_IS_AN_EXAMPLE_SNAKE_CASE = "this_is_an_example";
    private static final String THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE = "THIS_IS_AN_EXAMPLE";
    private static final String THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE = "ThisIsAnExample";
    
    private static final String IS_IT_A_SIMPLE_EXAMPLE = "is it a simple example";
    private static final String IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE = "IS_IT_A_SIMPLE_EXAMPLE";

    
    /**
     * Test new string
     */
    @Test
    public void testNewString() {
        assertNull(StringUtil.getInstance().newString(null, 0));
        assertEquals("", StringUtil.getInstance().newString(' ', 0));
        assertEquals("", StringUtil.getInstance().newString(' ', -1));
        assertEquals("      ", StringUtil.getInstance().newString(' ', 6));
        
        assertNull(StringUtil.getInstance().newString(null, 0));
        assertEquals("", StringUtil.getInstance().newString("", 0));
        assertEquals("", StringUtil.getInstance().newString(" ", 0));
        assertEquals("", StringUtil.getInstance().newString(" ", -1));
        assertEquals("     ", StringUtil.getInstance().newString(" ", 5));
    }
    
    
    /**
     * Test the count ending character
     */
    @Test
    public void testCountCharacters() {
        char sep = '_';
        assertEquals(0, StringUtil.getInstance().countCharacters(null, sep));
        assertEquals(0, StringUtil.getInstance().countCharacters("", sep));
        assertEquals(0, StringUtil.getInstance().countCharacters(" ", sep));
        assertEquals(1, StringUtil.getInstance().countCharacters("_", sep));
        assertEquals(2, StringUtil.getInstance().countCharacters("__", sep));
        assertEquals(0, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk", sep));
        assertEquals(1, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk_", sep));
        assertEquals(2, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk__", sep));
        assertEquals(3, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk___", sep));
        assertEquals(4, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk____", sep));
        assertEquals(5, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk_____", sep));
        assertEquals(6, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk______", sep));
        assertEquals(3, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk__ _", sep));
        assertEquals(3, StringUtil.getInstance().countCharacters("dsdfjlsdhjhsdfjfhsdjkfhsdfhk__A_", sep));
        assertEquals(1, StringUtil.getInstance().countCharacters("_dsdfjlsdhjhsdfjfhsdjkfhsdfhk", sep));
    }

    
    /**
     * Test the count ending character
     */
    @Test
    public void testCountEndingCharacter() {
        char sep = '_';
        assertEquals(0, StringUtil.getInstance().countEndingCharacter(null, sep));
        assertEquals(0, StringUtil.getInstance().countEndingCharacter("", sep));
        assertEquals(0, StringUtil.getInstance().countEndingCharacter(" ", sep));
        assertEquals(1, StringUtil.getInstance().countEndingCharacter("_", sep));
        assertEquals(2, StringUtil.getInstance().countEndingCharacter("__", sep));
        assertEquals(0, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk", sep));
        assertEquals(1, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk_", sep));
        assertEquals(2, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk__", sep));
        assertEquals(3, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk___", sep));
        assertEquals(4, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk____", sep));
        assertEquals(5, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk_____", sep));
        assertEquals(6, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk______", sep));
        assertEquals(1, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk__ _", sep));
        assertEquals(1, StringUtil.getInstance().countEndingCharacter("dsdfjlsdhjhsdfjfhsdjkfhsdfhk__A_", sep));
        assertEquals(0, StringUtil.getInstance().countEndingCharacter("_dsdfjlsdhjhsdfjfhsdjkfhsdfhk", sep));
    }
    
    
    /**
     * Test left trim
     */
    @Test
    public void testLeftTrim() {
        assertEquals(null, StringUtil.getInstance().trimLeft(null));
        assertEquals(null, StringUtil.getInstance().trimLeft(null, 'a'));
        assertEquals("", StringUtil.getInstance().trimLeft(""));
        assertEquals("", StringUtil.getInstance().trimLeft("", 'b'));
        assertEquals("y string ", StringUtil.getInstance().trimLeft("mmy string ", 'm'));
        assertEquals("mmy string ", StringUtil.getInstance().trimLeft(" mmy string ", ' '));
    }

    
    /**
     * Test left trim
     */
    @Test
    public void testRightTrim() {
        assertEquals(null, StringUtil.getInstance().trimRight(null));
        assertEquals(null, StringUtil.getInstance().trimRight(null, 'a'));
        assertEquals("", StringUtil.getInstance().trimRight(""));
        assertEquals("", StringUtil.getInstance().trimRight("", 'b'));
        assertEquals("mmy string", StringUtil.getInstance().trimRight("mmy string  ", ' '));
        assertEquals("  mmy string", StringUtil.getInstance().trimRight("  mmy string ", ' '));
    }

    
    /**
     * Test 
     */
    @Test
    public void testWidth() {
        assertEquals("     ", StringUtil.getInstance().width(null, 5, ' ', true));
        assertEquals("     ", StringUtil.getInstance().width("", 5, ' ', true));
        assertEquals("     ", StringUtil.getInstance().width(null, 5));
        assertEquals("     ", StringUtil.getInstance().width("", 5));
        assertEquals("Test   ", StringUtil.getInstance().width("Test", 7));
    }
    
    
    /**
     * Test the split
     */
    @Test
    public void testSplitAsList() {
        assertNull(StringUtil.getInstance().splitAsList(null, null));
        assertEquals(EMPTY_ARRAY_STRING, StringUtil.getInstance().splitAsList("", null).toString());
        assertEquals(EMPTY_ARRAY_STRING, StringUtil.getInstance().splitAsList("", "").toString());
        assertNull(StringUtil.getInstance().splitAsList(null, ""));

        assertEquals("[1]", StringUtil.getInstance().splitAsList("1", "").toString());
        assertEquals("[1, 2, 3]", StringUtil.getInstance().splitAsList("123", "").toString());
        assertEquals("[1, 2, a]", StringUtil.getInstance().splitAsList("1/2/a", SLASH).toString());
        assertEquals("[1, 2, 3]", StringUtil.getInstance().splitAsList("1/@*2/@*3", "/@*").toString());
        assertEquals("[1]", StringUtil.getInstance().splitAsList("1", SLASH).toString());
        assertEquals("[1]", StringUtil.getInstance().splitAsList("1/", SLASH).toString());
        assertEquals("[1]", Arrays.asList("1".split(SLASH)).toString());
        assertEquals("[1]", Arrays.asList("1/".split(SLASH)).toString());
    }

    
    /**
     * Test the split
     */
    @Test
    public void testSplitAsArray() {
        assertNull(StringUtil.getInstance().splitAsArray(null, null));
        assertEquals(EMPTY_ARRAY_STRING, Arrays.asList(StringUtil.getInstance().splitAsArray("", null)).toString());
        assertEquals(EMPTY_ARRAY_STRING, Arrays.asList(StringUtil.getInstance().splitAsArray("", "")).toString());
        assertNull(StringUtil.getInstance().splitAsArray(null, ""));

        assertEquals("[1, 2, 3]", Arrays.asList(StringUtil.getInstance().splitAsArray("123", "")).toString());
        assertEquals("[1, 2, 3]", Arrays.asList(StringUtil.getInstance().splitAsArray("1/2/3", SLASH)).toString());
        assertEquals("[1, 2, 3]", Arrays.asList(StringUtil.getInstance().splitAsArray("1/@*2/@*3", "/@*")).toString());
    }

    
    /**
     * Test the split
     */
    @Test
    public void testSplitAsListByLength() {
        assertNull(StringUtil.getInstance().splitAsList(null, -1));
        assertNull(StringUtil.getInstance().splitAsList(null, 0));
        assertNull(StringUtil.getInstance().splitAsList(null, 1));
        assertEquals(EMPTY_ARRAY_STRING, StringUtil.getInstance().splitAsList("", -1).toString());
        assertEquals(EMPTY_ARRAY_STRING, StringUtil.getInstance().splitAsList("", 0).toString());
        assertEquals(EMPTY_ARRAY_STRING, StringUtil.getInstance().splitAsList("", 1).toString());

        assertEquals("[1, 2, 3, 4, 5, 6]", StringUtil.getInstance().splitAsList("123456", 1).toString());
        assertEquals("[12, 34, 56, 7]", StringUtil.getInstance().splitAsList("1234567", 2).toString());
        assertEquals("[123456]", StringUtil.getInstance().splitAsList("123456", 6).toString());
        assertEquals("[123456]", StringUtil.getInstance().splitAsList("123456", 7).toString());
    }

    
    /**
     * Test the split
     */
    @Test
    public void testSplitAsArrayByLength() {
        assertNull(StringUtil.getInstance().splitAsArray(null, -1));
        assertNull(StringUtil.getInstance().splitAsArray(null, 0));
        assertNull(StringUtil.getInstance().splitAsArray(null, 1));
        assertEquals(EMPTY_ARRAY_STRING, Arrays.asList(StringUtil.getInstance().splitAsArray("", -1)).toString());
        assertEquals(EMPTY_ARRAY_STRING, Arrays.asList(StringUtil.getInstance().splitAsArray("", 0)).toString());
        assertEquals(EMPTY_ARRAY_STRING, Arrays.asList(StringUtil.getInstance().splitAsArray("", 1)).toString());

        assertEquals("[1, 2, 3, 4, 5]", Arrays.asList(StringUtil.getInstance().splitAsArray("12345", 1)).toString());
        assertEquals("[12, 34, 5]", Arrays.asList(StringUtil.getInstance().splitAsArray("12345", 2)).toString());
        assertEquals("[12345]", Arrays.asList(StringUtil.getInstance().splitAsArray("12345", 5)).toString());
        assertEquals("[12345]", Arrays.asList(StringUtil.getInstance().splitAsArray("12345", 6)).toString());
    }

    
    /** 
     * Test
     * @throws Exception in case of error
     */
    @Test
    public void testCamelCase() throws Exception {
        assertEquals(null, StringUtil.getInstance().toCamelCase(null, false));
        assertEquals("", StringUtil.getInstance().toCamelCase("", false));
        assertEquals("", StringUtil.getInstance().toCamelCase("    ", false));
        
        assertEquals(A_SIMPLE_EXAMPLE_CAMEL_CASE, StringUtil.getInstance().toCamelCase(A_SIMPLE_EXAMPLE, true));
        assertEquals("isItASimpleExample", StringUtil.getInstance().toCamelCase(IS_IT_A_SIMPLE_EXAMPLE, true));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE, true));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE2, true));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, true));
        assertEquals(NUMBERS, StringUtil.getInstance().toCamelCase(NUMBERS, true));

        assertEquals("ASimpleExample", StringUtil.getInstance().toCamelCase(A_SIMPLE_EXAMPLE, false));
        assertEquals("IsItASimpleExample", StringUtil.getInstance().toCamelCase(IS_IT_A_SIMPLE_EXAMPLE, false));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE, false));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE2, false));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, false));
        assertEquals(NUMBERS, StringUtil.getInstance().toCamelCase(NUMBERS, false));

        assertEquals(A_SIMPLE_EXAMPLE_CAMEL_CASE, StringUtil.getInstance().toCamelCase(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE, true));
        assertEquals("isItASimpleExample", StringUtil.getInstance().toCamelCase(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE, true));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, true));
        assertEquals(NUMBERS, StringUtil.getInstance().toCamelCase(NUMBERS, true));

        assertEquals("ASimpleExample", StringUtil.getInstance().toCamelCase(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE, false));
        assertEquals("IsItASimpleExample", StringUtil.getInstance().toCamelCase(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE, false));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE, StringUtil.getInstance().toCamelCase(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, false));
        assertEquals(NUMBERS, StringUtil.getInstance().toCamelCase(NUMBERS, false));
        
        assertEquals("MySimpleExample", StringUtil.getInstance().toCamelCase("my simple example", false));
        assertEquals("mySimpleExample", StringUtil.getInstance().toCamelCase("my simple example", true));
        assertEquals("ASimpleExample", StringUtil.getInstance().toCamelCase("a simple example", false));
        assertEquals("aSimpleExample", StringUtil.getInstance().toCamelCase("a simple example", true));
        assertEquals("MySimpleExample", StringUtil.getInstance().toCamelCase("My Simple example", false));
        assertEquals("mySimpleExample", StringUtil.getInstance().toCamelCase("My Simple example", true));
        assertEquals("ASimpleExample", StringUtil.getInstance().toCamelCase("A Simple example", false));
        assertEquals("aSimpleExample", StringUtil.getInstance().toCamelCase("A Simple example", true));
        
        String testString = "This is a very interesting case with a full Roundtrip";
        assertEquals("thisIsAVeryInterestingCaseWithAFullRoundtrip", StringUtil.getInstance().toCamelCase(testString, true));
        assertEquals("ThisIsAVeryInterestingCaseWithAFullRoundtrip", StringUtil.getInstance().toCamelCase(testString, false));
        assertEquals("this_is_a_very_interesting_case_with_a_full_roundtrip", StringUtil.getInstance().fromCamelCaseToSnakeCase("thisIsAVeryInterestingCaseWithAFullRoundtrip", true));
        assertEquals("THIS_IS_A_VERY_INTERESTING_CASE_WITH_A_FULL_ROUNDTRIP", StringUtil.getInstance().fromCamelCaseToSnakeCase("thisIsAVeryInterestingCaseWithAFullRoundtrip", false));
        assertEquals("This_is_a_very_interesting_case_with_a_full_Roundtrip", StringUtil.getInstance().toSnakeCase(testString));

        assertEquals("thisIsAVeryInterestingCaseWithAFullRoundtrip", StringUtil.getInstance().toCamelCase("this_is_a_very_interesting_case_with_a_full_roundtrip", true));
        assertEquals("ThisIsAVeryInterestingCaseWithAFullRoundtrip", StringUtil.getInstance().toCamelCase("this_is_a_very_interesting_case_with_a_full_roundtrip", false));
        assertEquals("ThisIsAVeryInterestingCaseWithAFullRoundtrip", StringUtil.getInstance().toCamelCase(testString, false));

        // to camel case == to snake -> to camel
        assertEquals(StringUtil.getInstance().toCamelCase(testString, false /* startWithLowerCase */), 
                     StringUtil.getInstance().toCamelCase(StringUtil.getInstance().toSnakeCase(testString), false));
        assertEquals(StringUtil.getInstance().toCamelCase(testString, true /* startWithLowerCase */), 
                     StringUtil.getInstance().toCamelCase(StringUtil.getInstance().toSnakeCase(testString), true));

        // to snake case == to camel -> to snake
        assertEquals(StringUtil.getInstance().fromCamelCaseToSnakeCase(StringUtil.getInstance().toCamelCase(testString, false), true),
                     StringUtil.getInstance().toSnakeCase(testString).toLowerCase());
        assertEquals(StringUtil.getInstance().fromCamelCaseToSnakeCase(StringUtil.getInstance().toCamelCase(testString, false), false),
                     StringUtil.getInstance().toSnakeCase(testString).toUpperCase());
    }

    
    /** 
     * Test
     * @throws Exception in case of error
     */
    @Test
    public void testSnakeCase() throws Exception {
        assertEquals(null, StringUtil.getInstance().toSnakeCase(null));
        assertEquals("", StringUtil.getInstance().toSnakeCase(""));
        assertEquals("", StringUtil.getInstance().toSnakeCase("    "));

        assertEquals(A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().toSnakeCase(A_SIMPLE_EXAMPLE));
        assertEquals("is_it_a_simple_example", StringUtil.getInstance().toSnakeCase(IS_IT_A_SIMPLE_EXAMPLE));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE2));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE));
        assertEquals(NUMBERS, StringUtil.getInstance().toSnakeCase(NUMBERS));

        assertEquals(A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().toSnakeCase(A_SIMPLE_EXAMPLE));
        assertEquals("is_it_a_simple_example", StringUtil.getInstance().toSnakeCase(IS_IT_A_SIMPLE_EXAMPLE));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE2));
        assertEquals(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE));
        assertEquals(NUMBERS, StringUtil.getInstance().toSnakeCase(NUMBERS));

        assertEquals(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE, StringUtil.getInstance().toSnakeCase(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE));
        assertEquals(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().toSnakeCase(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE));
        assertEquals(NUMBERS, StringUtil.getInstance().toSnakeCase(NUMBERS));

        assertEquals(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE, StringUtil.getInstance().toSnakeCase(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE));
        assertEquals(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().toSnakeCase(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().toSnakeCase(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE));
        assertEquals(NUMBERS, StringUtil.getInstance().toSnakeCase(NUMBERS));
    }

    // toCaml -> aSimple
    // toSnake > a_simple
    // fromCam > aSimple -> a_simple
    /** 
     * Test
     * @throws Exception in case of error
     */
    @Test
    public void testFromCamelCaseToSnakeCase() throws Exception {
        assertEquals(null, StringUtil.getInstance().fromCamelCaseToSnakeCase(null, true));
        assertEquals("", StringUtil.getInstance().fromCamelCaseToSnakeCase("", true));
        assertEquals("", StringUtil.getInstance().fromCamelCaseToSnakeCase("    ", true));

        assertEquals(A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(A_SIMPLE_EXAMPLE_CAMEL_CASE, true));
        assertEquals("is_it_a_simple_example", StringUtil.getInstance().fromCamelCaseToSnakeCase("isItASimpleExample", true));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_CASE, true));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE, true));
        assertEquals(NUMBERS, StringUtil.getInstance().fromCamelCaseToSnakeCase(NUMBERS, true));

        assertEquals(A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(A_SIMPLE_EXAMPLE, true));
        assertEquals("is_it_a_simple_example", StringUtil.getInstance().fromCamelCaseToSnakeCase(IS_IT_A_SIMPLE_EXAMPLE, true));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE, true));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE2, true));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, true));
        assertEquals(NUMBERS, StringUtil.getInstance().fromCamelCaseToSnakeCase(NUMBERS, true));

        assertEquals(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(A_SIMPLE_EXAMPLE_CAMEL_CASE, false));
        assertEquals("IS_IT_A_SIMPLE_EXAMPLE", StringUtil.getInstance().fromCamelCaseToSnakeCase("isItASimpleExample", false));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_CASE, false));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_CASE_START_UPPERCASE, false));
        assertEquals(NUMBERS, StringUtil.getInstance().fromCamelCaseToSnakeCase(NUMBERS, false));

        assertEquals(A_SIMPLE_EXAMPLE_CAMEL_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(A_SIMPLE_EXAMPLE, false));
        assertEquals(IS_IT_A_SIMPLE_EXAMPLE_SNAKE_CASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(IS_IT_A_SIMPLE_EXAMPLE, false));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE, false));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE2, false));
        assertEquals(THIS_IS_AN_EXAMPLE_SNAKE_AND_UPPERCASE, StringUtil.getInstance().fromCamelCaseToSnakeCase(THIS_IS_AN_EXAMPLE_CAMEL_AND_MIXEDCASE, false));
        assertEquals(NUMBERS, StringUtil.getInstance().fromCamelCaseToSnakeCase(NUMBERS, false));

        assertEquals("THIS_IS_AN_EXAMPLE_", StringUtil.getInstance().fromCamelCaseToSnakeCase("This.is.an.example.", false));
        assertEquals("THIS_IS_AN_EXAMPLE_", StringUtil.getInstance().fromCamelCaseToSnakeCase("This.is.an.example.  ", false));
    }
    
    
    /**
     * Test to string methods
     */
    @Test
    public void testToString() {
        assertNull(StringUtil.getInstance().toString((String[])null));
        assertNull(StringUtil.getInstance().toString((Object[])null));
        assertEquals("", StringUtil.getInstance().toString(new String[] {}));
        assertEquals("A B", StringUtil.getInstance().toString(new String[] {"A", "B"}));
        assertEquals("A,B", StringUtil.getInstance().toString(new String[] {"A", "B"}, ","));
        
        assertEquals("", StringUtil.getInstance().toString(new Object[] {}));
        assertEquals("A, B", StringUtil.getInstance().toString(new Object[] {"A", "B"}));
        assertEquals("A:B", StringUtil.getInstance().toString(new Object[] {"A", "B"}, ":"));

        assertEquals("A2:01", StringUtil.getInstance().toString(new byte[] {(byte)0xA2, (byte)0x01 }));
        
    }
}
