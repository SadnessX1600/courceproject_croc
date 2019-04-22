package com.sadnessx.course.project;


import com.sadnessx.course.project.console.application.ConsoleApp;
import com.sadnessx.course.project.handlers.DBHandler;
import com.sadnessx.course.project.handlers.IOHandler;


public class MainApp {
    public static void main(String[] args) {
        DBHandler db = new DBHandler();
        IOHandler io = new IOHandler();
        ConsoleApp consoleApp = new ConsoleApp(db, io);
        consoleApp.start();
    }
}
