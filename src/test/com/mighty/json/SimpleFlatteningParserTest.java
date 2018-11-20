package com.mighty.json;

import com.mighty16.json.core.parser.SimpleFlatteningParser;
import com.mighty16.json.resolver.KotlinResolver;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by krishan on 11/17/18.
 */
public class SimpleFlatteningParserTest {

    SimpleFlatteningParser simpleFlatteningParser;

    @Before
    public void setUp() throws Exception {
        simpleFlatteningParser = new SimpleFlatteningParser(new KotlinResolver());
    }

    @Test
    public void testFlatteningParserOutput() {
        String input = "{\n" +
                "  \"Id\": \"great\",\n" +
                "  \"menu\": {\n" +
                "    \"popup\": \"great\",\n" +
                "    \"id\": \"file\",\n" +
                "    \"value\": \"File\"\n" +
                "  },\n" +
                "  \"arrs\": [\n" +
                "    {\n" +
                "      \"id\": \"\",\n" +
                "      \"onclick\": [\n" +
                "        \"de\"\n" +
                "      ],\n" +
                "      \"arrs\": [\n" +
                "        \"string\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"Imp\": \"great\",\n" +
                "  \"design\": \"great\"\n" +
                "}";
        JSONObject json = new JSONObject(input);
        simpleFlatteningParser.parse(json, "A");
        System.out.println(Arrays.toString(simpleFlatteningParser.getClasses().toArray()));
        System.out.println(Arrays.toString(simpleFlatteningParser.getClasses().get(0).fields.toArray()));
        System.out.println(Arrays.toString(simpleFlatteningParser.getClasses().get(1).fields.toArray()));

    }

    @After
    public void tearDown() throws Exception {
    }
}