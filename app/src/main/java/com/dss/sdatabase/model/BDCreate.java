package com.dss.sdatabase.model;

/**
 * Created by gustavo.vieira on 04/05/2015.
 */
public class BDCreate {

    private String fieldName;
    private String fieldType;

    public BDCreate() {

    }

    public BDCreate(String fieldName, String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
