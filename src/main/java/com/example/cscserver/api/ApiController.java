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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import java.util.List;
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
     * Determines whether a date is in the format yyyy-MM-dd (excluding 0000-00-00) or a 32-bit epoch.
     */
    private static final String VALID_DATE =
            "^(?!0000-00-00)(\\d{4}-\\d{2}-\\d{2}|(19|20)\\d{8}|214748364[0-7]|-214748364[0-8])(?:Z|[+-]\\d{2}:\\d{2})?$";

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
     * @return response code 204 if successful, 400 due to bad JSON formatting,
     * 409 if the city already exists and 500 if there was a server error.
     */
    @PostMapping(value = "/city", consumes = {"application/json"})
    public ResponseEntity<?> addCity(@Valid @RequestBody City city) {
        ResponseEntity<?> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        /*
         * Spring boot automatically passes values to the City object,
         * we need to check that the date is valid.
         */
        if (city.getFoundingDate().matches(VALID_DATE)) {
            // Check the date the user has entered is before the current date.
            if (!city.isDateValid()) {
                // The date isn't valid display an error message.
                ErrorMessage errorMessage = new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                        "Date must be in the present or the past and in the format dd-MM-yyyy");
                return new ResponseEntity<>(errorMessage.toJson(), HttpStatus.BAD_REQUEST);
            }

            // Try to add the city.
            try {
                response = data.storeCity(city).get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error adding city " + city.getName() + ": " + e);
            }
        } else {
            // Date failed to parse, show error.
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    "Date must match format yyyy-MM-dd or epoch timestamp");
            response = new ResponseEntity<>(error.toJson(), HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    /**
     * The deleteCity method deletes a user defined city from the server.
     * @param name the name of the city to target.
     * @param state the state of the city to target.
     * @param country the country of the city to target.
     * @return response code 204 if success, 409 due to bad formatting or 404 if the city doesn't exist.
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
        // Check for valid date.
        if (date != null && date.matches(VALID_DATE)) {
            // Try to get the cities.
            try {
                response = data.getCities(null, null, date).get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error getting all cities: " + e);
            }
        } else {
            // Not a valid date show error.
            ErrorMessage error = new ErrorMessage(
                    HttpStatus.BAD_REQUEST.value(),
                    "Date must match format yyyy-MM-dd or epoch timestamp");
            response = new ResponseEntity<>(error.toJson(), HttpStatus.BAD_REQUEST);
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
     * Builds json objects containing constraint errors thrown by the server.
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

    /**
     * Builds json objects containing errors thrown by the server,
     * caused by malformed dates.
     * @param ex the constraint violations from the server.
     * @return 400 error response.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ArrayList<ErrorMessage> errorMessages = new ArrayList<>();

        // Save the error message.
        errorMessages.add(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Body"));

        Gson gson = new Gson();
        return ResponseEntity.badRequest().body(gson.toJson(new ErrorWrapper(errorMessages)));
    }

    /**
     * Builds json objects containing method argument errors thrown by the server.
     * @param ex the constraint violations from the server.
     * @return 400 error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ArrayList<ErrorMessage> errorMessages = new ArrayList<>();

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        // Iterate through every error and save the message.
        for (FieldError error : fieldErrors) {
            errorMessages.add(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage()));
        }

        Gson gson = new Gson();
        return ResponseEntity.badRequest().body(gson.toJson(new ErrorWrapper(errorMessages)));
    }
}
