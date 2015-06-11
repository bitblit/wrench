package com.erigir.wrench;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Simple class to swallow impossible UnknownEncodingExceptions
 */
public class UTF8Encoder {

    public static final String UTF8 = "UTF-8";

    /**
     * Encode the passed string into UTF8 URL
     */
    public static String encode(String input) {
        try {
            return (input == null) ? null : URLEncoder.encode(input, UTF8);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Cant happen", uee);
        }
    }

    /**
     * Decode the passed string as a  UTF8 URL
     */
    public static String decode(String input) {
        try {
            return (input == null) ? null : URLDecoder.decode(input, UTF8);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Cant happen", uee);
        }
    }

}
