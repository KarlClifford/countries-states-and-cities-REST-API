package com.example.cscserver.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a City object used for JSON serialisation.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class City {
    /**
     * The name of the city.
     */
    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The state the city is situated in.
     */
    @JsonProperty("state")
    @NotBlank(message = "state is required")
    private String state;

    /**
     * The country the state is situated in.
     */
    @JsonProperty("country")
    @NotBlank(message = "country is required")
    private String country;

    /**
     * The date the city was founded in the format dd-MM-yyyy.
     */
    @JsonProperty("dateFounded")
    @NotBlank
    @Pattern(regexp = "(0?[1-9]|[12][0-9]|3[01])[- /.](0?[1-9]|1[012])[- /.](19|20)\\d\\d")
    private String foundingDate;

    /**
     * Determined whether an entered date is valid or not.
     * @return true if the date is in the present or the past.
     */
    public boolean isDateValid() {
        // Setup a date formatter so we can convert the string representation to an actual Date object.
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        boolean isValid;
        try {
            // Check if the date is before or after the present date.
            isValid = new Date().after(format.parse(foundingDate));
        } catch (ParseException e) {
            // The date is malformed.
            isValid = false;
        }
        return isValid;
    }
}
