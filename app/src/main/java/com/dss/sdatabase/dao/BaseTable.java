package com.dss.sdatabase.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dss.sdatabase.annotations.BaseDBFieldName;
import com.dss.sdatabase.annotations.BaseDBMethodSetName;
import com.dss.sdatabase.annotations.BaseDBPrimaryKey;
import com.dss.sdatabase.annotations.BaseDBType;
import com.dss.sdatabase.constant.ConstantException;
import com.dss.sdatabase.exceptions.InvalidTypeException;
import com.dss.sdatabase.model.BDCreate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gustavo.vieira on 04/05/2015.
 */
public class BaseTable {

    public BaseDB baseDB;
    private SQLiteDatabase db = null;
    private Context context;
    private String DBname;
    private int DBversion;

    private String tableName;
    private ArrayList<String> fields;
    private ArrayList<String> types;
    private String pk;


    protected BaseTable(Context context, Class modelClass,String DBname, int DBversion){
        super();
        this.DBname = DBname;
        this.DBversion = DBversion;
        baseDB = new BaseDB(context, DBname, DBversion);
        this.context = context;
        Field[] f = modelClass.getDeclaredFields();

        tableName = modelClass.getSimpleName();

        fields = new ArrayList<>();
        types = new ArrayList<>();

        for(int i = 0; i < f.length; i++){
            f[i].setAccessible(true);
            if (f[i].isAnnotationPresent(BaseDBFieldName.class)){
                fields.add(f[i].getAnnotation(BaseDBFieldName.class).value());
                types.add(f[i].getAnnotation(BaseDBType.class).value());
            }
            if(f[i].isAnnotationPresent(BaseDBPrimaryKey.class)){
                pk = f[i].getAnnotation(BaseDBFieldName.class).value();
            }
        }

    }


    protected void createTable(){
        ArrayList<BDCreate> bdCreates = new ArrayList<>();
        BDCreate bdCreate;

        for(int i = 0; i < fields.size(); i++) {
            bdCreate = new BDCreate();
            bdCreate.setFieldName(fields.get(i));
            bdCreate.setFieldType(types.get(i));

            bdCreates.add(bdCreate);

        }

        openCoonection();
        baseDB.createTable(db, tableName, bdCreates, pk);
        closeConnection();
    }

    private void openCoonection(){
        this.baseDB = new BaseDB(context, DBname, DBversion);
        baseDB.setTable(tableName);
        db = baseDB.getWritableDatabase();
    }

    private void closeConnection() {
        try {
            if (null != db) {
                db.close();
                db = null;
                baseDB.close();
            }

        } catch (Exception e) {
            Log.e("Erro:", "Erro ao fechar conexoes");
        }
    }


    protected void insert(ArrayList<Object> receiveObject){

        openCoonection();
        Object insertObject;
        for(Object object : receiveObject) {
            insertObject = object;
            try {
                baseDB.insert(tableName, insertObject);
            } catch (InvalidTypeException invalidTypeException) {
                invalidTypeException.printStackTrace();
            }
        }
        closeConnection();

    }

