package com.example.cscserver.Model;

import com.google.gson.Gson;

import java.util.Date;

/**
 * This class represents an error message that we use for error handling of
 * API requests.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class ErrorMessage {

    /**
     * When the error was triggered.
     */
    private final Date timeStamp;
    /**
     * The error code, i.e. 400.
     */
    private final int errorCode;
    /**
     * The message we will share with the user.
     */
    private final String message;

    /**
     * The constructor of the error message.
     * @param errorCode the error code.
     * @param message the error message.
     */
    public ErrorMessage(int errorCode, String message) {
        this.timeStamp = new Date();
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Converts the object to json.
     * @return a JSON representation of this class.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
