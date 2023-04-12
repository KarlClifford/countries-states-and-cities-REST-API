package com.example.cscserver.configuration;

import java.util.ArrayList;

/**
 * A wrapper class used to wrap City objects before json serialisation.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class CityWrapper {
    /**
     * Cities found in get requests, can be BasicCity or City objects.
     */
    private final ArrayList<?> cities;

    /**
     * The constructor of this class.
     * @param cities the cities found in the get requests.
     */
    public CityWrapper(ArrayList<?> cities) {
        this.cities = cities;
    }
}
