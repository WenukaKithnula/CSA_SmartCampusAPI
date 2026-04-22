# Smart Campus API

**Author:** Wenuka  
**Student ID:** w2152924 / 20242072  
**Module:** 5COSC022W Client-Server Architectures  
**Academic Year:** 2025/26

---

## Project Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey) to manage campus rooms and sensors. It provides endpoints for facilities managers and automated building systems to track environmental conditions, occupancy, and equipment status across campus.

### Key Features

- **Room Management:** Create, retrieve, and delete campus rooms with capacity tracking
- **Sensor Management:** Register and monitor sensor types (Temperature, CO2, Light, etc.)
- **Historical Readings:** Time-series reading storage with automatic parent sensor updates
- **Query Filtering:** Filter sensors by type using query parameters
- **Business Logic Validation:** Blocks room deletion when sensors are assigned; blocks readings to sensors under maintenance
- **Error Handling:** Custom exception mappers for 409, 422, 403, 404, and 500 responses
- **Request/Response Logging:** Cross-cutting filter for full API observability

### Technology Stack

- **JAX-RS Implementation:** Jersey 2.32
- **JSON Processing:** Jackson
- **Servlet Container:** Apache Tomcat
- **Build Tool:** Maven 3.x
- **Java Version:** Java 8

---

## Build and Deployment

### Prerequisites

- JDK 8 or higher
- Apache Maven 3.6+
- Apache Tomcat 9+

### Steps

## How to run it

You need JDK 8+, Maven 3.6+, and Tomcat 9+.

**Step 1 - Build**
```bash
mvn clean install
```

**Step 2 - Deploy**

Copy the WAR file to your Tomcat webapps folder then start Tomcat.
```bash
cp target/smartcampus-1.0-SNAPSHOT.war /path/to/tomcat/webapps/
./bin/startup.sh
```

Or just use Run/Deploy in NetBeans directly.

**Step 3 - Check it works**
```bash
curl http://localhost:8080/smartcampus/api/v1/
```

Base URL is `http://localhost:8080/smartcampus/api/v1/`

## Sample cURL Commands

**1. Discovery endpoint**
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1/
```

**2. Get all rooms**
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1/rooms
```

**3. Create a room**
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CS-101","name":"Computer Science Lab","capacity":40}'
```
Returns 201 Created with a Location header.

**4. Get a specific room**
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1/rooms/LIB-301
```

**5. Delete a room that has no sensors**
```bash
curl -X DELETE http://localhost:8080/smartcampus/api/v1/rooms/WBS-G01
```
Returns 204 No Content.

**6. Try to delete a room that still has sensors**
```bash
curl -X DELETE http://localhost:8080/smartcampus/api/v1/rooms/LIB-301
```
Returns 409 Conflict with a JSON error body.

**7. Register a sensor with a valid roomId**
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```
Returns 201 Created.

**8. Register a sensor with a roomId that doesnt exist**
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'
```
Returns 422 Unprocessable Entity.

**9. Filter sensors by type**
```bash
curl -X GET "http://localhost:8080/smartcampus/api/v1/sensors?type=TEMP"
```
Filtering is case-insensitive.

**10. Post a reading to a sensor**
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors/01/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.7}'
```
Returns 201 Created and also updates the parent sensors currentValue to 24.7.

**11. Get reading history for a sensor**
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1/sensors/01/readings
```

**12. Post a reading to a sensor that is under maintenance**
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors/02/readings \
  -H "Content-Type: application/json" \
  -d '{"value":25.0}'
