package com.sadnessx.course.project.console.application;


import com.sadnessx.course.project.entities.Student;
import com.sadnessx.course.project.entities.StudentSpeciality;
import com.sadnessx.course.project.handlers.DBHandler;
import com.sadnessx.course.project.handlers.IOHandler;

import java.util.Scanner;

public class ConsoleApp {
    private Scanner scanner = new Scanner(System.in);
    private DBHandler db;
    private IOHandler io;

    public ConsoleApp(DBHandler db, IOHandler io) {
        this.db = db;
        this.io = io;
    }

    public void start() {
        boolean isAlive = true;
        while (isAlive) {
            System.out.println("Waiting for command");
            String cmd = scanner.nextLine();
            String[] cmdSplitted = cmd.split(" ");
            switch (cmdSplitted[0]) {
                case "/connect": {
                    System.out.println("Connected");
                    db.connectDB("actualDB.db");
                    break;
                }
                case "/createTable": {
                    System.out.println("Enter table class");
                    if (scanner.nextLine().equals("Student")) {
                        db.createTable(Student.class, true);
                    }
                    if (scanner.nextLine().equals("StudentSpeciality")) {
                        db.createTable(StudentSpeciality.class, true);
                    }
                    break;
                }
                case "/importAll": {
                    System.out.println("Enter path to xml file");
                    db.addEntitiesList(Student.class, io.importStudentsToList(scanner.nextLine()));
                    break;
                }
                case "/exportAll": {
                    System.out.println("Enter path to xml file");

                    break;
                }
                case "/quit": {
                    scanner.close();
                    db.disconnectDB();
                    isAlive = false;
                    break;
                }
                case "/help": {
                    System.out.println("Help info here");
                    break;
                }
            }
        }
    }


    public Scanner getScanner() {
        return scanner;
    }
}