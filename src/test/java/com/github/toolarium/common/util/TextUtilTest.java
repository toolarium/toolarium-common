/*
 * TextUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.junit.jupiter.api.Test;


/**
 * Test the text util 
 * @author patrick
 */
public class TextUtilTest {
    private static final String THIS_IS_MY_TEXT = "this is my text";
    private static final String TITLE = "title:";
    private static final String SPACES5 = "     ";

    
    /**
     * Test prepare
     */
    @Test
    public void testPrepare() {
        assertEquals("", TextUtil.getInstance().prepareInput((String)null));
        assertEquals(" my text ", TextUtil.getInstance().prepareInput(" my text "));
        assertEquals("", TextUtil.getInstance().prepareInput((StringBuilder)null).toString());
        assertEquals(" my text ", TextUtil.getInstance().prepareInput(new StringBuilder(" my text ")).toString());
    }

    
    /**
     * Test prepare
     */
    @Test
    public void testBlockTitleText() {
        assertEquals(" + title: this is my text     ", TextUtil.getInstance().blockText(" + ", TITLE, 7, THIS_IS_MY_TEXT, 20));
        assertEquals("title:this is my text     ", TextUtil.getInstance().blockText(null, TITLE, 6, THIS_IS_MY_TEXT, 20));
        assertEquals("tthis is my text     ", TextUtil.getInstance().blockText(null, TITLE, 1, THIS_IS_MY_TEXT, 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, TITLE, 0, THIS_IS_MY_TEXT, 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, TITLE, -1, THIS_IS_MY_TEXT, 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, 20));
        assertEquals("title:this \n"
                    + "      is  \n"
                    + "      my  \n"
                    + "      text", TextUtil.getInstance().blockText(null, TITLE, 6, THIS_IS_MY_TEXT, 4));
        assertEquals(" + title:this \n"
                    + "         is  \n"
                    + "         my  \n"
                    + "         text", TextUtil.getInstance().blockText(" + ", TITLE, 6, THIS_IS_MY_TEXT, 4));
        assertEquals(" + title:th\n"
                    + "         is \n"
                    + "         is \n"
                    + "         my \n"
                    + "         te\n"
                    + "         xt", TextUtil.getInstance().blockText(" + ", TITLE, 6, THIS_IS_MY_TEXT, 2));
        assertEquals(" + title:t\n"
                    + "         h\n"
                    + "         i\n"
                    + "         s \n"
                    + "         i\n"
                    + "         s \n"
                    + "         m\n"
                    + "         y \n"
                    + "         t\n"
                    + "         e\n"
                    + "         x\n"
                    + "         t", TextUtil.getInstance().blockText(" + ", TITLE, 6, THIS_IS_MY_TEXT, 1));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, 0));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, -1));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, (String)null, -1));
    }

    
    /**
     * Test prepare
     */
    @Test
    public void testBlockEmptyContent() {
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, 0));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, -1));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, (String)null, -1));
    }

    
    /**
     * Test prepare
     */
    @Test
    public void testBlockText() {
        assertEquals(" + title: this is my text     ", TextUtil.getInstance().blockText(" + ", TITLE, 7, THIS_IS_MY_TEXT, 20));
        assertEquals(" - title: this is my text with longer and longer messsage ", TextUtil.getInstance().blockText(" - ", TITLE, 7, THIS_IS_MY_TEXT + " with longer and longer messsage", 48));
        assertEquals(" - title: this is my text with longer \n          and longer messsage         ", TextUtil.getInstance().blockText(" - ", TITLE, 7, THIS_IS_MY_TEXT + " with longer and longer messsage", 28));
        assertEquals("title:this is my text     ", TextUtil.getInstance().blockText(null, TITLE, 6, THIS_IS_MY_TEXT, 20));
        assertEquals("tthis is my text     ", TextUtil.getInstance().blockText(null, TITLE, 1, THIS_IS_MY_TEXT, 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, TITLE, 0, THIS_IS_MY_TEXT, 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, TITLE, -1, THIS_IS_MY_TEXT, 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, 20));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, 0));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, -1));
        assertEquals("", TextUtil.getInstance().blockText(null, null, -1, (String)null, -1));
    }


    /**
     * Test block text 2
     */
    @Test
    public void testBlockText2() {
        String cveList = "CVE-2017-12626, CVE-2016-5000, CVE-2017-5644, CVE-2019-12415, CVE-2022-26336, CVE-2014-9527, CVE-2014-3529, CVE-2014-3574";
        assertEquals(" - CVE      CVE-2017-12626, CVE-2016-5000, CVE-2017-5644, CVE-2019-12415,           \n" 
                     + "            CVE-2022-26336, CVE-2014-9527, CVE-2014-3529, CVE-2014-3574             ", 
                     TextUtil.getInstance().blockText(" - ", "CVE", 9, cveList, 72));
    }


    /**
     * Test block text with multiple, leading and non space whitespaces
     */
    @Test
    public void testBlockTextWhitespaces() {
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, null, -1, "this  is   my text", 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, null, -1, " this is my text", 20));
        assertEquals(THIS_IS_MY_TEXT + SPACES5, TextUtil.getInstance().blockText(null, null, -1, "this\nis\tmy\r\ntext", 20));
        assertEquals("title:this \n"
                    + "      is  \n"
                    + "      my  \n"
                    + "      text", TextUtil.getInstance().blockText(null, TITLE, 6, "  this \n is\t\tmy  text", 4));
    }
    

    /**
     * Test that text which exactly fits the max length is not wrapped (L45)
     */
    @Test
    public void testBlockTextExactFit() {
        assertEquals(THIS_IS_MY_TEXT, TextUtil.getInstance().blockText(null, null, -1, THIS_IS_MY_TEXT, THIS_IS_MY_TEXT.length()));
    }


    /**
     * Test that the title of the caller is not modified
     */
    @Test
    public void testBlockTextTitleUnchanged() {
        StringBuilder title = new StringBuilder("ab");
        assertEquals("ab  my", TextUtil.getInstance().blockText(new StringBuilder(), title, 4, new StringBuilder("my"), 2).toString());
        assertEquals("ab", title.toString());
    }


    /**
     * Test that a very long word is split in linear time
     */
    @Test
    public void testBlockTextLongWordPerformance() {
        final int wordLength = 1_000_000;
        final int maxLen = 80;
        final String word = StringUtil.getInstance().newString("x", wordLength);
        String result = assertTimeout(Duration.ofSeconds(5), () -> TextUtil.getInstance().blockText(SPACES5, word, maxLen));

        String[] lines = result.split(TextUtil.NL);
        assertEquals(wordLength / maxLen, lines.length);
        assertEquals(SPACES5.length() + maxLen, lines[0].length());
        assertEquals(SPACES5.length() + maxLen, lines[lines.length - 1].length());
        assertEquals(word, result.replace(TextUtil.NL, "").replace(TextUtil.SPACE, ""));
        assertEquals("abcd" + TextUtil.NL + "efgh" + TextUtil.NL + "ij  ", TextUtil.getInstance().blockText("abcdefghij", 4));
    }


    /**
     * Test string tokenizer
     */
    @Test
    public void test() {
        List<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer("my data\nwith\t .", "\n\r\t\f ", true);
        while (st.hasMoreTokens()) {
            String dat = st.nextToken();
            result.add(dat);
        }
        assertEquals("[my,  , data, \n"
                    + ", with, \t,  , .]", result.toString());

        result = new ArrayList<String>();
        st = new StringTokenizer("my data\nwith\r .");
        while (st.hasMoreTokens()) {
            String dat = st.nextToken();
            result.add(dat);
        }
        assertEquals("[my, data, with, .]", result.toString());
    }
}
