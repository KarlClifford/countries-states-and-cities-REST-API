# Countries, States and Cities (CSC) API

CSC API 🏙️

- API version: 1.0.0

## Requirements
Building the API server requires:
1. Java version 1.7+
2. Maven

## Installation

To install the server dependencies to your local machine, simply execute:

```shell
mvn clean install
```

## Getting Started

To run the server please follow the [installation](#installation) instruction and execute the following:

```shell
mvn spring-boot:run
```

>**Note**: This server will bind to port **8080**.

## Documentation for API Endpoints



All URIs are relative to *http://localhost:8080/api/v1*



Class | Method | HTTP request | Description

------------ | ------------- | ------------- | -------------

*CityApi* | [**addCity**](http://localhost:8080/docs.html/CityApi.md#addCity) | **POST** /city | Adds a new city to the server

*CityApi* | [**deleteCity**](http://localhost:8080/docs.html/CityApi.md#deleteCity) | **DELETE** /city | Deletes a city from the server

*CityApi* | [**getCities**](http://localhost:8080/docs.html/CityApi.md#getCities) | **GET** /city | Get all cities

*CityApi* | [**getCityByCountry**](http://localhost:8080/docs.html/CityApi.md#getCityByCountry) | **GET** /city/{country} | Get all cities by country

*CityApi* | [**getCityByCountryState**](http://localhost:8080/docs.html/CityApi.md#getCityByCountryState) | **GET** /city/{country}/{state} | Get all cities by country and state

>**Note**: Please visit *http://localhost:8080/docs.html* for comprehensive documentation.


## Author



2108602@swansea.ac.uk