package com.sadnessx.course.project.entities;

import com.sadnessx.course.project.annotations.GenerateTable;
import com.sadnessx.course.project.annotations.TableField;


@GenerateTable(title = "specialities")
public class StudentSpeciality {
    @TableField(isPrimaryKey = true, isUnique = true)
    private int id;
    @TableField
    private String name;
    @TableField
    private String description;

    public StudentSpeciality() {
    }

    public StudentSpeciality(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StudentSpeciality) {
            return this.id == ((StudentSpeciality) obj).getId() &&
                    this.description.equals(((StudentSpeciality) obj).getDescription()) &&
                    this.name.equals(((StudentSpeciality) obj).getName());
        }
        return false;
    }
}
