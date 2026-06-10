# [VIBE CODING] 3Dolphins Priority Broadcast Engine 

## Overview

The **Priority Broadcast Engine** is a prototype of an Omnichannel Messaging Engine module. It is designed to manage high-volume asynchronous message dispatching to multiple customers without blocking the user interface. This project simulates the real-world constraints of third-party API integrations (e.g., WhatsApp or Facebook), handling slow responses and ensuring the frontend remains fully responsive.

## Tech Stack

This project is built using a robust, enterprise-grade Java EE stack:

- **Language:** Java 17
- **Frontend Framework:** Java Server Faces (JSF) 2.2 (Mojarra) & PrimeFaces 10.0
- **Dependency Injection / Backend Context:** CDI (Weld 3.1.9.Final)
- **Web Server:** Apache Tomcat 7 (via `tomcat7-maven-plugin`)
- **Build Tool:** Apache Maven

## Architecture & Concepts

1. **Asynchronous Processing:**
   The core broadcast logic runs on a background thread pool using `ExecutorService`. This approach guarantees that the main UI thread (handled by Tomcat) is never blocked during simulated external API delays (`Thread.sleep`).

2. **State Management:**
   The UI state (selected customers, progress, status) is preserved using the `@ViewScoped` annotation. This ensures that the state survives multiple AJAX requests within the same view while preventing the memory leaks commonly associated with `@SessionScoped`.

3. **Non-blocking UI Feedback:**
   The frontend utilizes PrimeFaces' `p:poll` to asynchronously fetch real-time updates from the background task. This allows the user to see live status changes and progress bar increments without triggering a full-page refresh.

## Prerequisites

Before running the project, ensure you have the following installed on your local environment:
- **Java Development Kit (JDK) 17**
- **Apache Maven (3.6+)**

## How to Run

### Option 1: Quick Run (Windows - Automator Script)
If you do not have Maven installed on your machine, you can use the provided automated script. It will download a local copy of Apache Maven, set up the required JVM environment variables (`MAVEN_OPTS`), and run the project automatically.

1. Open **PowerShell** in the project directory.
2. Run the following command:
   ```powershell
   ./run_project.ps1
   ```
3. Once the terminal displays `INFO: Starting ProtocolHandler ["http-bio-8080"]`, open your browser at:
   [http://localhost:8080/](http://localhost:8080/)

### Option 2: Manual Run (Standard Maven)
If you already have Maven globally installed, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/marcceljanara/3Dolphins-Technical-Challenge.git
   cd 3Dolphins-Technical-Challenge
   ```

2. **Set the environment variable (`MAVEN_OPTS`):**
   Because this project runs on Java 17 and uses Tomcat 7, you need to open internal JDK classes:
   * **For Linux/macOS (Bash/Zsh):**
     ```bash
     export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
     ```
   * **For Windows (PowerShell):**
     ```powershell
     $env:MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
     ```
   * **For Windows (Command Prompt):**
     ```cmd
     set MAVEN_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED
     ```

3. **Run the Tomcat server:**
   ```bash
   mvn clean tomcat7:run
   ```

4. **Access the application:**
   Navigate to:
   [http://localhost:8080/](http://localhost:8080/)

## Project Structure

- `src/main/java/com/mycompany/broadcast/controller/BroadcastController.java`: The JSF Managed Bean bridging the UI and background execution.
- `src/main/java/com/mycompany/broadcast/service/IntegrationService.java`: The service layer simulating external third-party API calls.
- `src/main/java/com/mycompany/broadcast/model/Customer.java`: The core domain model representing the recipient data.
- `src/main/webapp/broadcast.xhtml`: The main frontend interface powered by PrimeFaces.

## Technical Challenge Answers

For insights regarding advanced architectural decisions, such as rate limiting strategies, circuit breaker implementation, and scaling the system to handle 1 million records, please refer to the detailed write-up in [CHALLENGE_ANSWERS.md](CHALLENGE_ANSWERS.md).
