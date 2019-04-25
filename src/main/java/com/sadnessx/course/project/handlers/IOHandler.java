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

    public void exportFromListToXml(String path, ArrayList<Object> objects) {
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            try {
                sb.append(xmlMapper.writeValueAsString(object));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter fileWriter = new FileWriter(new File(path))) {
            fileWriter.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Object> importEntitiesToList(Class cls, String path) {
        try {
            ArrayList<Object> objList = new ArrayList<>();
            String xmlString = Files.lines(Paths.get(path)).collect(Collectors.joining("\n"));
            String[] objStrings = xmlString.split("</" + cls.getSimpleName() + ">");
            for (String objString : objStrings) {
                objList.add(xmlMapper.readValue(objString + "</" + cls.getSimpleName() + ">", cls));
            }
            return objList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

