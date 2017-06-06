# sDatabase
Android Sqlite API

This API is focused on improving the development of Sqlite databases on a native Android App.

To put it on your project, just follow the instructions:

Add to your gradle repositories:

```
    repositories {
        jcenter()
        ...
    }
```


Add to your gradle dependencies:

```
dependencies {
        compile 'com.dss.sDatabase:sDatabase:1.1.2'
        ...
    }
```

There is a full example of implementation (in java only, i'm doing it for kotlin) in my other project, <a href="https://github.com/gustavoasevedo/SFramework">sFramework</a>.

All the work is based on annotations.

For example, in your model you can tell one of your variables is on the db, so you need to asing his type,name on the database and if is
a primary key, there is a special annotation for it.

Java:
```java
    @BaseDBFieldName("id")
    @BaseDBType("INTEGER")
    @BaseDBPrimaryKey
    protected int id;

    @BaseDBFieldName("name")
    @BaseDBType("TEXT")
    protected String name;
```

Kotlin:
```kotlin
    @BaseDBFieldName("id")
    @BaseDBType("INTEGER")
    @BaseDBPrimaryKey
    var id: Int = 0

    @BaseDBFieldName("name")
    @BaseDBType("TEXT")
    var name: String = ""
```

Now you need to create a class just to do you inserts, selects and initate your database. 

The Dao needs to extends BaseTable, and in the constructor you need a context, the class that will be used(Remember of your model with
annotations? Its here you use it), the name of the database and the version (tip: create a constant class and import from him). Now you
only need to call createTable().


With your table created (if it already exists, nothing will happen) you can work with you database, i recomend this method to call when
you need your database:

Java:
```Java
    public static TestObjectDao getInstance(Context context) {
        if (instance == null) {
            synchronized (TestObjectDao.class) {
                if (instance == null) {
                    instance = new TestObjectDao(context);
                }
            }
        }
        return instance;
    }
```

Kotlin:
```kotlin
    companion object {

        private var instance: TestObjectDao? = null

        fun getInstance(context: Context): TestObjectDao? {
            if (instance == null) {
                synchronized(TestObjectDao::class.java) {
                    if (instance == null) {
                        instance = TestObjectDao(context)
                    }
                }
            }
            return instance
        }
    }
```


When you have, just create your own insert or select methods, there is methods for inserting one item and entire lists. Selects for
one and more itens and a rawQuery method too;

There is all methods you can call in the API.

```Java
      selectList(YourClass.class);
      selectListWhere(YourClass.class, fields, values);
      selectWhere(YourClass.class, fields, values);
      selectWhereArray(fields, values);
      selectNoWhere();
      selectWhereObject(fields, values);
      insert(objectArrayList)
```

Here is a example of a Dao class in Java:

```java
package com.dss.sframework.dao;

import android.content.Context;

import com.dss.sdatabase.dao.BaseTable;
import com.dss.sdatabase.exceptions.InvalidTypeException;
import com.dss.sframework.model.dto.TestObjectDTO;
import com.dss.sframework.tools.constant.ConstantDB;
import com.dss.sframework.model.entity.TestObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TestObjectDao extends BaseTable{

    private static TestObjectDao instance;

    public TestObjectDao(Context context) {
        super(context, TestObject.class, ConstantDB.dbName, ConstantDB.version);
        createTable();
    }

    public static TestObjectDao getInstance(Context context) {
        if (instance == null) {
            synchronized (TestObjectDao.class) {
                if (instance == null) {
                    instance = new TestObjectDao(context);
                }
            }
        }
        return instance;
    }

    public void insertObject(TestObjectDTO testObjectDTO) {

        ArrayList<Object> objectArrayList = new ArrayList<>();
        TestObject testObject = new TestObject(testObjectDTO);

        objectArrayList.add(testObject);

        insert(objectArrayList);
    }

    public void insertListObject(ArrayList<TestObjectDTO> testObjectDTOsList) {
        ArrayList<Object> objectArrayList = new ArrayList<>();
        ArrayList<TestObject> testObjects = new ArrayList<>();

        for(TestObjectDTO testObjectDTO : testObjectDTOsList){
            TestObject testObject = new TestObject(testObjectDTO);
            testObjects.add(testObject);
        }

        for (Object object : testObjects) {
            objectArrayList.add(object);
        }

        insert(objectArrayList);
    }


    public TestObjectDTO selectId(int Id) {

        TestObjectDTO testObject = new TestObjectDTO();
        ArrayList<String> fields = new ArrayList<>();
        fields.add("id_Format");
        String[] values = {String.valueOf(Id)};

        testObject = selectWhereObject(fields, values);

        return testObject;

    }


    public ArrayList<TestObjectDTO> selectListItems() {
        ArrayList<Object> objectList = new ArrayList<>();
        ArrayList<TestObjectDTO> lista = new ArrayList<>();

        objectList = selectNoWhere();

        lista = mountObjectList(objectList);

        return lista;
    }


    public ArrayList<TestObjectDTO> selectListbyName(String nome) {

        ArrayList<Object> objectList = new ArrayList<>();
        ArrayList<TestObjectDTO> lista = new ArrayList<>();

        ArrayList<String> fields = new ArrayList<>();
        fields.add("format_Name");
        String[] values = {nome};

        objectList = selectWhereArray(fields, values);

        lista = mountObjectList(objectList);

        return lista;
    }


    public TestObjectDTO selectWhereObject(ArrayList<String> fields, String[] values) {

        TestObject testObject = new TestObject();
        TestObjectDTO testObjectDTO = new TestObjectDTO();

        try {

            testObject = (TestObject) selectWhere(fields, values);
            testObjectDTO = new TestObjectDTO(testObject);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvalidTypeException e) {
            e.printStackTrace();
        }

        return testObjectDTO;
    }

    public ArrayList<Object> selectWhereArray(ArrayList<String> fields, String[] values) {

        ArrayList<Object> objectList = new ArrayList<>();

        try {

            objectList = selectListWhere(fields, values);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvalidTypeException e) {
            e.printStackTrace();
        }

        return objectList;
    }

    public ArrayList<Object> selectNoWhere() {

        ArrayList<Object> objectList = new ArrayList<>();

        try {

            objectList = selectList();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvalidTypeException e) {
            e.printStackTrace();
        }

        return objectList;
    }


    public ArrayList<TestObjectDTO> mountObjectList(ArrayList<Object> objectList) {

        ArrayList<TestObjectDTO> lista = new ArrayList<>();

        for (Object object : objectList) {
            TestObjectDTO testObject = new TestObjectDTO((TestObject) object);
            lista.add(testObject);
        }

        return lista;
    }

    public void drop(){

    }


    public void deleteData(){
        deleteAll();
    }

}
```
And this is the Kotlin example:

```kotlin
class TestObjectDao(context: Context) : BaseTable(context, TestObject::class.java, ConstantDB.dbName, ConstantDB.version) {

    init {
        createTable()
    }

    fun insertObject(testObjectDTO: TestObjectDTO) {

        val objectArrayList = ArrayList<Any>()
        val testObject = TestObject(testObjectDTO)

        objectArrayList.add(testObject)

        insert(objectArrayList)
    }

    fun insertListObject(testObjectDTOsList: ArrayList<TestObjectDTO>) {
        val objectArrayList = ArrayList<Any>()
        val testObjects = ArrayList<TestObject>()

        for (testObjectDTO in testObjectDTOsList) {
            val testObject = TestObject(testObjectDTO)
            testObjects.add(testObject)
        }

        for (`object` in testObjects) {
            objectArrayList.add(`object`)
        }

        insert(objectArrayList)
    }


    fun selectId(Id: Int): TestObjectDTO {

        var testObject = TestObjectDTO()
        val fields = ArrayList<String>()
        fields.add("id_Format")
        val values = arrayOf(Id.toString())

        testObject = selectWhereObject(fields, values)

        return testObject

    }


    fun selectListItems(): ArrayList<TestObjectDTO> {
        var objectList = ArrayList<Any>()
        var lista = ArrayList<TestObjectDTO>()

        objectList = selectNoWhere()

        lista = mountObjectList(objectList)

        return lista
    }


    fun selectListbyName(nome: String): ArrayList<TestObjectDTO> {

        var objectList = ArrayList<Any>()
        var lista = ArrayList<TestObjectDTO>()

        val fields = ArrayList<String>()
        fields.add("format_Name")
        val values = arrayOf(nome)

        objectList = selectWhereArray(fields, values)

        lista = mountObjectList(objectList)

        return lista
    }


    fun selectWhereObject(fields: ArrayList<String>, values: Array<String>): TestObjectDTO {

        var testObject = TestObject()
        var testObjectDTO = TestObjectDTO()

        try {

            testObject = selectWhere(fields, values) as TestObject
            testObjectDTO = TestObjectDTO(testObject)

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvalidTypeException) {
            e.printStackTrace()
        }

        return testObjectDTO
    }

    fun selectWhereArray(fields: ArrayList<String>, values: Array<String>): ArrayList<Any> {

        var objectList = ArrayList<Any>()

        try {

            objectList = selectListWhere(fields, values)

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvalidTypeException) {
            e.printStackTrace()
        }

        return objectList
    }

    fun selectNoWhere(): ArrayList<Any> {

        var objectList = ArrayList<Any>()

        try {

            objectList = selectList()

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvalidTypeException) {
            e.printStackTrace()
        }

        return objectList
    }


    fun mountObjectList(objectList: ArrayList<Any>): ArrayList<TestObjectDTO> {

        val lista = ArrayList<TestObjectDTO>()

        for (`object` in objectList) {
            val testObject = TestObjectDTO(`object` as TestObject)
            lista.add(testObject)
        }

        return lista
    }

    fun drop() {

    }


    fun deleteData() {
        deleteAll()
    }

    companion object {

        private var instance: TestObjectDao? = null

        fun getInstance(context: Context): TestObjectDao? {
            if (instance == null) {
                synchronized(TestObjectDao::class.java) {
                    if (instance == null) {
                        instance = TestObjectDao(context)
                    }
                }
            }
            return instance
        }
    }
   ``` 
