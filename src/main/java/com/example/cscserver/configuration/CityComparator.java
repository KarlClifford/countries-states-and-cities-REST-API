package com.example.cscserver.configuration;

import com.example.cscserver.Model.City;
import com.example.cscserver.api.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * This class is used to compare City objects by date.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class CityComparator implements Comparator<City> {
    /**
     * Handles server logs.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(ApiController.class);

    /**
     * Compares one city to another.
     * @param o1 the first city to be compared.
     * @param o2 the second city to be compared.
     * @return Whether o1 is newer, older or the same age as o2.
     */
    @Override
    public int compare(City o1, City o2) {
        // Assign the dates from the city to Date types, so we can compare.
        LocalDate date1 = o1.getDate();
        LocalDate date2 = o2.getDate();

        return date1.compareTo(date2);
    }
}