```
Returns 403 Forbidden because sensor 02 is seeded with MAINTENANCE status.

## Pre-loaded test data

The DataStore comes with this data already loaded so you can test right away:

| Type   | ID       | Info                                          |
|--------|----------|-----------------------------------------------|
| Room   | LIB-301  | Library Quiet Study, capacity 50              |
| Room   | WBS-G01  | Lecture Theatre, capacity 200                 |
| Sensor | 01       | Type: TEMP, Status: ACTIVE, Room: LIB-301     |
| Sensor | 02       | Type: Light, Status: MAINTENANCE, Room: LIB-301 |

Sensor 02 is set to MAINTENANCE so you can demo the 403 straight away without creating anything.

## Storage

Everything is stored in memory using ConcurrentHashMap. Data resets when the server restarts. No database is used, which is what the coursework requires.

## Report Questions

### Question 1.1 

> In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

By default JAX-RS creates a brand new instance of each resource class for every single HTTP request that comes in. So something like SensorRoomResource or SensorResource gets created, handles the request, and then gets thrown away. This is called request-scoped. It is the opposite of a singleton where one instance would stick around and handle everything.

Because of this, you cannot store any data in instance variables inside your resource classes because they get destroyed after every request. So if you stored rooms in an instance variable it would be gone by the time the next request comes in.

To get around this the API uses a Singleton DataStore class. The Singleton pattern means only one instance of DataStore ever gets created and everything shares it. It stays alive for the whole time the server is running regardless of how many resource instances come and go:

```java
public static synchronized DataStore getInstance() {
    if (instance == null) {
        instance = new DataStore();
    }
    return instance;
}
```

But then you have another problem. Tomcat handles multiple requests at the same time on different threads so multiple threads are reading and writing to these shared maps simultaneously. A normal HashMap is not thread safe so concurrent writes can corrupt the internal structure and cause ConcurrentModificationException or lost data.

So the API uses ConcurrentHashMap for all three stores:

```java
private final Map<String, Room> roomStore = new ConcurrentHashMap<>();
private final Map<String, Sensor> sensorStore = new ConcurrentHashMap<>();
private final Map<String, List<SensorReading>> sensorReadingStore = new ConcurrentHashMap<>();
```

ConcurrentHashMap handles concurrent reads and writes safely without corrupting the data. The reading lists are also wrapped with Collections.synchronizedList() so concurrent additions to individual lists are safe too.

### Question 1.2

> Why is the provision of Hypermedia considered a hallmark of advanced RESTful design? How does this approach benefit client developers compared to static documentation?

HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that instead of clients having to know all the URLs upfront, the API responses include links that tell the client where to go next.

In this API the discovery endpoint at GET /api/v1/ returns links to the rooms and sensors collections. Each room in the list also includes an href pointing to its own detail endpoint. These links are built dynamically at request time using UriInfo so they always reflect the real host and port the server is running on:

```json
{
  "version": "v1.0.0",
  "description": "Smart Campus API is Live",
  "adminContact": "wenuka.20242072@iit.ac.lk",
  "links": {
    "rooms": "http://localhost:8080/smartcampus/api/v1/rooms",
    "sensors": "http://localhost:8080/smartcampus/api/v1/sensors"
  }
}
```

The benefit over static documentation is that the links are always correct because the server generates them live. Static docs get written once and then drift out of sync as the API changes. URLs get renamed, endpoints get added, and the docs dont always get updated. Clients built on stale documentation break.

With HATEOAS the client only needs to know one starting point which is /api/v1/ and it can discover everything from there by following the links. If a URL changes the client doesnt need a code change because it is following the link name not a hardcoded string.

### Question 2.1 

> When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

If you only return IDs in the list, the client has to make a separate request for every single room to get any useful information like the name or capacity. If there are hundreds of rooms that is hundreds of extra HTTP requests just to show a simple list. This is called the N+1 problem and it puts unnecessary load on the server and makes things slow, especially on mobile networks.

If you return the full room objects you avoid those extra requests but you end up sending a lot of data the client probably doesnt need. For example the sensorIds list gets sent for every room even if the client only wants to show a dropdown of room names.

This API returns summary objects with just the id, name, and an href link:

```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "href": "http://localhost:8080/smartcampus/api/v1/rooms/LIB-301"
  }
]
```

This way the client gets what it needs for most use cases without extra requests, and if it needs the full details it can follow the href. The href also means the client doesnt have to construct the URL itself which ties into the HATEOAS approach.

### Question 2.2 

> Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

The DELETE is idempotent in terms of the server state even though the response code changes between calls.

The first time you call DELETE /api/v1/rooms/WBS-G01 on a room with no sensors, the service finds it, removes it, and returns 204 No Content. The second time you call it, the room is already gone so the service throws a NotFoundException which the GlobalExceptionMapper converts to a 404 Not Found.

The server state is the same after the first call and every call after it, the room does not exist. That is what idempotency actually means, the state outcome is the same not that the response code has to be identical.

This matters in practice because networks are unreliable and clients sometimes retry requests if they dont get a response in time. With this implementation retrying a DELETE is completely safe. The worst that happens is the client gets a 404 back which tells them the room is already gone, which is true. There are no dangerous side effects from retrying.

### Question 3.1 

> We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The @Consumes(MediaType.APPLICATION_JSON) annotation tells the JAX-RS runtime that this method only accepts requests where the Content-Type header is application/json. This is checked before the method even runs.

If a client sends Content-Type: text/plain or Content-Type: application/xml, JAX-RS looks at all the @Consumes declarations for that endpoint and finds no match. It automatically returns 415 Unsupported Media Type and the registerSensor method never executes at all.

The way this works under the hood is through the MessageBodyReader system. JAX-RS has registered readers that know how to convert a request body into a Java object. Jackson registers a reader for application/json that can turn the JSON into a Sensor object. If the content type is something else there is no reader for that combination and the request gets rejected straight away at the framework level before reaching any of your code.

This is useful because clients get a clear error code telling them exactly what went wrong. It also protects against things like XXE injection attacks where someone might try to send malicious XML to an endpoint that is expecting JSON.

### Question 3.2 

> You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Path parameters are for identifying a specific resource. Something like /sensors/TEMP-01 points to one specific sensor. Query parameters are for filtering or shaping a collection. Something like /sensors?type=Temperature narrows down the results.

If you put the filter in the path like /sensors/type/CO2 you are implying that type/CO2 is an actual resource that lives under sensors in a hierarchy, which it is not. Type is just an attribute you want to filter by. You would also need a completely separate URL to get all sensors with no filter which means duplicate route handling.

With query parameters both GET /api/v1/sensors and GET /api/v1/sensors?type=TEMP go to the same method:

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response getAllSensors(@QueryParam("type") String type) {
    List<Sensor> sensors = sensorService.getSensors(type);
    ...
}
```

