package com.erigir.wrench.simpleincludes;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by cweiss on 9/2/15.
 */
public class TestSimpleIncludesPatternMapSource {

    @Test
    public void testMatching()
    {
        LinkedHashMap<Pattern, String> testMap = new LinkedHashMap<>();
        testMap.put(Pattern.compile("e.*"),"e-start");
        testMap.put(Pattern.compile(".*f"),"f-end");

        SimpleIncludesPatternMapSource bean = new SimpleIncludesPatternMapSource(
                testMap
        );

        assertEquals("e-start", bean.findContent("e pluribus unum"));
        assertEquals("f-end", bean.findContent("wheres the beef"));
        assertEquals("e-start", bean.findContent("eeek wheres the beef"));
        assertEquals("", bean.findContent("four score and seven years ago"));



    }


}
