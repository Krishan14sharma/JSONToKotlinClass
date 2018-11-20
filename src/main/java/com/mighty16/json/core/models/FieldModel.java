package com.mighty16.json.core.models;

public class FieldModel {

    public String jsonName;
    public String name; // name of the field in the code
    public String type; // type of the fields
    public boolean optional;
    public String originalValue;  // whether its a object or an array
    public boolean enabled;
    public boolean mutable;
    public String defaultValue;

    public FieldModel(String jsonName, String name, String type, String originalValue) {
        this.jsonName = jsonName;
        this.name = name;
        this.type = type;
        this.originalValue = originalValue;
        this.enabled = true;
        this.optional = false;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
