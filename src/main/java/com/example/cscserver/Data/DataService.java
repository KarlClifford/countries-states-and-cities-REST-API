package com.example.cscserver.Data;

import com.example.cscserver.Model.BasicCity;
import com.example.cscserver.Model.City;
import com.example.cscserver.configuration.CityComparator;
import com.example.cscserver.configuration.CityWrapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * This class stores and handles data stored in the server asynchronously.
 * @author Karl Clifford
 * @version 1.0.0
 */
@Service
public class DataService {

    /**
     * Handles server logs.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(DataService.class);

    /**
     * Stores country, states and cities.
     */
    private final HashMap<String, HashMap<String, ArrayList<BasicCity>>> data = new HashMap<>();

    /**
     * Whether data is being written.
     */
    private boolean writing = false;
    /**
     * Whether data is being deleted.
     */
    private boolean deleting = false;

    /**
     * Stores a new city.
     * @param city the city to store.
     * @return response code 204 if success, 409 due to bad formatting or 404 if the city doesn't exist.
     * @throws InterruptedException if the operation is cancelled.
     */
    @Async
    public synchronized CompletableFuture<ResponseEntity<?>> storeCity(City city)
            throws InterruptedException {
        ResponseEntity<?> responseEntity;

        // Check the city doesn't already exist.
        if (!hasCity(city)) {
            // If another thread is modifying the data, wait.
            while (deleting) {
                wait();
            }

            // Prevent other threads from making changes to the data.
            writing = true;
            // Check the country exists.
            if (!data.containsKey(city.getCountry())) {
                // Country doesn't exist so add it.
                data.put(city.getCountry(), new HashMap<>());
            }

            // Check the state exists.
            if (!data.get(city.getCountry()).containsKey(city.getState())) {
                // The state doesn't exist so add it.
                data.get(city.getCountry()).put(city.getState(), new ArrayList<>());
            }

            // Add the city.
            data.get(city.getCountry()).get(city.getState()).add(new BasicCity(city.getName(), city.getFoundingDate()));

            // Wake up waiting threads.
            writing = false;
            notifyAll();

            // Inform the user the operation was successful.
            responseEntity = ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);


        } else {
            // The city already exists, return 409 error.
            responseEntity = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        return CompletableFuture.completedFuture(responseEntity);
    }

