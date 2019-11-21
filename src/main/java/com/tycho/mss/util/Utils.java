package com.tycho.mss.util;

import org.json.simple.JSONObject;

public class Utils {

    public static JSONObject createText(final String text, final String color){
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        jsonObject.put("color", color);
        return jsonObject;
    }
}
