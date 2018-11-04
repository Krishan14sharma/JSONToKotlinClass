package com.mighty16.json.resolver;

import org.apache.commons.lang.StringUtils;

/**
 * Created by krishan on 11/4/18.
 */
public class KotlinDataClassResolver extends KotlinResolver {

    public static final String DATA_MODEL_POSTFIX = "Api";

    @Override
    public String getClassName(String jsonKey) {
        String result = toCamelCase(jsonKey) + DATA_MODEL_POSTFIX;
        return StringUtils.capitalize(result);
    }

    @Override
    public String getArrayType(String type) {
        String result = !isCustomType(type.toUpperCase()) ? toCamelCase(type) : toCamelCase(type) + DATA_MODEL_POSTFIX;
        return "List<" + result + ">?";
    }

    public static boolean isCustomType(String type) {
        String arr[] = {"INT", "LONG", "STRING", "BOOLEAN", "DOUBLE"};
        for (String anArr : arr) {
            if (type.toUpperCase().equals(anArr)) {
                return false;
            }
        }
        return true;
    }

}
