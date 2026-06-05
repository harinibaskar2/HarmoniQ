package com.harmoniq;

/**
 * Represents a user in the HarmoniQ system.
 *
 * Stores basic user information including the user ID,
 * username, and hashed password. This class is used for
 * authentication and user management.
 *
 * The password is stored as a hash for security purposes.
 *
 * @author Harini Baskar
 */

public class User {
    private int id;
    private String username;
    private String passwordHash;

    public User() {} // default constructor for Gson

    public User(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
