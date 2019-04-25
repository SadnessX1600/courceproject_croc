package com.sadnessx.course.project.console.application;


import com.sadnessx.course.project.handlers.DBHandler;
import com.sadnessx.course.project.handlers.IOHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ConsoleApp {
    private Scanner scanner = new Scanner(System.in);
    private DBHandler db;
    private IOHandler io;
    private Set<Class<?>> tables;
    private String cd = "";
    private boolean found = false;
    private static final Logger LOGGER = LogManager.getLogger(ConsoleApp.class);

    public ConsoleApp(DBHandler db, IOHandler io) {
        this.db = db;
        this.io = io;
        this.tables = db.getTablesSet();
    }

    public void start() {
        boolean isAlive = true;
        while (isAlive) {
            System.out.println("Waiting for command");
            String cmd = scanner.nextLine();
            String[] cmdSplitted = cmd.split(" ");
            switch (cmdSplitted[0]) {
                case "/cd": {
                    try {
                        cd = cmdSplitted[1] + "/";
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.error("Empty path");
                        break;
                    }
                }
                case "/connect": {
                    try {
                        db.connectDB(cd + cmdSplitted[1]);
                        LOGGER.info("Connected");
                        break;
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        break;
                    }

                }
                case "/createTable": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.createTable(table, true);
                                found = true;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.error("Incorrect output");
                    }
                }
                case "/deleteTable": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.deleteTable(table);
                                found = true;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.error("Incorrect input");
                    }
                }
                case "/clearTable": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.clearTable(table);
                                found = true;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.error("Incorrect input");
                    }
                }
                case "/importAll": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.addEntitiesList(io.importEntitiesToList(table, cd + cmdSplitted[2]));
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (RuntimeException e) {
                        LOGGER.error("Incorrect input");
                        break;
                    }
                }
                case "/add": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.addEntity(table, this);
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (RuntimeException e) {
                        LOGGER.error("Incorrect input");
                        break;
                    }
                }
                case "/exportAll": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                io.exportFromListToXml(cd + cmdSplitted[2], db.getEntitiesList(table));
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (RuntimeException e) {
                        LOGGER.error("Incorrect input");
                        break;
                    }
                }
                case "/findByField": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.showResultSet(db.findByField(table, cmdSplitted[2], cmdSplitted[3]));
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (RuntimeException e) {
                        LOGGER.error("Incorrect input");
                        break;
                    }
                }
                case "/getAll": {
                    try {
                        for (Class<?> table : tables) {
                            if (table.getSimpleName().equals(cmdSplitted[1])) {
                                db.showResultSet(db.getAllFromDB(table));
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            found = false;
                            break;
                        }
                        LOGGER.error("Defined class not found or doesn't have annotation @Generate table");
                        break;
                    } catch (RuntimeException e) {
                        LOGGER.error("Incorrect input");
                        break;
                    }
                }
                case "/findAvg": {
                    try {
                        db.showResultSet(db.calculateAverageScoreFromSpec(Integer.valueOf(cmdSplitted[1])));
                        break;
                    } catch (InputMismatchException e) {
                        LOGGER.error("Incorrect input");
                        break;
                    }
                }
                case "/disconnect": {
                    db.disconnectDB();
                    LOGGER.info("Disconnected");
                    break;
                }
                case "/quit": {
                    scanner.close();
                    db.disconnectDB();
                    isAlive = false;
                    LOGGER.info("Quitting");
                    break;
                }
                case "/help": {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\nList of commands:\n" +
                            "Set current directory /cd path" +
                            "\nConnect to database /connect path\n" +
                            "Create table /createTable Classname\n" +
                            "Delete table /deleteTable Classname\n" +
                            "Clear table /clearTable Classname\n" +
                            "Import to table from xml /importAll Classname path\n" +
                            "Export from table /exportAll Classname path\n" +
                            "Find value by field /findByField Classname Fieldname value\n" +
                            "Show all values from DB /getAll Classname\n" +
                            "Find average score by speciality id /findAvg Speciality_id\n" +
                            "Disconnect from current DB /disconnect\n" +
                            "Exit and close connection /quit\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    break;
                }
            }
        }
    }


    public Scanner getScanner() {
        return scanner;
    }
}