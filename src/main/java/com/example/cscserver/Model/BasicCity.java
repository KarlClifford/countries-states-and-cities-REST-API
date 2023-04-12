package com.example.cscserver.Model;

/**
 * Represents a City for storing limited city data.
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

    /**
     * The constructor of this class.
     * @param name the name of the city.
     * @param foundingDate the date this city was founded.
     */
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
}
