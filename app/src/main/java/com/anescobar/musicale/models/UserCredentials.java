package com.anescobar.musicale.models;

/**
 * Created by Andres Escobar on 7/14/14.
 * Represents user's credentials
 * Instance of this class is NEVER cached, but is simply used during authentication
 */
public class UserCredentials {
    public String username;
    public String password;

    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
