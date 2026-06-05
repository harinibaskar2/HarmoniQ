package com.harmoniq;





/**
 * Stores database configuration settings used by the application.
 *
 * Contains the database file path and the JDBC connection URL
 * required to connect to the SQLite database.
 *
 * @author Harini Baskar
 */



public class DBConfig {


    public static final String DB_PATH =
        "/Users/harinibaskar/Desktop/College3rdYear/winterquarter/HarmoniQ/backend/users.db";



    public static final String DB_URL =
        "jdbc:sqlite:" + DB_PATH;
}


