package com.example.cscserver.Data;

import com.example.cscserver.Model.City;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<String, HashMap<String, ArrayList<City>>> data = new HashMap<>();

    private boolean writing = false;
    private boolean deleting = false;

    /**
     * Stores a new city.
     * @return the appropriate http response //TODO improve wording.
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
            data.get(city.getCountry()).get(city.getState()).add(city);

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
     * Deletes a stored city
     * @param city the city to delete.
     * @returnthe appropriate http response //TODO improve wording.
     * @throws InterruptedException InterruptedException when the server is interrupted.
     */
    @Async
    public synchronized CompletableFuture<ResponseEntity<?>> deleteCity(City city)
        throws InterruptedException {
        ResponseEntity<?> responseEntity =
                new ResponseEntity<>(HttpStatus.NOT_FOUND);

        // Check that the city exists.
        if (hasCity(city)) {
            // If another thread is modifying the data, wait.
            if (writing) {
                wait();
            }
            // The city exits, remove it.
            deleting = true;
            data.get(city.getCountry()).get(city.getState()).remove(city);
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
                ArrayList<City> citiesInState = data.get(city.getCountry()).get(city.getState());
                for (City storedCity : citiesInState) {
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
