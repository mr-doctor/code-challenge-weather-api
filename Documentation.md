# Java Coding Challenge - Weather API Documentation

The code for the API is all contained within the `nz.co.solnet.api` package. 
I did not modify the functionality of any other Java code. I did change the `webapp` code to expose
API endpoints. 

## Running the API
I have not changed the method of running the challenge. 
Run `mvn jetty:run` in the project base folder, as before.

The API will be up on [http://localhost:8080](http://localhost:8080).

This should load `index.html`, with a set of simple HTML forms to help with demonstration.

## Demonstration

### Requirement 1 - Fetch data for all cities
Go to [http://localhost:8080/get](http://localhost:8080/get). This
 should load all records in the SQL database. They will appear as JSON, so I recommend some form
 of browser extension to view the JSON better.
 
 ### Requirement 2 - Fetch data for a single record
 This will load a single record, specified by ID. You will need to know the ID of the record
 that you want. Go to [http://localhost:8080/get?id=X](http://localhost:8080/get?id=X),
 where X must be the ID of the record as defined in the SQL database. This will load the single
 record that matches that ID as JSON.
 
 ### Requirement 3 - Fetch warmest or coldest city
 **Warmest:** [http://localhost:8080/get?extremeRecord=warmest](http://localhost:8080/get?extremeRecord=warmest)
 
 **Coldest:** [http://localhost:8080/get?extremeRecord=coldest](http://localhost:8080/get?extremeRecord=coldest)
 
 ### Requirement 4 - Add a new record
 Go to [http://localhost:8080](http://localhost:8080) and fill in the form under "Let's test adding".
 
 You will have to fill out all of the fields before pressing "Submit" or the server will return
 a 422 Error due to unprocessable input. Looking at [http://localhost:8080/get](http://localhost:8080/get)
 will show you that there is a new record.
 
 ### Requirement 5 - Modify a record
 
 Under "Let's test updating" in the form, fill out whatever fields you want to update, and specify an ID.
 An ID *must* be provided.
 
  ### Requirement 6 - Delete a record
  
  Under "Let's test deleting" in the form, fill specify an ID to delete.