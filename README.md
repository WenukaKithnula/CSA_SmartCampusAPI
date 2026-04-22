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

**1. Build the project**
```bash
mvn clean install
```

**2. Deploy to Tomcat**

Copy `target/smartcampus-1.0-SNAPSHOT.war` to your Tomcat `webapps/` directory and start Tomcat.

**3. Base URL**

http://localhost:8080/smartcampus/api/v1/

---

## Sample cURL Commands

### 1. Discovery
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1/
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-101","name":"Main Library","capacity":50}'
```

### 3. Register a Sensor
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-01","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-101"}'
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/smartcampus/api/v1/sensors?type=Temperature"
```

### 5. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors/TEMP-01/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.5}'
```

### 6. Get Reading History
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1/sensors/TEMP-01/readings
```

### 7. Delete a Room with Sensors Assigned
```bash
curl -X DELETE http://localhost:8080/smartcampus/api/v1/rooms/LIB-101
```
Expected: `409 Conflict`

### 8. Post Reading to Sensor in Maintenance
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors/02/readings \
  -H "Content-Type: application/json" \
  -d '{"value":25.0}'
```
Expected: `403 Forbidden`

---

## Data Persistence Note

This API uses **in-memory storage** via `ConcurrentHashMap` as required. All data is lost when the server restarts. No database is used.

---

## Report: Question Responses

### Question 1.1 — JAX-RS Resource Lifecycle

> Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this impacts in-memory data management.

JAX-RS resource classes follow a **request-scoped lifecycle** by default. A new instance is created for every incoming HTTP request and discarded after the response is sent. This means instance variables cannot store shared application data — any data stored there would be destroyed at the end of each request.

To manage shared state, this API uses a **Singleton `DataStore` class** with `ConcurrentHashMap` collections. The Singleton pattern ensures one shared instance persists across all requests regardless of how many resource instances are created and destroyed:

```java
public static synchronized DataStore getInstance() {
    if (instance == null) {
        instance = new DataStore();
    }
    return instance;
}
```

`ConcurrentHashMap` provides thread-safe operations for concurrent access from multiple request threads, preventing race conditions and data corruption. Apache Tomcat processes multiple requests simultaneously on separate threads, so a standard `HashMap` would be unsafe — concurrent modifications can corrupt its internal state causing `ConcurrentModificationException` or lost updates. Individual reading lists are also wrapped with `Collections.synchronizedList()` to protect concurrent modifications to list contents.

---

### Question 1.2 — HATEOAS and Hypermedia Design

> Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

HATEOAS (Hypermedia as the Engine of Application State) means API responses include hyperlinks to related resources. In this API, `GET /api/v1/` returns dynamic links to the rooms and sensors collections, and each room in the list response includes its own `href` pointing to its detail endpoint.

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

Unlike static documentation that becomes outdated as the API evolves, HATEOAS links are generated dynamically at request time using `UriInfo` and are always accurate. Clients only need to know one entry point and can discover everything else by following links embedded in responses, making them resilient to URL changes. If a URL structure changes, clients that navigate by following named links adapt without code changes because they are not depending on hardcoded path strings.

---

### Question 2.1 — Room List Return Format

> When returning a list of rooms, what are the implications of returning only IDs versus returning full room objects? Consider network bandwidth and client-side processing.

Returning only IDs forces clients to make N+1 additional requests to get room details, increasing server load and latency. For a campus with thousands of rooms, one list request would spawn thousands of individual detail requests, which is especially problematic over mobile networks.

Returning full room objects avoids the extra requests but transfers all fields — including the complete `sensorIds` list — for every room in every response. Most clients, such as a dashboard showing a room selection menu, only need the name and ID. Forcing all clients to download and parse complete objects wastes bandwidth and increases memory consumption on lower-powered devices.

This API returns **summary objects** with only `id`, `name`, and a HATEOAS `href` link:

```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "href": "http://localhost:8080/smartcampus/api/v1/rooms/LIB-301"
  }
]
```

Clients get enough data for common use cases and can follow the `href` link for full details when needed, saving bandwidth while maintaining HATEOAS compliance.

---

### Question 2.2 — DELETE Idempotency

> Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request multiple times.

The DELETE operation is **idempotent** in terms of server-side state. The first request removes the room and returns `204 No Content`. Subsequent identical requests find the room no longer exists — the service throws `NotFoundException` which the `GlobalExceptionMapper` returns as `404 Not Found`.

The server state after the first request — the room does not exist — is identical after any subsequent request. This satisfies idempotency even though the response code changes from `204` to `404`, because idempotency concerns the state outcome, not the response code.

This behaviour makes retries safe, which is important for unreliable networks where clients may retry a request after a timeout without knowing whether the original reached the server. The worst outcome is a `404` confirming the room was already gone — not a dangerous or confusing side effect.

---

### Question 3.1 — @Consumes and Content-Type Enforcement

> Explain the technical consequences if a client sends data in a different format than application/json. How does JAX-RS handle this mismatch?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation enforces that only requests with `Content-Type: application/json` are accepted. This contract is enforced during request routing before the resource method is ever invoked.

If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, the JAX-RS runtime compares the incoming content type against the `@Consumes` declaration and finds no match. The framework automatically returns **`415 Unsupported Media Type`** without executing the resource method or any business logic.

The underlying mechanism is the `MessageBodyReader` system. JAX-RS uses registered readers to deserialize request bodies into Java objects. The Jackson library provides a reader for `application/json`. If the content type is `text/plain` or `application/xml`, no suitable reader exists for that combination and the request is rejected immediately at the framework layer.

This protects the API from malformed or unexpected input formats and prevents content-type confusion attacks where a malicious client might send XML containing XXE injection sequences to an endpoint expecting JSON.

---

### Question 3.2 — @QueryParam vs Path Parameter for Filtering

> Contrast @QueryParam filtering with an alternative design where type is part of the URL path. Why is the query parameter approach superior?

Path parameters identify **specific resources** — for example `/sensors/TEMP-01` identifies one sensor. Query parameters **filter collections** — for example `/sensors?type=Temperature` narrows the collection by an attribute. Conflating these two concepts leads to URL designs that misrepresent the domain model.

Using `/sensors/type/CO2` as a path incorrectly implies that `type/CO2` is a hierarchical sub-resource that exists as a named entity below `sensors`. It also requires a separate URL pattern to retrieve all sensors with no filter, creating redundant routes and duplicate handler code.

Query parameters are naturally optional, support multiple filters composing cleanly (`?type=CO2&status=ACTIVE`), and keep the endpoint URL consistent. Both `GET /api/v1/sensors` and `GET /api/v1/sensors?type=Temperature` route to the same method:

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response getAllSensors(@QueryParam("type") String type) {
    List<Sensor> sensors = sensorService.getSensors(type);
    ...
}
```

