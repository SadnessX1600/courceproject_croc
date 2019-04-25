package com.sadnessx.tests;

import com.sadnessx.course.project.entities.Student;
import com.sadnessx.course.project.entities.StudentSpeciality;
import com.sadnessx.course.project.handlers.IOHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ReadObjectTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new ArrayList<>(Arrays.asList(new Student(1, "testStudent1", new StudentSpeciality(2, "SampleSpec2", "test2"), 7.5f),
                        new Student(2, "testStudent2", new StudentSpeciality(1, "SampleSpec", "test1"), 25.3f),
                        new Student(3, "testStudent3", new StudentSpeciality(2, "SampleSpec2", "test2"), 15f))), "student.xml", Student.class},
                {new ArrayList<>(Arrays.asList(new Student(1, "testStudent1", new StudentSpeciality(2, "SampleSpec2", "test2"), 7.5f),
                        new Student(2, "testStudent2", new StudentSpeciality(1, "wrong", "test1"), 25.3f),
                        new Student(3, "testStudent3", new StudentSpeciality(2, "SampleSpec2", "test2"), 15f))), "student.xml", Student.class},
                {new ArrayList<>(Arrays.asList(new Student(1, "testStudent1", new StudentSpeciality(2, "SampleSpec2", "test2"), 7.5f),
                        new Student(2, "testStudent2", new StudentSpeciality(1, "SampleSpec", "test1"), 25.3f),
                        new Student(3, "testStudent3", new StudentSpeciality(2, "SampleSpec2", "test2"), 15f))), "student.xml", StudentSpeciality.class}
        });
    }

    private ArrayList<Student> studentsList;
    private String path;
    private IOHandler io;
    private Class cls;

    public ReadObjectTest(ArrayList<Student> studentsList, String path, Class cls) {
        this.studentsList = studentsList;
        this.path = path;
        this.cls = cls;
    }

    @Before
    public void init() {
        io = new IOHandler();
    }

    @Test
    public void test1() {
        Assert.assertEquals(studentsList, io.importEntitiesToList(cls, path));
    }

}
