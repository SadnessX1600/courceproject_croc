package com.sadnessx.course.project.handlers;

import com.sadnessx.course.project.annotations.CustomGetter;
import com.sadnessx.course.project.annotations.CustomSetter;
import com.sadnessx.course.project.annotations.GenerateTable;
import com.sadnessx.course.project.annotations.TableField;
import com.sadnessx.course.project.console.application.ConsoleApp;
import com.sadnessx.course.project.entities.Student;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class DBHandler {
    private Connection connection;
    private Statement stmt;
    private PreparedStatement prep;
    private HashMap<Class, String> converter = new HashMap<>();
    private Set<Class<?>> tablesSet;
    private static Logger LOGGER = LogManager.getLogger();

    public DBHandler() {
        converter.put(String.class, "STRING");
        converter.put(int.class, "INTEGER");
        converter.put(float.class, "REAL");
        converter.put(Float.class, "REAL");
        converter.put(Integer.class, "INTEGER");
        converter.put(boolean.class, "BOOLEAN");
        converter.put(Boolean.class, "BOOLEAN");
        Reflections reflections = new Reflections("com.sadnessx.course.project");
        tablesSet = reflections.getTypesAnnotatedWith(GenerateTable.class);
    }

    public void clearTable(Class cls) {
        try {
            stmt.executeUpdate("DELETE FROM " + ((GenerateTable) cls.getAnnotation(GenerateTable.class)).title() + ";");
            stmt.executeUpdate("VACUUM;");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deleteTable(Class cls) {
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + ((GenerateTable) cls.getAnnotation(GenerateTable.class)).title() + ";");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void createTable(Class cls, Boolean dropIfExists) {
        if (!cls.isAnnotationPresent(GenerateTable.class)) {
            throw new RuntimeException("Annotation @GenerateTable not present");
        }
        StringBuilder sb = new StringBuilder();
        if (dropIfExists) {
            try {
                stmt.executeUpdate("DROP TABLE IF EXISTS " + ((GenerateTable) cls.getAnnotation(GenerateTable.class)).title() + ";");
            } catch (SQLException e) {
                LOGGER.error(e.getSQLState());
            }
        }
        sb.append("CREATE TABLE ");
        sb.append(((GenerateTable) cls.getAnnotation(GenerateTable.class)).title()).append(" (");
        Field[] fields = cls.getDeclaredFields();
        for (Field field :
                fields) {
            if (field.isAnnotationPresent(TableField.class)) {
                sb.append(field.getName()).append(" ");
                if (!field.getAnnotation(TableField.class).type().equals("")) {
                    sb.append(field.getAnnotation(TableField.class).type());
                } else {
                    sb.append(converter.get(field.getType()));
                }
                if (field.getAnnotation(TableField.class).isPrimaryKey()) {
                    sb.append(" PRIMARY KEY ");
                }
                if (field.getAnnotation(TableField.class).isUnique()) {
                    sb.append(" UNIQUE ");
                }
                if (!field.getAnnotation(TableField.class).referenceClassName().equals("")) {
                    Iterator iter = tablesSet.iterator();
                    Class targetClass;
                    while (iter.hasNext()) {
                        targetClass = (Class) iter.next();
                        if (targetClass.getSimpleName().
                                equals(field.getAnnotation(TableField.class).referenceClassName())) {
                            sb.append(" REFERENCES ").append(((GenerateTable) targetClass.getAnnotation(GenerateTable.class)).title());
                            Field[] fieldsRef = targetClass.getDeclaredFields();
                            for (Field fieldRef : fieldsRef) {
                                if (fieldRef.isAnnotationPresent(TableField.class) &&
                                        fieldRef.getAnnotation(TableField.class).isPrimaryKey()) {
                                    sb.append(" (").append(fieldRef.getName()).append(") ");
                                    break;
                                }
                            }
                        }
                    }
                }
                sb.append((", "));
            }
        }
        sb.setLength(sb.length() - 2);
        sb.append(");");
        try {
            stmt.executeUpdate(sb.toString());
            LOGGER.trace(sb.toString());
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void addEntity(Class cls, ConsoleApp consoleApp) {
        Scanner scanner = consoleApp.getScanner();
        try {
            if (!cls.isAnnotationPresent(GenerateTable.class)) {
                throw new RuntimeException("Annotation @GenerateTable not present");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(((GenerateTable) cls.getAnnotation(GenerateTable.class)).title()).append("(");
            Field[] fields = cls.getDeclaredFields();
            HashMap<Integer, ArrayList<String>> values = new HashMap<>();
            Integer valueKey = 1;
            for (Field field :
                    fields) {
                if (field.isAnnotationPresent(TableField.class)) {
                    if (!field.getAnnotation(TableField.class).type().equals("")) {
                        sb.append(field.getName()).append(",");
                        values.put(valueKey, new ArrayList<>(Arrays.asList(field.getAnnotation(TableField.class).type(), field.getName())));
                        valueKey++;
                    } else {
                        sb.append(field.getName()).append(",");
                        values.put(valueKey, new ArrayList<>(Arrays.asList(converter.get(field.getType()), field.getName())));
                        valueKey++;
                    }
                }
            }
            sb.setLength(sb.length() - 1);
            sb.append(") VALUES(");
            for (int i = 1; i < valueKey; i++) {
                if (values.get(i).get(0).equals("REAL")) {
                    sb.append("round(?,2),");
                } else {
                    sb.append("?,");
                }
            }
            sb.setLength(sb.length() - 1);
            sb.append(");");
            LOGGER.trace(sb.toString());
            prep = connection.prepareStatement(sb.toString());
            for (int i = 1; i < valueKey; i++) {
                System.out.println("Enter " + values.get(i).get(0) + " value for " + values.get(i).get(1));
                prep.setObject(i, scanner.nextLine());
            }
            prep.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void addEntitiesList(ArrayList<Object> objects) {
        Class currentClass = objects.get(0).getClass();
        boolean isTableExists = false;
        for (Class<?> aClass : tablesSet) {
            if (aClass.equals(currentClass)) {
                isTableExists = true;
                break;
            }
        }
        if (!isTableExists) {
            LOGGER.error("Table not found");
            throw new RuntimeException("Table with defined objects not found");
        }
        int valueKey;
        HashMap<Integer, Field> values = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        PropertyDescriptor pd;
        Method getter = null;

        for (Object object : objects) {
            try {
                sb.append("INSERT INTO ");
                sb.append(((GenerateTable) currentClass.getAnnotation(GenerateTable.class)).title()).append("(");
                Field[] fields = currentClass.getDeclaredFields();
                valueKey = 1;
                for (Field field : fields) {
                    if (field.isAnnotationPresent(TableField.class)) {
                        sb.append(field.getName()).append(",");
                        values.put(valueKey, field);
                        valueKey++;
                    }
                }
                sb.setLength(sb.length() - 1);
                sb.append(") VALUES(");
                for (int i = 1; i < valueKey; i++) {
                    if (!values.get(i).getAnnotation(TableField.class).type().equals("")) {
                        if (values.get(i).getAnnotation(TableField.class).type().equals("REAL")) {
                            sb.append("round(?,2),");
                        } else {
                            sb.append("?,");
                        }
                    } else {
                        if (converter.get(values.get(i).getType()).equals("REAL")) {
                            sb.append("round(?,2),");
                        } else {
                            sb.append("?,");
                        }
                    }
                }
                sb.setLength(sb.length() - 1);
                sb.append(");");
                prep = connection.prepareStatement(sb.toString());
                for (int i = 1; i < valueKey; i++) {
                    pd = new PropertyDescriptor(values.get(i).getName(), currentClass);
                    if (pd.getReadMethod().isAnnotationPresent(CustomGetter.class)) {
                        try {
                            getter = object.getClass().getMethod(pd.getReadMethod().getAnnotation(CustomGetter.class).getterMethodName());
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    } else {
                        getter = pd.getReadMethod();
                    }
                    if (getter == null) {
                        throw new RuntimeException("Can't find getter method");
                    }
                    prep.setObject(i, getter.invoke(object));
                }
                prep.executeUpdate();
                sb.setLength(0);
                values.clear();
            } catch (SQLException | IllegalAccessException | InvocationTargetException |
                    IntrospectionException | RuntimeException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }


    public ArrayList<Object> getEntitiesList(Class cls) {
        ResultSet resultSet = getAllFromDB(cls);
        try {
            ResultSet referenceRS;
            Iterator iter;
            String fieldName;
            ArrayList<Object> returnList = new ArrayList<>();
            ArrayList<Field> annotatedFields = new ArrayList<>();
            Field[] targetClassFields = cls.getDeclaredFields();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            PropertyDescriptor pd;
            Method setter = null;
            Field targetClassField;
            for (Field targetClassField1 : targetClassFields) {
                if (targetClassField1.isAnnotationPresent(TableField.class)) {
                    annotatedFields.add(targetClassField1);
                }
            }
            while (resultSet.next()) {
                Object obj = cls.newInstance();
                iter = annotatedFields.iterator();
                while (iter.hasNext()) {
                    targetClassField = (Field) iter.next();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        //Если поле является ссылкой на другую сущность, создаем сущность ее
                        if (!((targetClassField.getAnnotation(TableField.class).referenceClassName().equals(""))) &&
                                targetClassField.getName().equals(rsmd.getColumnName(i))) {
                            //Получаем setter на ссылаемый объект
                            fieldName = rsmd.getColumnName(i);
                            if (targetClassField.getName().equals(fieldName)) {
                                pd = new PropertyDescriptor(fieldName, cls);
                                if (pd.getWriteMethod().isAnnotationPresent(CustomSetter.class)) {
                                    try {
                                        setter = cls.getMethod(pd.getWriteMethod().getAnnotation(CustomSetter.class).setterMethodName());
                                    } catch (NoSuchMethodException e) {
                                        try {
                                            throw new NoSuchMethodException("Can't find setter method for " + obj.getClass().getSimpleName());
                                        } catch (NoSuchMethodException e1) {
                                            LOGGER.error(e1.getMessage());
                                        }
                                    }
                                } else {
                                    setter = pd.getWriteMethod();
                                }
                            }
                            if (setter == null) {
                                throw new RuntimeException("Can't find setter method for " + obj.getClass().getSimpleName());
                            }
                            //Создаем объект на который ссылается данный
                            Class referenceClass = findTableClassByClassName(targetClassField.getAnnotation(TableField.class).referenceClassName());
                            Field[] refrenceClassFields = referenceClass.getDeclaredFields();
                            if (refrenceClassFields == null) {
                                throw new RuntimeException("Can't find any fields");
                            }
                            ArrayList<Field> referenceAnnotatedFields = new ArrayList<>();
                            Field refrenceClassField;
                            String referenceKeyName = null;
                            Method setterRef = null;
                            for (Field refrenceClassField1 : refrenceClassFields) {
                                if (refrenceClassField1.isAnnotationPresent(TableField.class)) {
                                    referenceAnnotatedFields.add(refrenceClassField1);
                                }
                                if (refrenceClassField1.getAnnotation(TableField.class).isPrimaryKey()) {
                                    referenceKeyName = refrenceClassField1.getName();
                                }
                            }
                            referenceRS = stmt.executeQuery("SELECT * FROM " +
                                    ((GenerateTable) referenceClass.getAnnotation(GenerateTable.class)).title() +
                                    " WHERE " + referenceKeyName + " = " + resultSet.getString(targetClassField.getName()));
                            ResultSetMetaData rsmdRef = referenceRS.getMetaData();
                            Object objRef = referenceClass.newInstance();
                            Iterator iterRef = referenceAnnotatedFields.iterator();
                            referenceRS.next();
                            while (iterRef.hasNext()) {
                                refrenceClassField = (Field) iterRef.next();
                                for (int j = 1; j <= rsmdRef.getColumnCount(); j++) {
                                    fieldName = rsmdRef.getColumnName(j);
                                    if (refrenceClassField.getName().equals(fieldName)) {
                                        pd = new PropertyDescriptor(fieldName, referenceClass);
                                        if (pd.getWriteMethod().isAnnotationPresent(CustomSetter.class)) {
                                            try {
                                                setterRef = referenceClass.getMethod(pd.getWriteMethod().getAnnotation(CustomSetter.class).setterMethodName());
                                            } catch (NoSuchMethodException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            setterRef = pd.getWriteMethod();
                                        }
                                        if (setterRef == null) {
                                            throw new RuntimeException("Can't find setter method for " + objRef.getClass().getSimpleName());
                                        }
                                        switch (refrenceClassField.getType().getSimpleName().toLowerCase()) {
                                            case ("int"): {
                                                setterRef.invoke(objRef, Integer.valueOf(referenceRS.getString(j)));
                                                break;
                                            }
                                            case ("integer"): {
                                                setterRef.invoke(objRef, Integer.valueOf(referenceRS.getString(j)));
                                                break;
                                            }
                                            case ("string"): {
                                                setterRef.invoke(objRef, referenceRS.getString(j));
                                                break;
                                            }
                                            case ("float"): {
                                                setterRef.invoke(objRef, Float.valueOf(referenceRS.getString(j)));
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            setter.invoke(obj, objRef);
                            break;
                        }
                        //Иначе создаем простое поле
                        else {
                            fieldName = rsmd.getColumnName(i);
                            if (targetClassField.getName().equals(fieldName)) {
                                pd = new PropertyDescriptor(fieldName, cls);
                                if (pd.getWriteMethod().isAnnotationPresent(CustomSetter.class)) {
                                    try {
                                        setter = cls.getMethod(pd.getWriteMethod().getAnnotation(CustomSetter.class).setterMethodName());
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    setter = pd.getWriteMethod();
                                }
                                if (setter == null) {
                                    throw new RuntimeException("Can't find setter method for " + obj.getClass().getSimpleName());
                                }
                                switch (targetClassField.getType().getSimpleName().toLowerCase()) {
                                    case ("int"): {
                                        setter.invoke(obj, Integer.valueOf(resultSet.getString(i)));
                                        break;
                                    }
                                    case ("integer"): {
                                        setter.invoke(obj, Integer.valueOf(resultSet.getString(i)));
                                        break;
                                    }
                                    case ("string"): {
                                        setter.invoke(obj, resultSet.getString(i));
                                        break;
                                    }
                                    case ("float"): {
                                        setter.invoke(obj, Float.valueOf(resultSet.getString(i)));
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                returnList.add(obj);
            }
            return returnList;
        } catch
        (SQLException | InstantiationException | InvocationTargetException
                        | IllegalAccessException | IntrospectionException | RuntimeException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public ResultSet findByField(Class cls, String fieldName, String targetValue) {
        try {
            if (!cls.isAnnotationPresent(GenerateTable.class)) {
                throw new RuntimeException("Annotation @GenerateTable not present");
            }
            Field[] fields = cls.getDeclaredFields();
            for (Field field :
                    fields) {
                if (field.isAnnotationPresent(TableField.class) && field.getName().equals(fieldName)) {
                    prep = connection.prepareStatement("SELECT * FROM " +
                            ((GenerateTable) cls.getAnnotation(GenerateTable.class)).title() + " WHERE " +
                            fieldName + " = ?;");
                    break;
                }
            }
            prep.setString(1, targetValue);
            return prep.executeQuery();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public ResultSet calculateAverageScoreFromSpec(int speciality) {
        try {
            prep = connection.prepareStatement("SELECT avg(averageScore) FROM " +
                    Student.class.getAnnotation(GenerateTable.class).title() +
                    " WHERE speciality = ?;");
            prep.setInt(1, speciality);
            return prep.executeQuery();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public ResultSet getAllFromDB(Class cls) {
        try {
            if (!cls.isAnnotationPresent(GenerateTable.class)) {
                throw new RuntimeException("Annotation @GenerateTable not present");
            }
            prep = connection.prepareStatement("SELECT * FROM " +
                    ((GenerateTable) cls.getAnnotation(GenerateTable.class)).title() + ";");
            return prep.executeQuery();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public void showResultSet(ResultSet resultSet) {
        try {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Class findTableClassByClassName(String name) {
        Iterator iter = tablesSet.iterator();
        Class currentClass;
        Class searchedClass;
        while (iter.hasNext()) {
            currentClass = (Class) iter.next();
            if (currentClass.getSimpleName().equals(name)) {
                searchedClass = currentClass;
                return searchedClass;
            }
        }
        return null;
    }

    public void connectDB(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            stmt = connection.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException("Unable to connect to DB");
        }
    }

    public void disconnectDB() {
        try {
            stmt.close();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        try {
            prep.close();
        } catch (SQLException | NullPointerException e) {
            LOGGER.error(e.getMessage() + " prepared statement wasn't opened");
        }
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public Set<Class<?>> getTablesSet() {
        return tablesSet;
    }
}