    /**
     * Deletes a stored city.
     * @param name the name of the city to target.
     * @param state the name of the state to target.
     * @param country the name of the country to target.
     * @return response code 204 if success or 404 if the city doesn't exist.
     * @throws InterruptedException if the operation is cancelled.
     */
    @Async
    public synchronized CompletableFuture<ResponseEntity<?>> removeCity(String name, String state, String country)
        throws InterruptedException {
        ResponseEntity<?> responseEntity =
                new ResponseEntity<>(HttpStatus.NOT_FOUND);

        City city = new City(name, state, country, null);

        // Check that the city exists.
        if (hasCity(city)) {
            // If another thread is modifying the data, wait.
            while (writing) {
                wait();
            }
            // The city exits, remove it.
            deleting = true;

            // Try and find the city in the state.
            // We found the city so remove it.
            data.get(city.getCountry())
                    .get(city.getState()).removeIf(myCity -> myCity.getName().equals(city.getName()));
            // Check that we still have some states in this country.
            if (data.get(city.getCountry()).get(city.getState()).isEmpty()) {
                // State is empty, delete the state.
                data.get(city.getCountry()).remove(city.getState());
                // Check that the country still has some states in it.
                if (data.get(city.getCountry()).isEmpty()) {
                    // Country contains no states, remove the country.
                    data.remove(city.getCountry());
                }
            }

            // Release the lock.
            deleting = false;
            notifyAll();

            // The data was deleted successfully, return success response.
            responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return CompletableFuture.completedFuture(responseEntity);
    }

    /**
     * Get the cities.
     * @param country (optional) country to filter.
     * @param state (optional) state to filter.
     * @param date (optional) date to filter.
     * @return response code 200 if success with JSON city data or 404 if no cities exist.
     */
    @Async
    public synchronized CompletableFuture<ResponseEntity<?>> getCities(String country, String state, String date) {
        ResponseEntity<?> responseEntity =
                new ResponseEntity<>(HttpStatus.NOT_FOUND);

        PriorityQueue<City> cityData = new PriorityQueue<>(new CityComparator());

        if (country == null && state == null) {
            // Get the dataset attached to each country.
            for (Map.Entry<String, HashMap<String, ArrayList<BasicCity>>> countr : data.entrySet()) {
                // Get the dataset attached to each state.
                for (Map.Entry<String, ArrayList<BasicCity>> st : countr.getValue().entrySet()) {
                    // Go through every stored city and add it to the cityData queue, sorted by date.
                    for (BasicCity city : st.getValue()) {
                        cityData.add(new City(city.getName(), st.getKey(), countr.getKey(), city.getFoundingDate()));
                    }
                }
            }
        } else {
            // Get all cities by country (and state).
            if (state == null && data.containsKey(country)) {
                // No user defined state so get all the states in this country.
                for (Map.Entry<String, ArrayList<BasicCity>> stateData : data.get(country).entrySet()) {
                    // Add all the cities in every state.
                    for (BasicCity city : stateData.getValue()) {
                        cityData.add(new City(city.getName(), stateData.getKey(), country, city.getFoundingDate()));
                    }
                }
            } else {
                // We need to also filter by state, get by country and state.
                if (data.containsKey(country)) {
                    // Check the state exists in this country.
                    if (data.get(country).containsKey(state)) {
                        // The state exists, traverse through them.
                        for (BasicCity city : data.get(country).get(state)) {
                            // Add all cities in the defined state.
                            cityData.add(new City(city.getName(), state, country, city.getFoundingDate()));
                        }
                    }
                }
            }
        }

        Gson gson = new Gson();
        String sortedData;

        // Decide if we will produce complex City objects or simplified BasicCity objects.
        if (country == null && state == null) {
            // We need to return the most complex data.
            ArrayList<City> cities = new ArrayList<>();
            // Determine if we need to filter by date or if we can just return the data as is.
            if (!(date == null)) {
                // We need to filter the cities by date.
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                Date maxDate = new Date();
                try {
                    maxDate = formatter.parse(date);
                } catch (ParseException e) {
                    LOG.error("Date couldn't be parsed: " + e);
                }
                // Search through all the cities we collected.
                while (!cityData.isEmpty()) {
                    // Remove the top city.
                    City city = cityData.poll();
                    // Store this cities date.
                    Date cityDate = new Date();
                    try {
                        cityDate = formatter.parse(city.getFoundingDate());
                    } catch (ParseException e) {
                        LOG.error("Date couldn't be parsed: " + e);
                    }
                    // Add all the cities from newest to oldest with dates less the maxDate.
                    if (cityDate.before(maxDate)) {
                        cities.add(city);
                    }
                }
            } else {
                // Don't filter the cities by date, search through all the cities.
                while (!cityData.isEmpty()) {
                    // Remove the top city.
                    City city = cityData.poll();
                    // Add the city.
                    cities.add(city);
            }
        }
            // Convert the data to JSON format.
            sortedData = gson.toJson(new CityWrapper(cities));
    } else {
            // We need to simplify our data.
            ArrayList<BasicCity> cities = new ArrayList<>();
            while (!cityData.isEmpty()) {
                // Remove the top city.
                City city = cityData.poll();
                // Simplify the data.
                BasicCity simplifiedCity = new BasicCity(city.getName(), city.getFoundingDate());
                // Add the city.
                cities.add(simplifiedCity);
            }
            // Convert the data to JSON format.
            sortedData = gson.toJson(new CityWrapper(cities));
        }

        // See if we have any data.
        if (!sortedData.equals("{\"cities\":[]}")) {
            // We have data, send it.
            responseEntity = new ResponseEntity<>(sortedData, HttpStatus.OK);
        }

        return CompletableFuture.completedFuture(responseEntity);
    }

    /**
     * Checks if this city already exists on the server.
     * @param city the city we want to find.
     * @return true if the city exists.
     */
    private synchronized boolean hasCity(City city) {
        // Wait while other threads are modifying the data.
        while (writing || deleting) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Find the country.
        if (data.containsKey(city.getCountry())) {
            // The country exists, check if the state exists.
            if (data.get(city.getCountry()).containsKey(city.getState())) {
                // The state exists, check that the city exist.
                ArrayList<BasicCity> citiesInState = data.get(city.getCountry()).get(city.getState());
                for (BasicCity storedCity : citiesInState) {
                    if (storedCity.getName().equals(city.getName())) {
                        // The city was found.
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
