package com.sadnessx.course.project.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sadnessx.course.project.entities.Student;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class IOHandler {
    private XmlMapper xmlMapper = new XmlMapper();

    public void exportFromList(String path, ArrayList<Student> students) {
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        StringBuilder sb = new StringBuilder();
        for (Student student : students) {
            try {
                sb.append(xmlMapper.writeValueAsString(student));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter fileWriter = new FileWriter(new File(path))) {
            fileWriter.write("<Students>\n" + sb.toString() + "</Students>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO threads
    public ArrayList<Student> importStudentsToList(String path) {
        try {
            ArrayList<Student> studentsList = new ArrayList<>();
            String xmlString = Files.lines(Paths.get(path)).collect(Collectors.joining("\n"));
            String[] studentsStrings = xmlString.split("</Student>");
            studentsStrings[0] = studentsStrings[0].substring(11);
            for (int i = 0; i < studentsStrings.length - 1; i++) {
                studentsList.add(xmlMapper.readValue(studentsStrings[i] + "</Student>", Student.class));
            }
            return studentsList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

