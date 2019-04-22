package com.sadnessx.course.project.entities;

import com.sadnessx.course.project.annotations.GenerateTable;
import com.sadnessx.course.project.annotations.TableField;

@GenerateTable(title = "students")
public class Student {
    @TableField(isPrimaryKey = true, isUnique = true)
    private int id;
    @TableField
    private String name;
    @TableField(type = "STRING")
    private StudentSpeciality speciality;
    @TableField
    private float averageScore;

    public Student() {
    }

    public Student(int id, String name, StudentSpeciality speciality, float averageScore) {
        this.id = id;
        this.name = name;
        this.speciality = speciality;
        this.averageScore = averageScore;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecility(StudentSpeciality speciality) {
        this.speciality = speciality;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public StudentSpeciality getSpeciality() {
        return speciality;
    }

    public float getAverageScore() {
        return averageScore;
    }

}
