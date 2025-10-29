# Android Board Game WebSocket Server

This repository contains the server-side application for a digital Monopoly-like game. It is built with Spring Boot and uses WebSockets for real-time communication with the game client.

This backend handles game logic, player state, and communication between all connected clients.

## ⚠️ Important: Client-Server Architecture

This server is **only** the backend component of the Monopoly game. It cannot be used on its own.

It is designed to work exclusively with the corresponding client-side application, which is managed in a separate repository. Both components are required for the game to function.

  * **Client Repository:** [https://github.com/hoffmann2109/Monopoly-Client.git](https://github.com/hoffmann2109/Monopoly-Client.git)

-----

## Technology Stack

  * **Java:** 17
  * **Framework:** Spring Boot 3.2.3
  * **Communication:** Spring Boot Starter WebSocket
  * **Database/Services:** Firebase Admin 9.2.0
  * **Build Tool:** Apache Maven
  * **Testing:** JUnit 5, Mockito

-----

## Building and Running Locally

### Prerequisites

  * Java 17 (or newer)
  * Apache Maven

### Build

1.  **Clone the repository:**

    ```bash
    git clone [URL_to_this_server_repo]
    cd Monopoly-Server-....
    ```

2.  **Build the project using the Maven wrapper:**

      * On macOS/Linux:
        ```bash
        ./mvnw clean package
        ```
      * On Windows:
        ```bash
        ./mvnw.cmd clean package
        ```

    This will create a `.jar` file (e.g., `WebSocketDemo-Server-0.0.1-SNAPSHOT.jar`) in the `target/` directory.

### Run

1.  **Execute the built `.jar` file:**
    ```bash
    java -jar target/WebSocketDemo-Server-0.0.1-SNAPSHOT.jar
    ```

The server will start and be ready to accept WebSocket connections from the client.
