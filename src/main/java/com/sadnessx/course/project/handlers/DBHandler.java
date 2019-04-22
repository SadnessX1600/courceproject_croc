package com.sadnessx.course.project.handlers;

import com.sadnessx.course.project.annotations.GenerateTable;
import com.sadnessx.course.project.annotations.TableField;
import com.sadnessx.course.project.console.application.ConsoleApp;
import com.sadnessx.course.project.entities.Student;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class DBHandler {
    private Connection connection;
    private Statement stmt;
    private PreparedStatement prep;
    private HashMap<Class, String> converter = new HashMap<>();

    public DBHandler() {
        converter.put(String.class, "STRING");
        converter.put(int.class, "INTEGER");
        converter.put(float.class, "REAL");
    }

    public void createTable(Class cls, Boolean dropIfExists) {
        if (!cls.isAnnotationPresent(GenerateTable.class)) {
            throw new RuntimeException("Annotation @GenerateTable not present");
        }
        //Todo add more convertable types
        StringBuilder sb = new StringBuilder();
        if (dropIfExists) {
            try {
                stmt.executeUpdate("DROP TABLE IF EXISTS " + ((GenerateTable) cls.getAnnotation(GenerateTable.class)).title() + ";");
            } catch (SQLException e) {
                e.printStackTrace();
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
                sb.append((", "));
            }
        }
        sb.setLength(sb.length() - 2);
        sb.append(");");
        try {
            System.out.println(sb.toString());
            stmt.executeUpdate(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addEntity(Class cls, ConsoleApp consoleApp) {
        Scanner scanner = consoleApp.getScanner();
        int tempInt;
        String tempString;
        float tempFloat;
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
            System.out.println(sb.toString());
            prep = connection.prepareStatement(sb.toString());
            for (int i = 1; i < valueKey; i++) {
                switch (values.get(i).get(0)) {
                    case ("INTEGER"): {
                        System.out.println("Enter " + values.get(i).get(0) + " value for " + values.get(i).get(1));
                        tempInt = scanner.nextInt();
                        prep.setInt(i, tempInt);
                        break;

                    }
                    case ("STRING"): {
                        System.out.println("Enter " + values.get(i).get(0) + " value for " + values.get(i).get(1));
                        tempString = scanner.next();
                        prep.setString(i, tempString);
                        break;
                    }
                    case ("REAL"): {
                        System.out.println("Enter " + values.get(i).get(0) + " value for " + values.get(i).get(1));
                        tempFloat = Float.valueOf(scanner.next());
                        prep.setFloat(i, tempFloat);
                        break;
                    }
                }
            }
            prep.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addEntitiesList(Class cls, ArrayList<Student> students) {
        try {
            if (!cls.isAnnotationPresent(GenerateTable.class)) {
                throw new RuntimeException("Annotation @GenerateTable not present");
            }
            int valueKey;
            HashMap<Integer, String> values = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            for (Student student : students) {
                sb.append("INSERT INTO ");
                sb.append(((GenerateTable) cls.getAnnotation(GenerateTable.class)).title()).append("(");
                Field[] fields = cls.getDeclaredFields();
                valueKey = 1;
                for (Field field : fields) {
                    if (field.isAnnotationPresent(TableField.class)) {
                        sb.append(field.getName()).append(",");
                        values.put(valueKey, field.getName());
                        valueKey++;
                    }
                }
                sb.setLength(sb.length() - 1);
                sb.append(") VALUES(");
                for (int i = 1; i < valueKey; i++) {
                    if (values.get(i).equals("averageScore")) {
                        sb.append("round(?,2),");
                    } else {
                        sb.append("?,");
                    }
                }
                sb.setLength(sb.length() - 1);
                sb.append(");");
                prep = connection.prepareStatement(sb.toString());
                for (int i = 1; i < valueKey; i++) {
                    switch (values.get(i)) {
                        case ("id"): {
                            prep.setInt(i, student.getId());
                            break;
                        }
                        case ("name"): {
                            prep.setString(i, student.getName());
                            break;
                        }
                        case ("speciality"): {
                            prep.setString(i, student.getSpeciality().getName());
                            break;
                        }
                        case ("averageScore"): {
                            prep.setFloat(i, student.getAverageScore());
                            break;
                        }
                    }
                }
                System.out.println(sb.toString());
                prep.executeUpdate();
                values.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Student> getEntitiesList() {
        ResultSet resultSet = getAllFromDB(Student.class);
        return null;
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
            System.out.println(prep);
            prep.setString(1, targetValue);
            return prep.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet calculateAverageScoreFromSpec(String speciality) {
        try {
            prep = connection.prepareStatement("SELECT avg(averageScore) FROM " +
                    Student.class.getAnnotation(GenerateTable.class).title() +
                    " WHERE speciality = ?;");
            prep.setString(1, speciality);
            return prep.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
                System.out.println("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connectDB(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            stmt = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Unable to connect to DB");
        }
    }

    public void disconnectDB() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            prep.close();
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
