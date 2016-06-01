package com.dss.sdatabase.model;

import java.lang.reflect.Field;

/**
 * Created by gustavo.vieira on 04/05/2015.
 */
public class BDInsert {

    private Field field;
    private String fieldName;
    private Object fieldValue;


    public BDInsert(Field field,String fieldName, Object fieldValue) {
        this.setField(field);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }


    public BDInsert() {

    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
