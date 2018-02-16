package com.dss.sdatabase.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.dss.sdatabase.annotations.BaseDBFieldName
import com.dss.sdatabase.annotations.BaseDBPrimaryKey
import com.dss.sdatabase.annotations.BaseDBType
import com.dss.sdatabase.constant.ConstantException
import com.dss.sdatabase.exceptions.InvalidTypeException
import com.dss.sdatabase.model.BDCreate
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created by gustavo.vieira on 04/05/2015.
 */
open class BaseTable {

    open var context: Context? = null
    open var DBname: String = ""
    open var DBversion: Int = 0
    open var baseDB: BaseDB? = null
    private var db: SQLiteDatabase? = null
    private var aClass: Class<*>
    private val tableName: String
    private val fields: ArrayList<String>
    private val types: ArrayList<String>
    private var pk: String = ""

    protected constructor(context: Context, modelClass: Class<*>, DBname: String, DBversion: Int) {
        this.context = context
        this.DBname = DBname
        this.DBversion = DBversion
        baseDB = BaseDB(context, DBname, DBversion)

        val f = modelClass.declaredFields
        aClass = modelClass
        tableName = modelClass.simpleName
        fields = ArrayList<String>()
        types = ArrayList<String>()
        for (i in f.indices) {
            f[i].isAccessible = true
            if (f[i].isAnnotationPresent(BaseDBFieldName::class.java)) {
                fields.add(f[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)
                types.add(f[i].getAnnotation<BaseDBType>(BaseDBType::class.java).value)
            }
            if (f[i].isAnnotationPresent(BaseDBPrimaryKey::class.java)) {
                pk = f[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value
            }
        }
    }

    companion object {
        init {

        }
    }

    protected fun createTable() {
        val bdCreates = ArrayList<BDCreate>()
        var bdCreate: BDCreate

        for (i in fields.indices) {
            bdCreate = BDCreate()
            bdCreate.fieldName = fields[i]
            bdCreate.fieldType = types[i]

            bdCreates.add(bdCreate)

        }

        openCoonection()
        baseDB?.createTable(db as SQLiteDatabase, tableName, bdCreates, pk)
        closeConnection()
    }

    private fun openCoonection() {
        this.baseDB = BaseDB(context, DBname, DBversion)
        (baseDB as BaseDB).table = tableName
        db = (baseDB as BaseDB).writableDatabase
    }

    private fun closeConnection() {
        try {
            if (null != db) {
                (db as SQLiteDatabase).close()
                baseDB?.close()
            }

        } catch (e: Exception) {
            Log.e("Erro:", "Erro ao fechar conexoes")
        }

    }


    protected fun insert(receiveObject: ArrayList<Any>) {

        openCoonection()
        var insertObject: Any
        for (`object` in receiveObject) {
            insertObject = `object`
            try {
                baseDB?.insert(tableName, insertObject)
            } catch (invalidTypeException: InvalidTypeException) {
                invalidTypeException.printStackTrace()
            }

        }
        closeConnection()

    }

    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, InvalidTypeException::class)
    protected fun selectList(): ArrayList<Any> {

        val clazz = Class.forName(aClass.canonicalName)
        val ctor = clazz.getConstructor()
        ctor.isAccessible = true

        val list = ArrayList<Any>()
        var `object`: Any


        openCoonection()

        val c = baseDB?.get(tableName, fields)

        try {
            while (c?.moveToNext() as Boolean) {
                `object` = ctor.newInstance()

                val variables = aClass.declaredFields

                for (i in variables.indices) {
                    variables[i].isAccessible = true


                    if (variables[i].isAnnotationPresent(BaseDBFieldName::class.java)) {

                        val parameterizedType = variables[i].genericType

                        if (parameterizedType === Int::class.java || parameterizedType === Int::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))


                        } else if (parameterizedType === Long::class.java) {

                            variables[i].set(`object`, c.getLong(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else if (parameterizedType === Double::class.java) {

                            variables[i].set(`object`, c.getDouble(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                            //Verify if variable is text
                        } else if (parameterizedType === String::class.java || parameterizedType === Char::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getString(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        }else if(parameterizedType.toString() == Boolean::class.javaObjectType.toString()) {
                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)) > 0)

                        } else {
                            throw InvalidTypeException(ConstantException.invalidtypeexception)
                        }
                    }
                }

                list.add(`object`)

            }
        } finally {
            c?.close()
        }

        closeConnection()

        return list

    }

    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, InvalidTypeException::class)
    protected fun selectWhere(field: ArrayList<String>, values: Array<String>): Any {


        val clazz = Class.forName(aClass.canonicalName)
        val ctor = clazz.getConstructor()
        ctor.isAccessible = true

        val `object` = ctor.newInstance()

        openCoonection()
        val c = baseDB?.getWhere(tableName, fields, field, values)

        try {
            if (c?.moveToNext() as Boolean) {
                val variables = aClass.declaredFields

                for (i in variables.indices) {
                    variables[i].isAccessible = true

                    if (variables[i].isAnnotationPresent(BaseDBFieldName::class.java)) {
                        var parameterizedType = variables[i].genericType

                        if (parameterizedType === Int::class.java || parameterizedType === Int::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else if (parameterizedType === Long::class.java) {

                            variables[i].set(`object`, c.getLong(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else if (parameterizedType === Double::class.java) {

                            variables[i].set(`object`, c.getDouble(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                            //Verify if variable is text
                        } else if (parameterizedType === String::class.java || parameterizedType === Char::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getString(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        }else if(parameterizedType.toString() == Boolean::class.javaObjectType.toString()) {
                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)) > 0)

                        } else {
                            throw InvalidTypeException(ConstantException.invalidtypeexception)
                        }
                    }
                }
            }
        } finally {
            c?.close()
        }

        closeConnection()

        return `object`
    }

    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, InvalidTypeException::class)
    protected fun selectListWhere(field: ArrayList<String>, values: Array<String>): ArrayList<Any> {

        val clazz = Class.forName(aClass.canonicalName)
        val ctor = clazz.getConstructor()
        ctor.isAccessible = true

        val list = ArrayList<Any>()
        var `object`: Any

        openCoonection()
        val c = baseDB?.getWhere(tableName, fields, field, values)

        try {
            while (c?.moveToNext() as Boolean) {
                `object` = ctor.newInstance()

                val variables = aClass.declaredFields

                for (i in variables.indices) {
                    variables[i].isAccessible = true


                    if (variables[i].isAnnotationPresent(BaseDBFieldName::class.java)) {

                        val parameterizedType = variables[i].genericType

                        if (parameterizedType === Int::class.java || parameterizedType === Int::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))


                        } else if (parameterizedType === Long::class.java) {

                            variables[i].set(`object`, c.getLong(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else if (parameterizedType === Double::class.java) {

                            variables[i].set(`object`, c.getDouble(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                            //Verify if variable is text
                        } else if (parameterizedType === String::class.java || parameterizedType === Char::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getString(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        }else if(parameterizedType.toString() == Boolean::class.javaObjectType.toString()) {
                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)) > 0)

                        } else {
                            throw InvalidTypeException(ConstantException.invalidtypeexception)
                        }
                    }
                }

                list.add(`object`)

            }
        } finally {
            c?.close()
        }



        closeConnection()
        return list
    }


    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, InvalidTypeException::class)
    protected fun selectRawQuery(query: String, values: Array<String>): ArrayList<Any> {


        val clazz = Class.forName(aClass.canonicalName)
        val ctor = clazz.getConstructor()
        ctor.isAccessible = true

        val list = ArrayList<Any>()

        var `object`: Any

        openCoonection()
        val c = baseDB?.getRawQuery(query, values)

        try {
            while (c?.moveToNext() as Boolean) {
                `object` = ctor.newInstance()
                val variables = aClass.declaredFields

                for (i in variables.indices) {
                    variables[i].isAccessible = true

                    if (variables[i].isAnnotationPresent(BaseDBFieldName::class.java)) {

                        val parameterizedType = variables[i].genericType

                        if (parameterizedType === Int::class.java || parameterizedType === Int::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))


                        } else if (parameterizedType === Long::class.java) {

                            variables[i].set(`object`, c.getLong(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else if (parameterizedType === Double::class.java) {

                            variables[i].set(`object`, c.getDouble(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                            //Verify if variable is text
                        } else if (parameterizedType === String::class.java || parameterizedType === Char::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getString(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        }else if(parameterizedType.toString() == Boolean::class.javaObjectType.toString()) {
                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)) > 0)

                        } else {
                            throw InvalidTypeException(ConstantException.invalidtypeexception)
                        }
                    }
                }
                list.add(`object`)

            }
        } finally {
            c?.close()
        }

        closeConnection()

        return list
    }

    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, InvalidTypeException::class)
    protected fun selectListRawQuery(query: String, values: Array<String>): ArrayList<Any> {

        val clazz = Class.forName(aClass.canonicalName)
        val ctor = clazz.getConstructor()
        ctor.isAccessible = true

        val list = ArrayList<Any>()
        var `object`: Any

        openCoonection()
        val c = baseDB?.getRawQuery(query, values)

        try {
            while (c?.moveToNext() as Boolean) {
                `object` = ctor.newInstance()

                val variables = aClass.declaredFields

                for (i in variables.indices) {
                    variables[i].isAccessible = true


                    if (variables[i].isAnnotationPresent(BaseDBFieldName::class.java)) {

                        val parameterizedType = variables[i].genericType

                        if (parameterizedType === Int::class.java || parameterizedType === Int::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getInt(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))


                        } else if (parameterizedType === Long::class.java) {

                            variables[i].set(`object`, c.getLong(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else if (parameterizedType === Double::class.java) {

                            variables[i].set(`object`, c.getDouble(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                            //Verify if variable is text
                        } else if (parameterizedType === String::class.java || parameterizedType === Char::class.javaPrimitiveType) {

                            variables[i].set(`object`, c.getString(c.getColumnIndex(variables[i].getAnnotation<BaseDBFieldName>(BaseDBFieldName::class.java).value)))

                        } else {
                            throw InvalidTypeException(ConstantException.invalidtypeexception)
                        }
                    }
                }

                list.add(`object`)

            }
        } finally {
            c?.close()
        }

        closeConnection()
        return list
    }


    protected fun deleteAll(): Boolean {

        val b = baseDB?.DeleteFieldsTable(tableName)

        return b as Boolean
    }

    protected fun deleteAll(field: String, values: Array<String>): Boolean {

        val b = baseDB?.DeleteFieldsTableWhere(tableName, field, values)

        return b as Boolean
    }


    protected fun dropTable() {
        baseDB?.dropTable(db as SQLiteDatabase, tableName)
    }
}
