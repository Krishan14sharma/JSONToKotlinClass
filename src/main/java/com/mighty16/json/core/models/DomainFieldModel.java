package com.mighty16.json.core.models;

/**
 * Created by krishan on 12/2/18.
 */
public class DomainFieldModel extends FieldModel {

    private String accessPath;

    public DomainFieldModel(String jsonName, String name, String type, String originalValue, String accessPath) {
        super(jsonName, name, type, originalValue);
        this.accessPath = accessPath;
    }

    public String getAccessPath() {
        return accessPath;
    }

    public void setAccessPath(String accessPath) {
        this.accessPath = accessPath;
    }

    @Override
    public String toString() {
        return accessPath;
    }
}
