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
        compile 'com.dss.sDatabase:sDatabase:1.0.5'
        ...
    }
```


All the work is based on annotations.

For example, in your model you can tell one of your variables is on the db, so you need to asing his type,name on the database and if is
a primary key, there is a special annotation for it.


```java
    @BaseDBFieldName("id")
    @BaseDBType("INTEGER")
    @BaseDBPrimaryKey
    protected int id;

    @BaseDBFieldName("name")
    @BaseDBType("TEXT")
    protected String name;
```

Then you need to assing what is the gets and sets, the API will recognize by a annotation with the name of the field on database.

```java
    @BaseDBMethodGetName("name")
    public String getName() {
        return name;
    }

    @BaseDBMethodSetName("name")
    public void setName(String name) {
        this.name = name;
    }
```

Now you need to create a class just to do you inserts, selects and initate your database. 

The Dao needs to extends BaseTable, and in the constructor you need a context, the class that will be used(Remember of your model with
annotations? Its here you use it), the name of the database and the version (tip: create a constant class and import from him). Now you
only need to call createTable().


With your table created (if it already exists, nothing will happen) you can work with you database, i recomend this method to call when
you need your database:


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

Here is a example of a Dao class:

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

public class TestObjectDao extends BaseTable {

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
        fields.add("id");
        String[] values = {String.valueOf(Id)};

        testObject = selectWhereObject(fields, values);

        return testObject;

    }


    public ArrayList<TestObjectDTO> selectList() {
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
        fields.add("name");
        String[] values = {nome};

        objectList = selectWhereArray(fields, values);

        lista = mountObjectList(objectList);

        return lista;
    }


    public TestObjectDTO selectWhereObject(ArrayList<String> fields, String[] values) {

        TestObject testObject = new TestObject();
        TestObjectDTO testObjectDTO = new TestObjectDTO();

        try {

            testObject = (TestObject) selectWhere(TestObject.class, fields, values);
            testObjectDTO = new TestObjectDTO(testObject);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
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

            objectList = selectListWhere(TestObject.class, fields, values);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
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

            objectList = selectList(TestObject.class);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
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

        ArrayList<TestObjectDTO> list = new ArrayList<>();

        for (Object object : objectList) {
            TestObjectDTO testObject = new TestObjectDTO((TestObject) object);
            list.add(testObject);
        }

        return list;
    }

    public void drop(){

    }


    public void deleteData(){
        deleteAll();
    }

}
```

