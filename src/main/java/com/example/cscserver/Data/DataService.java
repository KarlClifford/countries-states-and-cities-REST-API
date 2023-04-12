package com.example.cscserver.Data;

import com.example.cscserver.Model.BasicCity;
import com.example.cscserver.Model.City;
import com.example.cscserver.configuration.CityComparator;
import com.example.cscserver.configuration.CityWrapper;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class stores and handles data stored in the server asynchronously.
 * @author Karl Clifford
 * @version 1.0.0
 */
@Service
public class DataService {

    /**
     * Stores country, state and cities.
     */
    private HashMap<String, HashMap<String, ArrayList<BasicCity>>> data = new HashMap<>();

    private boolean writing = false;
    private boolean deleting = false;

    /**
     * Stores a new city.
     * @return the appropriate http response. //TODO improve wording.
     * @throws InterruptedException when the server is interrupted.
     */
    @Async
    public synchronized CompletableFuture<ResponseEntity<?>> storeCity(City city)
            throws InterruptedException {
        ResponseEntity<?> responseEntity;

        // Check the city doesn't already exist.
        if (!hasCity(city)) {
            // If another thread is modifying the data, wait.
            if (deleting) {
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

            responseEntity = ResponseEntity.status(HttpStatus.OK).body(null);


        } else {
            // The city already exists, return 409 error.
            responseEntity = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        return CompletableFuture.completedFuture(responseEntity);
    }

    /**
     * Deletes a stored city //TODO: This comment needs to params added.
     * @return the appropriate http response. //TODO improve wording.
     * @throws InterruptedException InterruptedException when the server is interrupted.
     */
    @Async
    public synchronized CompletableFuture<ResponseEntity<?>> removeCity(String name, String country, String state) //TODO change to openapi sepc
        throws InterruptedException {
        ResponseEntity<?> responseEntity =
                new ResponseEntity<>(HttpStatus.NOT_FOUND);

        City city = new City(name, state, country, null);

        // Check that the city exists.
        if (hasCity(city)) {
            // If another thread is modifying the data, wait.
            if (writing) {
                wait();
            }
            // The city exits, remove it.
            deleting = true;
            data.get(city.getCountry()).get(city.getState()).remove(
                    new BasicCity(city.getName(), city.getFoundingDate()));
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
                // No state so get all the states in this country.
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

        if (country == null && state == null) {
            // We need to return the most complex data.
            ArrayList<City> cities = new ArrayList<>();
            if (!(date == null)) {
                // Filter the cities by date.
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                Date maxDate;
                try {
                    maxDate = formatter.parse(date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                while (!cityData.isEmpty()) {
                    // Remove the top city.
                    City city = cityData.poll();
                    // Store this cities date.
                    Date cityDate;
                    try {
                        cityDate = formatter.parse(city.getFoundingDate());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    // Add all the cities from newest to oldest with dates less the maxDate.
                    if (cityDate.before(maxDate)) {
                        cities.add(city);
                    }
                }
            } else {
                // Don't filter the cities by date.
                while (!cityData.isEmpty()) {
                    // Remove the top city.
                    City city = cityData.poll();
                    // Add the city.
                    cities.add(city);
            }
        }
            sortedData = gson.toJson(new CityWrapper(cities));
    } else {
            // We need to simplify our data
            ArrayList<BasicCity> cities = new ArrayList<>();
            while (!cityData.isEmpty()) {
                // Remove the top city.
                City city = cityData.poll();
                // Simplify the data.
                BasicCity simplifiedCity = new BasicCity(city.getName(), city.getFoundingDate());
                // Add the city.
                cities.add(simplifiedCity);
            }
            sortedData = gson.toJson(new CityWrapper(cities));
        }

        // See if we have any data.
        if (!sortedData.equals("[]")) {
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
        while (writing || deleting) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        boolean cityFound = false;

        // Find the country.
        if (data.containsKey(city.getCountry())) {
            // The country exists, check if the state exists.
            if (data.get(city.getCountry()).containsKey(city.getState())) {
                // The state exists, check that the city exist.
                ArrayList<BasicCity> citiesInState = data.get(city.getCountry()).get(city.getState());
                for (BasicCity storedCity : citiesInState) {
                    if (storedCity.getName().equals(city.getName())) {
                        // The city was found.
                        cityFound = true;
                        break; //TODO check this can be here!!!
                    }
                }
            }
        }

        return cityFound;
    }
}
