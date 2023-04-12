package com.example.cscserver.configuration;

import com.example.cscserver.Model.ErrorMessage;

import java.util.ArrayList;

/**
 * A wrapper class for errors thrown by the server, used in json serialisation.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class ErrorWrapper {
    /**
     * The errors thrown by the server.
     */
    private final ArrayList<ErrorMessage> errors;

    /**
     * The constructor of this class.
     * @param errors errors thrown by the server.
     */
    public ErrorWrapper(ArrayList<ErrorMessage> errors) {
        this.errors = errors;
    }
}
