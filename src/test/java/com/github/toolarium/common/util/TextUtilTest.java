/*
 * TextUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
