package com.github.gustavoasevedo.dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.gustavoasevedo.annotations.BaseDBFieldName;
import com.github.gustavoasevedo.constant.ConstantException;
import com.github.gustavoasevedo.exceptions.InvalidTypeException;
import com.github.gustavoasevedo.model.BDCreate;
import com.github.gustavoasevedo.model.BDInsert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

;

/**
 * Created by gustavo.vieira on 04/05/2015.
 */



public class BaseDB extends SQLiteOpenHelper {

    private  String database ; // Database Name
    private int version; // Database Version
    private String table;
    Context context;


    public BaseDB(Context context,String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
        this.context = context;
        this.database = dbName;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS ");
        sb.append(getTable());
        db.execSQL(sb.toString());
        onCreate(db);
    }

    public static void dropDatabase(Context context,String database){

        context.deleteDatabase(database);

    }

    /**
     * Create a table based on received parameters
     *
     * @param db Sqlite Database
     * @param table Name of the table
     * @param coluns Colluns of the table
     * @param pk Table Primary Key
     */
    public void createTable(SQLiteDatabase db, String table, List<BDCreate> coluns, String pk){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(table);
        sb.append(" ");
        sb.append("(");

        for(BDCreate field : coluns) {
            sb.append(field.getFieldName());
            sb.append(" ");
            sb.append(field.getFieldType());
            sb.append(", ");

        }
        if(pk != "" || pk != null) {
            sb.append("PRIMARY KEY(");
            sb.append(pk);
            sb.append("));");
        }
        else{
            sb.append(");");
        }

        db.execSQL(sb.toString());
    }


    public void dropTable(SQLiteDatabase db,String table){

        db.execSQL("DROP TABLE IF EXISTS " + table);
    }


    /**
     * Receive values from another class and insert on a table.
     *
     * @param table Name of the table
     * @param insertObject Generic Object with the values will be put in the table
     * @return
     */
    public Long insert(String table, Object insertObject) throws InvalidTypeException {

        Field[] f = insertObject.getClass().getDeclaredFields();

        ArrayList<BDInsert> inserts = new ArrayList<>();
        BDInsert bdInsert;
        Object o;


        for(int i = 0; i < f.length; i++){
            if (f[i].isAnnotationPresent(BaseDBFieldName.class)){

                f[i].setAccessible(true);
                bdInsert = new BDInsert();
                o = new Object();
                try {
                    o = f[i].get(insertObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                bdInsert.setFieldName(f[i].getAnnotation(BaseDBFieldName.class).value());
                bdInsert.setField(f[i]);
                bdInsert.setFieldValue(o);
                inserts.add(bdInsert);
            }
        }




        ContentValues values = new ContentValues();

        for (BDInsert insert : inserts) {

            //Verify if variable is numeric
            if (insert.getField().getType() == Integer.class ||
                    insert.getField().getType() == Long.class ||
                    insert.getField().getType() == Double.class ||
                    insert.getField().getType() == int.class ){

                values.put(insert.getFieldName(), Integer.valueOf(insert.getFieldValue().toString()));

                //Verify if variable is text
            }else if (insert.getField().getType() == String.class || insert.getField().getType() == char.class) {

                values.put(insert.getFieldName(), insert.getFieldValue().toString());

                //Verify if variable is boolean
            }else if(insert.getField().getType() == Boolean.class ){

                values.put(insert.getFieldName(), Boolean.valueOf(insert.getFieldValue().toString()));
            }else{
                throw new InvalidTypeException(ConstantException.getINVALIDTYPEEXCEPTION());
            }
        }

        Long row = getWritableDatabase().replace(table, null, values);
        Log.d(table, row.toString());
        return row;
    }


    /**
     * Receive table and coluns to return a generic select on the table
     *
     * @param table Name of the table
     * @param colluns Colluns of the table
     * @return
     */
    public Cursor get(String table, ArrayList<String> colluns) {

        String[] collumnames = new String[colluns.size()];
        collumnames = colluns.toArray(collumnames);

        Cursor c = getWritableDatabase().query(table, collumnames, null, null,
                null, null, null);

        return c;
    }


    /**
     * Receive table, coluns, and one or more arguments and run a filtered select on table.
     *
     * @param table Name of the table
     * @param colluns Colluns of the table
     * @param fields Name of the fields who will be the filter
     * @param args Values of the filter
     * @return
     */
    public Cursor getWhere(String table, ArrayList<String> colluns, ArrayList<String> fields,String[] args) {

        String[] collumnames = new String[colluns.size()];
        collumnames = colluns.toArray(collumnames);

        StringBuilder sb = new StringBuilder();

        for(String field: fields){
            sb.append(field);
            sb.append("= ?,");
        }
        String Sfields = sb.toString();
        String query = Sfields.substring(0, Sfields.length() - 1);

        Cursor c = getWritableDatabase().query(table, collumnames, query, args,
                null, null, null);

        return c;
    }

    public Boolean DeleteFieldsTable(String table){

        Boolean b = getWritableDatabase().delete(table,null,null) > 0;

        return b;
    }

    public Boolean DeleteFieldsTableWhere(String table,String field,String[] args){

        Boolean b = getWritableDatabase().delete(table,field,args) > 0;

        return b;
    }




    public Cursor getRawQuery(String query,String[] args) {

        Cursor c = getWritableDatabase().rawQuery(query,args);

        return c;
    }


    public int getVersion() {
        return version;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }


}
