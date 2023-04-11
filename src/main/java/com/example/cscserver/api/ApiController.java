package com.example.cscserver.api;

import com.example.cscserver.Data.DataService;
import com.example.cscserver.Model.City;
import com.example.cscserver.Model.ErrorMessage;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jdk.jfr.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * The controller for the API which handles requests from the client.
 * @author Karl Clifford
 * @version 1.0.0
 */
@Singleton
@RestController
@Validated
@RequestMapping("/api/v1")
public class ApiController {

    /**
     * The service that handles CRUD operations on the server data.
     */
    DataService data = new DataService();

    /**
     * Handles server logs.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(ApiController.class);

    /**
     * Handles storing requests from the client.
     */
    private final HttpServletRequest request;

    /**
     * The constructor of the API.
     * @param request the request made from the client.
     */
    public ApiController(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Just a simple query to check the service is running.
     * @param name the user's name.
     * @return a greeting to the user.
     */
    @GetMapping(value = "/hello", produces = {"text/plain"})
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    /**
     * The addCity method, verifies and adds a city to the server.
     * @param city the city to add.
     * @param errors errors caught during validation.
     * @return response code 201/202 if successful, 400 due to bad JSON formatting,
     * 409 if the city already exists and 500 if there was a server error.
     *
     */
    @PostMapping(value = "/city", consumes = {"application/json"})
    public ResponseEntity<?> addCity(@Valid @RequestBody City city, Errors errors) {
        // Check we passed the validation step.
        if (errors.hasErrors()) {
            // The JSON was malformed, return an error to the user.
            return errorResponse(errors);
        }

        // Check the date the user has entered is valid.
        if (!city.isDateValid()) {
            // The date isn't valid display an error message.
            ErrorMessage errorMessage = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    "Date must be in the present or the past and in the format dd-MM-yyyy");
            return new ResponseEntity<>(errorMessage.toJson(), HttpStatus.BAD_REQUEST);
        }

        // Try to add the city.
        ResponseEntity<?> response;
        try {
            response = data.storeCity(city).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    //TODO: Comment.
    @DeleteMapping("/city")
    public ResponseEntity<?> deleteCity(
            @NotBlank @QueryParam(value = "name") String name,
            @NotBlank @QueryParam(value = "state") String state,
            @NotBlank @QueryParam(value = "country") String country,
            Errors errors) {

        // Check we passed the validation step.
        if (errors.hasErrors()) {
            // The query parameters were malformed, return an error to the user.
            return errorResponse(errors);
        }

        // Try to delete the city.
        ResponseEntity<?> response;
        try {
            response = data.removeCity(name, state, country).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    // TODO: comment for methods.
    @GetMapping(value = "/city", produces = {"application/json"})
    public ResponseEntity<?> getCities(@QueryParam("dateFounded") String date) {
        // Try to get the cities.
        ResponseEntity<?> response;
        try {
            response = data.getCities(null, null, date).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    @GetMapping(value = "city/{country}", produces = {"application/json"})
    public ResponseEntity<?> getCitiesByCountry(@PathVariable("country") String country) {
        // Try to get the cities.
        ResponseEntity<?> response;
        try {
            response = data.getCities(country, null, null).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
    @GetMapping(value = "city/{country}/{state}", produces = {"application/json"})
    public ResponseEntity<?> getCitiesByCountry(
            @PathVariable("country") String country,
            @PathVariable("state") String state) {
        // Try to get the cities.
        ResponseEntity<?> response;
        try {
            response = data.getCities(country, state, null).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    /**
     * Builds json objects containing errors thrown by the server.
     * @param errors the errors that were caught.
     * @return a 400 error response.
     */
    private static ResponseEntity<String> errorResponse(Errors errors) {
        ArrayList<ErrorMessage> errorMessages = new ArrayList<>();
        // Iterate through every error and save the message.
        for (ObjectError error : errors.getAllErrors()) {
            errorMessages.add(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage()));
        }

        Gson gson = new Gson();
        return new ResponseEntity<>(gson.toJson(errorMessages), HttpStatus.BAD_REQUEST);
    }
}
