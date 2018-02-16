package com.dss.sdatabase.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.dss.sdatabase.annotations.BaseDBFieldName
import com.dss.sdatabase.constant.ConstantException
import com.dss.sdatabase.exceptions.InvalidTypeException
import com.dss.sdatabase.model.BDCreate
import com.dss.sdatabase.model.BDInsert
import java.util.*

@Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
/**
 * Created by gustavo.vieira on 04/05/2015.
 */


class BaseDB(internal var context: Context?, private val database: String // Database Name
             , dbVersion: Int) : SQLiteOpenHelper(context, database, null, dbVersion) {
    var version: Int = 0 // Database Version
    var table: String = ""

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sb = StringBuilder()
        sb.append("DROP TABLE IF EXISTS ")
        sb.append(table)
        db.execSQL(sb.toString())
        onCreate(db)
    }

    /**
     * Create a table based on received parameters

     * @param db Sqlite Database
     * *
     * @param table Name of the table
     * *
     * @param coluns Colluns of the table
     * *
     * @param pk Table Primary Key
     */
    fun createTable(db: SQLiteDatabase, table: String, coluns: List<BDCreate>, pk: String) {
        val sb = StringBuilder()
        sb.append("CREATE TABLE IF NOT EXISTS ")
        sb.append(table)
        sb.append(" ")
        sb.append("(")

        var size = coluns.size

        for (field in coluns) {
            size--

            sb.append(field.fieldName)
            sb.append(" ")
            sb.append(field.fieldType)


            if (size > 0) {
                sb.append(", ")
            }
        }

        if (pk != null) {
            if (pk != "") {
                sb.append(", PRIMARY KEY(")
                sb.append(pk)
                sb.append("));")
            } else {
                sb.append(");")
            }
        } else {
            sb.append(");")
        }

        db.execSQL(sb.toString())
    }


    fun dropTable(db: SQLiteDatabase, table: String) {

        db.execSQL("DROP TABLE IF EXISTS " + table)
    }


    /**
     * Receive values from another class and insert on a table.

     * @param table Name of the table
     * *
     * @param insertObject Generic Object with the values will be put in the table
     * *
     * @return
     */
    @Throws(InvalidTypeException::class)
    fun insert(table: String, insertObject: Any): Long {

        val f = insertObject.javaClass.getDeclaredFields()

        val inserts = ArrayList<BDInsert>()
        var bdInsert: BDInsert
        var o: Any


        for (i in f.indices) {
            if (f[i].isAnnotationPresent(BaseDBFieldName::class.java)) {

                f[i].setAccessible(true)
                bdInsert = BDInsert()
                o = Any()
                try {
                    o = f[i].get(insertObject)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

                bdInsert.fieldName = f[i].getAnnotation(BaseDBFieldName::class.java).value
                bdInsert.field = f[i]
                bdInsert.fieldValue = o
                inserts.add(bdInsert)
            }
        }


        val values = ContentValues()

        for (insert in inserts) {

            //Verify if variable is numeric
            if (insert.field?.type == Int::class.java ||
                    insert.field?.type == Long::class.java ||
                    insert.field?.type == Double::class.java ||
                    insert.field?.type == Int::class.javaPrimitiveType) {

                values.put(insert.fieldName, Integer.valueOf(insert.fieldValue.toString()))

                //Verify if variable is text
            } else if (insert.field?.type == String::class.java || insert.field?.type == Char::class.javaPrimitiveType) {

                values.put(insert.fieldName, insert.fieldValue.toString())

                //Verify if variable is boolean
            } else if (insert.field?.type!!.canonicalName == Boolean::class.javaObjectType.canonicalName) {

                values.put(insert.fieldName, java.lang.Boolean.valueOf(insert.fieldValue.toString()))
            } else {
                throw InvalidTypeException(ConstantException.invalidtypeexception)
            }
        }

        val row = writableDatabase.replace(table, null, values)
        Log.d(table, row.toString())
        return row
    }


    /**
     * Receive table and coluns to return a generic select on the table

     * @param table Name of the table
     * *
     * @param colluns Colluns of the table
     * *
     * @return
     */
    operator fun get(table: String, colluns: ArrayList<String>): Cursor {

        var collumnames = arrayOfNulls<String>(colluns.size)
        collumnames = colluns.toTypedArray()

        val c = writableDatabase.query(table, collumnames, null, null, null, null, null)

        return c
    }


    /**
     * Receive table, coluns, and one or more arguments and run a filtered select on table.

     * @param table Name of the table
     * *
     * @param colluns Colluns of the table
     * *
     * @param fields Name of the fields who will be the filter
     * *
     * @param args Values of the filter
     * *
     * @return
     */
    fun getWhere(table: String, colluns: ArrayList<String>, fields: ArrayList<String>, args: Array<String>): Cursor {

        var collumnames = colluns.toTypedArray()

        val sb = StringBuilder()

        for (field in fields) {
            sb.append(field)
            sb.append("= ?,")
        }

        val Sfields = sb.toString()
        val query = Sfields.substring(0, Sfields.length - 1)

        val c = writableDatabase.query(table, collumnames, query, args, null, null, null)

        return c
    }

    fun DeleteFieldsTable(table: String): Boolean {

        val b = writableDatabase.delete(table, null, null) > 0

        return b
    }

    fun DeleteFieldsTableWhere(table: String, field: String, args: Array<String>): Boolean {

        val b = writableDatabase.delete(table, field, args) > 0

        return b
    }


    fun getRawQuery(query: String, args: Array<String>): Cursor {

        val c = writableDatabase.rawQuery(query, args)

        return c
    }

    override fun onCreate(db: SQLiteDatabase) {

    }

    companion object {

        fun dropDatabase(context: Context, database: String) {

            context.deleteDatabase(database)

        }
    }


}