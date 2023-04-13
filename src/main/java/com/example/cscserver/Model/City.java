package com.example.cscserver.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a City object used for JSON serialisation.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class City {

    /**
     * Epoch date.
     */
    private static final int[] EPOCH_DATE = {1970, 1, 1};
    /**
     * Helps determine if a date is in epoch format or in date format.
     */
    private static final int MINIMUM_VIABLE_DATE_LENGTH = 4;
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
    @JsonProperty("foundingDate")
    @NotBlank(message = "date founded is required")
    @Pattern(regexp =
            "^(-?[1-9]\\d{0,8}|0)$|^(?!0000)[1-9]\\d{3}[-/.](?:0[1-9]|1[0-2])[-/.](?:0[1-9]|[12]\\d|3[01])$",
            message = "date must match pattern yyyy-MM-dd or -99999 to 99999")
    private String foundingDate;

    /**
     * Stores the date in the correct format.
     */
    private transient LocalDate formattedDate;

    /**
     * The constructor of the City class.
     * @param name the name of the city.
     * @param state the state this city is in.
     * @param country the country the state is in.
     * @param foundingDate the date the city was founded.
     */
    public City(String name, String state, String country, String foundingDate) {
        this.name = name;
        this.state = state;
        this.country = country;
        //this.foundingDate = foundingDate;
        if (!foundingDate.isEmpty()) {
            this.formattedDate = parseDate(foundingDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            this.foundingDate = this.formattedDate.format(formatter);
        }
    }

    /**
     * Gets the name of the city.
     * @return the name of the city.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the state this city belongs too.
     * @return the state the city is in.
     */
    public String getState() {
        return state;
    }

    /**
     * Gets the country this city belongs too.
     * @return the country the city is in.
     */
    public String getCountry() {
        return country;
    }

    /**
     * The date this city was founded in.
     * @return the date the city was founded in.
     */
    public String getFoundingDate() {
        return foundingDate;
    }

    /**
     * The date this city was founded in, with the correct format.
     * @return the date the city was founded in.
     */
    public LocalDate getDate() {
        return formattedDate;
    }

    /**
     * Determined whether an entered date is valid or not.
     * @return true if the date is in the present or the past.
     */
    public boolean isDateValid() {
        boolean isValid;
        // Check if the date is before or after the present date.
        LocalDate dateNow = LocalDate.now();
        isValid = dateNow.isAfter(formattedDate);
        return isValid;
    }

    /**
     * Converts a string representation of a date in the format -999999999 to
     * 999999999 or a valid date format dd-MM-yyyy to a LocalDate.
     * @param date the date to convert.
     * @return the date.
     */
    public static LocalDate parseDate(String date) {
        LocalDate formattedDate;
        // Determine if the date is in a date format or integer format.
        if ((date.charAt(0) == '-') || (date.length() <= MINIMUM_VIABLE_DATE_LENGTH) || date.matches("^\\d{5}.*")) {
            // Date is in integer format, convert using the days since epoch.
            long daysSinceEpoch = Long.parseLong(date);
            LocalDate epochDate = LocalDate.of(EPOCH_DATE[0], EPOCH_DATE[1], EPOCH_DATE[2]);
            formattedDate = epochDate.plusDays(daysSinceEpoch);
        } else {
            // Date is in a date format, split it to convert to a LocalDate.
            String[] splitDate = date.replaceAll("[-/.]", " ").split(" ");
            formattedDate = LocalDate.of(Integer.parseInt(splitDate[0]),
                    Integer.parseInt(splitDate[1]),
                    Integer.parseInt(splitDate[2]));
        }
        return formattedDate;
    }
}
