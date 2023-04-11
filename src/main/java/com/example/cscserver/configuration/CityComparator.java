package com.example.cscserver.configuration;

import com.example.cscserver.Model.City;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

public class CityComparator implements Comparator<City> {

    @Override
    public int compare(City o1, City o2) {
        Date date1;
        Date date2;

        // Assign the dates from the city to Date types, so we can compare.
        try {
            date1 = DateFormat.getDateInstance().parse(o1.getFoundingDate());
            date2 = DateFormat.getDateInstance().parse(o2.getFoundingDate());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        int comparatorValue;

        if(date1.before(date2)) {
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