This follows REST conventions — query parameters are specifically designed for filtering, searching, and paginating collections — and produces cleaner, more maintainable code.

---

### Question 4.1 — Sub-Resource Locator Pattern

> Discuss the architectural benefits of the Sub-Resource Locator pattern. How does it help manage complexity in large APIs?

The Sub-Resource Locator pattern allows a resource class to delegate handling of a nested path to a separate, dedicated class. The locator method carries only a `@Path` annotation — no HTTP verb — so the JAX-RS runtime delegates further request matching to the returned object rather than treating the method itself as a direct handler.

In this API, `SensorResource` delegates all reading operations to `SensorReadingResource`:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

Without this pattern, all reading endpoints would be defined inside `SensorResource`. As the API grows — adding pagination, date-range filtering, or reading aggregation — `SensorResource` would become a massive class mixing sensor-level and reading-level concerns. Finding, modifying, or testing specific logic becomes error-prone and slow.

Separating the classes gives each one a single, clear responsibility. The `sensorId` is passed via the constructor, giving all methods in `SensorReadingResource` automatic access to their context without re-extracting path parameters in every method. In a team environment, two developers can work on these classes simultaneously without merge conflicts. In testing, `SensorReadingResource` can be instantiated and unit tested in complete isolation. These benefits scale significantly as the API grows deeper in resource nesting.

