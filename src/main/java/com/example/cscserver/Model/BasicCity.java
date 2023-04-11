package com.example.cscserver.Model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents a City for storing limited ciy data.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class BasicCity {
    /**
     * The name of the city.
     */
    private final String name;

    /**
     * The date the city was founded in.
     */
    private final String foundingDate;

    public BasicCity(String name, String foundingDate) {
        this.name = name;
        this.foundingDate = foundingDate;
    }

    /**
     * Gets the name of the city.
     * @return the name of the city.
     */
    public String getName() {
        return name;
    }

    /**
     * The date this city was founded in.
     * @return the date the city was founded in.
     */
    public String getFoundingDate() {
        return foundingDate;
    }

    //TODO: Strings should have double quotes around them in JSON.
    @Override
    public String toString() {
        return "{\nname: " + getName() + "\ndateFounded: " + getFoundingDate() + "\n}";
    }
}
