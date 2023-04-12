package com.example.cscserver.configuration;

import com.example.cscserver.Model.City;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * This class is used to compare City objects by date.
 * @author Karl Clifford
 * @version 1.0.0
 */
public class CityComparator implements Comparator<City> {

    /**
     * Compares one city to another.
     * @param o1 the first city to be compared.
     * @param o2 the second city to be compared.
     * @return Whether o1 is newer, older or the same age as o2.
     */
    @Override
    public int compare(City o1, City o2) {
        Date date1;
        Date date2;

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        // Assign the dates from the city to Date types, so we can compare.
        try {
            date1 = formatter.parse(o1.getFoundingDate());
            date2 = formatter.parse(o2.getFoundingDate());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        int comparatorValue;

        if (date1.before(date2)) {
            // City o1 is older.
            comparatorValue = 1;
        } else if (date1.after(date2)) {
            // City o1 is newer.
            comparatorValue = -1;
        } else {
            // There is no difference in dates.
            comparatorValue = 0;
        }

        return comparatorValue;
    }
}