The type parameter is optional by nature. When it is not there you get everything. When it is there you get filtered results. Adding more filters is also easy, you just add more query parameters like ?type=CO2&status=ACTIVE without changing the URL structure or adding new methods. With path parameters you would need a new route for every filter combination which gets messy fast.

### Question 4.1 

> Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

The sub-resource locator pattern is when a resource class delegates a nested path to a completely separate class. The key thing is the locator method only has @Path on it, no @GET or @POST, which tells JAX-RS to pass the request on to the object that gets returned rather than handle it directly.

In this API SensorResource hands off everything under /readings to SensorReadingResource:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

Without this pattern all the reading endpoints would have to live inside SensorResource. As the API grows and you add things like pagination or filtering by date range, SensorResource would turn into a massive class that mixes sensor logic with reading logic. It becomes hard to find things, easy to break things accidentally, and difficult to test.

By separating them each class has one job. SensorResource handles sensor registration and retrieval. SensorReadingResource handles everything to do with readings. The sensorId gets passed into SensorReadingResource through the constructor so every method in that class already has access to it without having to extract it from the path again.

The sensor existence check also only needs to be written once in the locator and it covers all reading operations automatically. Two developers can also work on these two classes at the same time without stepping on each other. And SensorReadingResource can be tested on its own by just creating an instance with a known sensorId.

### Question 5.1 

> Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

404 Not Found means the URL you requested does not exist. When a client posts to /api/v1/sensors that URL does exist and works fine. Returning 404 there would make the client think it got the URL wrong which is not what happened.

The actual problem is inside the request body. The JSON is valid, the content type is correct, all the fields are there. The issue is just that the roomId value points to a room that does not exist in the system. That is a data problem not a URL problem.

422 Unprocessable Entity is the right status for this situation. It tells the client that the server received the request fine, understood it, parsed the JSON correctly, but cannot process it because the data breaks a business rule. In this case referential integrity because the room being referenced does not exist.

From a practical point of view this matters because a 404 tells the client to check its URL and a 422 tells the client to check its data. That distinction saves time when debugging. It also lets client applications handle these two types of errors differently in their code, routing errors versus validation errors.

### Question 5.2 

> From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Without the GlobalExceptionMapper any unhandled exception would show the full Java stack trace in the response. This is a real security problem because it hands attackers a map of your application without them needing access to the source code.

A stack trace shows the internal package and class structure, something like com.mycompany.smartcampus.resources.SensorRoomResource:87. That tells an attacker exactly how the project is structured, what classes exist, and which line of code failed. With that information they can start reasoning about the internal logic and figure out where to probe further.

Stack traces also show what libraries are being used and which versions. If there is a Jersey or Jackson frame in the trace an attacker can look up that exact version in CVE databases and find known vulnerabilities to exploit.

Exception messages also leak business logic. Something like NullPointerException: cannot invoke String.length() on null tells an attacker that sending a null value for a certain field crashes the application. That gives them a starting point for fuzzing to find more crashes and potentially cause denial of service.

The GlobalExceptionMapper stops all of this by catching every Throwable and only returning a generic message:

```java
ErrorResponse error = new ErrorResponse("An unexpected server error occurred.", 500);
```

The full exception still gets printed to the server logs with ex.printStackTrace() so developers can debug it, but the client never sees anything useful. This separates what the server knows from what the outside world gets to see.

### Question 5.3 

> Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging rather than manually inserting Logger.info() statements inside every single resource method?

If you manually add log statements to every resource method you end up with the same code copy-pasted across every class. When you add a new endpoint you have to remember to add logging to it. When an exception gets thrown before the response log line runs that line never executes so you get gaps in your logs for error cases. If you want to change the log format you have to update every single method.

It also makes your resource methods harder to read because the actual business logic is surrounded by logging code that has nothing to do with what the method is supposed to do.

The ApiLoggingFilter handles all of this in one place. Because it is registered with @Provider JAX-RS automatically applies it to every request and response across the whole API:

```java
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        LOGGER.info(">>> Incoming: [" + requestContext.getMethod() + "] "
                + requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        LOGGER.info("<<< Outgoing: Status " + responseContext.getStatus()
                + " for [" + requestContext.getMethod() + "] "
                + requestContext.getUriInfo().getRequestUri());
    }
}
```

The response filter also runs after the exception mapper has already turned any exception into a proper HTTP response so even error responses get logged. There are no gaps. Every request and response is covered automatically with zero duplication.

If you want to add something like a correlation ID or response time in the future you only change this one file. The resource methods stay clean and just focus on their actual job.

## Video Demonstration

A video has been submitted on Blackboard showing all the endpoints working, the error scenarios including 409, 422, 403, and 500, and the server console showing the ApiLoggingFilter logging every request and response.

## Contact

**Author:** Wenuka
**Email:** wenuka.20242072@iit.ac.lk
**Student ID:** w2152924 / 20242072
