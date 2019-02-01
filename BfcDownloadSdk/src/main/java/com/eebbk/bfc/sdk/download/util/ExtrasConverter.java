package com.eebbk.bfc.sdk.download.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-06 15:56
 * Email: jacklulu29@gmail.com
 */
public class ExtrasConverter {

    private ExtrasConverter(){
        // private construct
    }

    private static final char TAG_B = '{';
    private static final char TAG_E = '}';
    private static final char TAG_S = ':';


    private static final String CODE_FORMAT = "UTF-8";
    private static final Pattern ENCODE_PATTERN = Pattern.compile("[^(){}:]+");

    public static String encode(HashMap<String, String> hashMap) throws UnsupportedEncodingException {
        if (hashMap == null) {
            throw new IllegalArgumentException(
                    "The hashMap is null can't be encode!");
        }
        StringBuilder ret = new StringBuilder();
        @SuppressWarnings("rawtypes")
        Iterator iter = hashMap.entrySet().iterator();
        ret.append('(');
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();

            ret.append(encodeFormat(key, val));
        }
        ret.append(')');

        return ret.toString();
    }


    public static String encodeFormat(String key, String val) throws UnsupportedEncodingException {
        StringBuilder ret = new StringBuilder();
        key = encodeCheck(key);
        val = encodeCheck(val);
        ret.append(TAG_B);
        ret.append(URLEncoder.encode(key, CODE_FORMAT));
        ret.append(TAG_S);
        ret.append(URLEncoder.encode(val, CODE_FORMAT));
        ret.append(TAG_E);
        return ret.toString();
    }

    private static String encodeCheck(String str) {
        String ret = null;
        if (str == null) {
            ret = "#null#";
        } else if (str.equals("")) {
            ret = "#space#";
        } else {
            ret = str;
        }
        return ret;
    }

    private static String decodeCheck(String str) {
        String ret = null;
        switch (str) {
            case "#null#":
                ret = null;
                break;
            case "#space#":
                ret = "";
                break;
            default:
                ret = str;
                break;
        }
        return ret;
    }


    public static HashMap<String, String> decode(String strEncode)
            throws UnsupportedEncodingException {
        if (strEncode == null) {
            throw new IllegalArgumentException(
                    "The String is null can't be decode!");
        }
        HashMap<String, String> map = new HashMap<>();
        try {
            String key = null;
            String value = null;
            Matcher m = ENCODE_PATTERN.matcher(strEncode);
            while (m.find()) {
                if (key == null) {
                    key = URLDecoder.decode(m.group(), CODE_FORMAT);
                } else {
                    value = URLDecoder.decode(m.group(), CODE_FORMAT);
                    key = decodeCheck(key);
                    value = decodeCheck(value);
                    map.put(key, value);
                    key = null;
                    value = null;
                }
            }

        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
        return map;
    }

    public static String extrasToString(HashMap<String, String> extras){
        if(extras == null || extras.isEmpty()){
            return "";
        }
        StringBuilder str = new StringBuilder();
        Iterator<?> iter = extras.entrySet().iterator();
        str.append("(");
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            str.append(key);
            str.append(":");
            str.append(val);
            if(iter.hasNext()){
                str.append(",");
            }
        }
        str.append(")");

        return str.toString();
    }

}