    protected ArrayList<Object> selectList(Class aClass)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException, InvalidTypeException {

        Class<?> clazz = Class.forName(aClass.getName());
        Constructor<?> ctor = clazz.getConstructor();
        ctor.setAccessible(true);

        ArrayList<Object> list = new ArrayList<>();
        Object object;


        openCoonection();

        Cursor c = baseDB.get(tableName,fields);

        try {
            while (c.moveToNext()) {
                object = ctor.newInstance();

                Method[] methods = aClass.getMethods();

                for(int i = 0;i < methods.length;i++) {
                    methods[i].setAccessible(true);


                    if (methods[i].isAnnotationPresent(BaseDBMethodSetName.class)) {

                            Type parameterizedType = (Type) methods[i].getGenericParameterTypes()[0];

                        if (parameterizedType == Integer.class||
                                parameterizedType == int.class) {

                            methods[i].invoke(object,c.getInt(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));


                        } else if (parameterizedType == Long.class) {

                            methods[i].invoke(object,c.getLong(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else if (parameterizedType == Double.class) {

                            methods[i].invoke(object,c.getDouble(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                            //Verify if variable is text
                        }else if (parameterizedType == String.class ||
                                parameterizedType == char.class) {

                            methods[i].invoke(object,c.getString(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else{
                            throw new InvalidTypeException(ConstantException.getINVALIDTYPEEXCEPTION());
                        }
                    }
                }

                list.add(object);

            }
        } finally {
            c.close();
        }
        return list;

    }

    protected Object selectWhere(Class aClass,ArrayList<String> field,String[] values)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException, InvalidTypeException{


        Class<?> clazz = Class.forName(aClass.getName());
        Constructor<?> ctor = clazz.getConstructor();
        ctor.setAccessible(true);

        Object object = ctor.newInstance();

        Cursor c = baseDB.getWhere(tableName, fields, field, values);

        try {
            if (c.moveToNext()) {
                Method[] methods = aClass.getMethods();

                for(int i = 0;i < methods.length;i++) {
                    methods[i].setAccessible(true);

                    if (methods[i].isAnnotationPresent(BaseDBMethodSetName.class)) {

                        Type parameterizedType = (Type) methods[i].getGenericParameterTypes()[0];

                        if (parameterizedType == Integer.class||
                                parameterizedType == int.class) {

                            methods[i].invoke(object,c.getInt(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));


                        } else if (parameterizedType == Long.class) {

                            methods[i].invoke(object,c.getLong(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else if (parameterizedType == Double.class) {

                            methods[i].invoke(object,c.getDouble(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                            //Verify if variable is text
                        }else if (parameterizedType == String.class ||
                                parameterizedType == char.class) {

                            methods[i].invoke(object,c.getString(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else{
                            throw new InvalidTypeException(ConstantException.getINVALIDTYPEEXCEPTION());
                        }
                    }
                }
            }
        } finally {
            c.close();
        }

        return object;
    }

    protected ArrayList<Object> selectListWhere(Class aClass,ArrayList<String>field,String[] values)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException, InvalidTypeException{

        Class<?> clazz = Class.forName(aClass.getName());
        Constructor<?> ctor = clazz.getConstructor();
        ctor.setAccessible(true);

        ArrayList<Object> list = new ArrayList<>();
        Object object;

        openCoonection();
        Cursor c = baseDB.getWhere(tableName, fields, field, values);

        try {
            while (c.moveToNext()) {
                object = ctor.newInstance();

                Method[] methods = aClass.getMethods();

                for(int i = 0;i < methods.length;i++) {
                    methods[i].setAccessible(true);


                    if (methods[i].isAnnotationPresent(BaseDBMethodSetName.class)) {

                        Type parameterizedType = (Type) methods[i].getGenericParameterTypes()[0];

                        if (parameterizedType == Integer.class||
                                parameterizedType == int.class) {

                            methods[i].invoke(object,c.getInt(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));


                        } else if (parameterizedType == Long.class) {

                            methods[i].invoke(object,c.getLong(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else if (parameterizedType == Double.class) {

                            methods[i].invoke(object,c.getDouble(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                            //Verify if variable is text
                        }else if (parameterizedType == String.class ||
                                parameterizedType == char.class) {

                            methods[i].invoke(object,c.getString(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else{
                            throw new InvalidTypeException(ConstantException.getINVALIDTYPEEXCEPTION());
                        }
                    }
                }

                list.add(object);

            }
        } finally {
            c.close();
        }
        return list;
    }


    protected ArrayList<Object> selectRawQuery(Class aClass, String query, String[] values)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException, InvalidTypeException{


        Class<?> clazz = Class.forName(aClass.getName());
        Constructor<?> ctor = clazz.getConstructor();
        ctor.setAccessible(true);

        ArrayList<Object> list = new ArrayList<>();

        Object object = ctor.newInstance();

        Cursor c = baseDB.getRawQuery(query,values);

        try {
            while (c.moveToNext()) {
                object = ctor.newInstance();

                Method[] methods = aClass.getMethods();

                for(int i = 0;i < methods.length;i++) {
                    methods[i].setAccessible(true);

                    if (methods[i].isAnnotationPresent(BaseDBMethodSetName.class)) {

                        Type parameterizedType = (Type) methods[i].getGenericParameterTypes()[0];

                        if (parameterizedType == Integer.class||
                                parameterizedType == int.class) {

                            methods[i].invoke(object,c.getInt(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));


                        } else if (parameterizedType == Long.class) {

                            methods[i].invoke(object,c.getLong(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else if (parameterizedType == Double.class) {

                            methods[i].invoke(object,c.getDouble(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                            //Verify if variable is text
                        }else if (parameterizedType == String.class ||
                                parameterizedType == char.class) {

                            methods[i].invoke(object,c.getString(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else{
                            throw new InvalidTypeException(ConstantException.getINVALIDTYPEEXCEPTION());
                        }
                    }
                }
                list.add(object);

            }
        } finally {
            c.close();
        }

        return list;
    }

    protected ArrayList<Object> selectListRawQuery(Class aClass,String query,String[] values)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException, InvalidTypeException{

        Class<?> clazz = Class.forName(aClass.getName());
        Constructor<?> ctor = clazz.getConstructor();
        ctor.setAccessible(true);

        ArrayList<Object> list = new ArrayList<>();
        Object object;

        openCoonection();
        Cursor c = baseDB.getRawQuery(query,values);

        try {
            while (c.moveToNext()) {
                object = ctor.newInstance();

                Method[] methods = aClass.getMethods();

                for(int i = 0;i < methods.length;i++) {
                    methods[i].setAccessible(true);


                    if (methods[i].isAnnotationPresent(BaseDBMethodSetName.class)) {

                        Type parameterizedType = (Type) methods[i].getGenericParameterTypes()[0];

                        if (parameterizedType == Integer.class||
                                parameterizedType == int.class) {

                            methods[i].invoke(object,c.getInt(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));


                        } else if (parameterizedType == Long.class) {

                            methods[i].invoke(object,c.getLong(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else if (parameterizedType == Double.class) {

                            methods[i].invoke(object,c.getDouble(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                            //Verify if variable is text
                        }else if (parameterizedType == String.class ||
                                parameterizedType == char.class) {

                            methods[i].invoke(object,c.getString(c.getColumnIndex(methods[i].getAnnotation(BaseDBMethodSetName.class).value())));

                        }else{
                            throw new InvalidTypeException(ConstantException.getINVALIDTYPEEXCEPTION());
                        }
                    }
                }

                list.add(object);

            }
        } finally {
            c.close();
        }
        return list;
    }

}
