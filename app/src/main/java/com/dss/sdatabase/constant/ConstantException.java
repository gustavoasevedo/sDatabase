package com.github.gustavoasevedo.constant;

/**
 * Created by digipronto on 01/03/16.
 */
public abstract class ConstantException {

    private static final String INVALIDTYPEEXCEPTION = "Invalid data to Input in SQLite database";

    public static String getINVALIDTYPEEXCEPTION() {
        return INVALIDTYPEEXCEPTION;
    }
}