---

### Question 5.1 — HTTP 422 vs 404 for Missing References

> Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

`404 Not Found` means the resource identified by the **requested URL** does not exist. When posting a sensor to `POST /api/v1/sensors`, the URL is valid and the endpoint exists. Returning `404` would mislead the client into thinking it sent the request to the wrong URL, which is not the problem.

The real issue is inside the request body. The JSON is syntactically valid, all required fields are present, and the content type is correct. The problem is that the `roomId` value references a room that does not exist — a semantic violation of referential integrity, not a routing or format failure.

`422 Unprocessable Entity` (RFC 4918) signals that the server understood the request, received it at the correct URL, parsed the JSON successfully, but cannot process it due to a business rule violation. This accurately describes the scenario.

The distinction has direct practical importance for client developers. A `404` tells the client to check its URL. A `422` tells the client to check the data inside its request body — specifically the `roomId` field. This gives actionable, unambiguous information for debugging and allows client applications to handle data validation errors distinctly from routing errors.

---

### Question 5.2 — Cybersecurity Risks of Stack Trace Exposure

> From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing stack traces is a serious information disclosure vulnerability. A raw trace reveals:

- **Internal package and class structure** — for example `com.mycompany.smartcampus.resources.SensorRoomResource:87`. This maps the application's architecture and exact line numbers, helping attackers reason about internal control flow without source code access.
- **Technology stack and library versions** — frames from Jersey, Tomcat, and Jackson can be cross-referenced against CVE databases to find known vulnerabilities for those exact versions. If the trace reveals an outdated library with a published exploit, the attacker can attempt it immediately.
- **Business logic weaknesses** — exception messages such as `NullPointerException: cannot invoke String.length() on null` tell an attacker that sending null values for certain fields may crash the application, guiding systematic fuzzing and facilitating denial of service attacks.

The `GlobalExceptionMapper` eliminates all of this by catching every `Throwable` and returning only a safe, generic message:

```java
ErrorResponse error = new ErrorResponse("An unexpected server error occurred.", 500);
```

Full exception details remain in server-side logs via `ex.printStackTrace()` for developers, while attackers receive no exploitable intelligence from the HTTP response.

---

### Question 5.3 — JAX-RS Filters for Cross-Cutting Concerns

> Why is it advantageous to use JAX-RS filters for logging rather than manually inserting Logger.info() statements inside every single resource method?

Logging is a cross-cutting concern — it applies across the entire application but is not the responsibility of any individual component. Manual logging violates the DRY (Don't Repeat Yourself) principle, creates a large maintenance surface, and risks inconsistent coverage. When new endpoints are added, developers may forget to include log statements, creating silent gaps. If an exception is thrown before the response log line is reached, that line never executes, so error responses frequently go unlogged.

The `ApiLoggingFilter` solves all of these problems in a single class registered once with `@Provider`:

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

It intercepts every request and response automatically, including requests that result in exceptions, because the response filter runs after the exception mapper has converted the exception to a proper HTTP response. Every interaction is guaranteed to be logged with zero gaps and no manual effort. Future enhancements such as adding correlation IDs require changing only this one file. Resource methods remain clean and focused entirely on business logic, following the Single Responsibility Principle.

---

## Video Demonstration

A video demonstration has been submitted via Blackboard, covering all API endpoints, error scenarios (409, 422, 403, 500), and server logs showing the `ApiLoggingFilter` in action for every request and response.

---

## Contact

**Author:** Wenuka  
**Email:** wenuka.20242072@iit.ac.lk  
**Student ID:** w2152924 / 20242072
