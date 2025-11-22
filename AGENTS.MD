# GEMINI.md

## Project Overview

This is a full-stack application called "TaskAndPay" designed to help parents and children manage chores and allowances.

The project is structured as a monorepo with a `frontend` and a `backend` directory.

### Frontend

The frontend is a React application built with Vite, TypeScript, and Tailwind CSS. It uses the shadcn-ui component library and Playwright for end-to-end testing. The application is served by the backend.

### Backend

The backend is a Java application built with Spring Boot and Maven. It uses several Google Cloud services, including Firestore as a NoSQL database, Pub/Sub for messaging, and Vertex AI (Gemini) for AI-powered features like task suggestions and proof validation. It also integrates with Twilio for WhatsApp notifications.

The backend is designed as a modular monolith, with distinct modules for identity, tasks, allowance, AI, and WhatsApp.

## Building and Running

### Frontend

To run the frontend development server:

```sh
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`.

### Backend

The backend is configured to run with local emulators for Google Cloud services.

1.  **Start the emulators:**

    ```sh
    cd backend
    docker-compose up -d
    ```

2.  **Run the application:**

    ```sh
    ./mvnw spring-boot:run
    ```

The backend will be available at `http://localhost:8080`. The Spring Boot application will also serve the frontend.

## Development Conventions

### Backend

*   The backend follows a modular monolithic architecture.
*   It uses Google Java Format (AOSP style) for code formatting.
*   Unit and integration tests are written with JUnit and Testcontainers.
*   Jacoco is used for code coverage analysis.

### Frontend

*   The frontend uses ESLint for linting.
*   End-to-end tests are written with Playwright.
