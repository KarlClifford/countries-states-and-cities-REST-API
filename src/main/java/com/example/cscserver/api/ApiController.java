package com.example.cscserver.api;

import com.example.cscserver.Data.DataService;
import com.example.cscserver.Model.City;
import com.example.cscserver.Model.ErrorMessage;
import com.example.cscserver.configuration.ErrorWrapper;
import com.google.gson.Gson;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
     * Handles server logs.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(ApiController.class);

    /**
     * The service that handles CRUD operations on the server data.
     */
    private final DataService data = new DataService();

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
     * @return response code 201/202 if successful, 400 due to bad JSON formatting,
     * 409 if the city already exists and 500 if there was a server error.
     */
    @PostMapping(value = "/city", consumes = {"application/json"})
    public ResponseEntity<?> addCity(@Valid @RequestBody City city) {

        // Check the date the user has entered is valid.
        if (!city.isDateValid()) {
            // The date isn't valid display an error message.
            ErrorMessage errorMessage = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    "Date must be in the present or the past and in the format dd-MM-yyyy");
            return new ResponseEntity<>(errorMessage.toJson(), HttpStatus.BAD_REQUEST);
        }

        // Try to add the city.
        ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            response = data.storeCity(city).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error adding city " + city.getName() + ": " + e);
        }

        return response;
    }

    /**
     * The deleteCity method deletes a user defined city from the server.
     * @param name the name of the city to target.
     * @param state the state of the city to target.
     * @param country the country of the city to target.
     * @return response code 204 if success or 404 if the city doesn't exist.
     */
    @DeleteMapping("/city")
    public ResponseEntity<?> deleteCity(
            @Valid @NotBlank(message = "name must not be blank") @QueryParam("name") String name,
            @Valid @NotBlank(message = "state must not be blank") @QueryParam("state") String state,
            @Valid @NotBlank(message = "country must not be blank") @QueryParam("country") String country) {

        // Try to delete the city.
        ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            response = data.removeCity(name, state, country).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error deleting the city " + name + ": " + e);
        }

        return response;
    }

    /**
     * Gets all the cities stored on the server.
     * @param date (optional) maximum date to filter the cities by.
     * @return response code 200 if success with JSON city data or 404 if no cities exist.
     */
    @GetMapping(value = "/city", produces = {"application/json"})
    public ResponseEntity<?> getCities(@QueryParam("dateFounded") String date) {
        // Try to get the cities.
        ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            response = data.getCities(null, null, date).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error getting all cities: " + e);
        }

        return response;
    }

    /**
     * Gets all the cities stored on the server, filtered by country.
     * @param country the country to target.
     * @return response code 200 if success with JSON city data or 404 if no cities exist.
     */
    @GetMapping(value = "city/{country}", produces = {"application/json"})
    public ResponseEntity<?> getCitiesByCountry(@PathVariable("country") String country) {
        // Try to get the cities.
        ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            response = data.getCities(country, null, null).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error getting cities in country: " + country + e);
        }
        return response;
    }

    /**
     * Gets all the cities stored on the server, filtered by country and state.
     * @param country the country to target.
     * @param state the state to target.
     * @return response code 200 if success with JSON city data or 404 if no cities exist.
     */
    @GetMapping(value = "city/{country}/{state}", produces = {"application/json"})
    public ResponseEntity<?> getCitiesByCountry(
            @PathVariable("country") String country,
            @PathVariable("state") String state) {

        // Try to get the cities.
        ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            response = data.getCities(country, state, null).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error getting cities in country: " + country + " and state: " + state + e);
        }
        return response;
    }

    /**
     * Builds json objects containing errors thrown by the server.
     * @param ex the constraint violations from the server.
     * @return 400 error response.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        ArrayList<ErrorMessage> errorMessages = new ArrayList<>();

        // Iterate through every error and save the message.
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errorMessages.add(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), violation.getMessage()));
        }

        Gson gson = new Gson();
        return ResponseEntity.badRequest().body(gson.toJson(new ErrorWrapper(errorMessages)));
    }
}
