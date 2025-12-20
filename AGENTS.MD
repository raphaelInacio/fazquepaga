# Agents Guide

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


## 📂 Project Structure

The project is a monorepo with two main parts:

```
.
├── backend/      # Spring Boot modular monolith
│   ├── src/main/java/com/fazquepaga/taskandpay
│   │   ├── ai/           # AI integration (Gemini)
│   │   ├── allowance/    # Allowance calculation & Ledger
│   │   ├── config/       # App configuration (Security, CORS)
│   │   ├── controller/   # REST Controllers
│   │   ├── giftcard/     # Gift Card Store logic
│   │   ├── identity/     # User management (Parent/Child)
│   │   ├── security/     # Spring Security setup
│   │   ├── shared/       # Shared utilities & exceptions
│   │   ├── subscription/ # Plan limits & logic
│   │   ├── tasks/        # Task management domain
│   │   └── whatsapp/     # Twilio/WhatsApp integration
│   ├── pom.xml
│   └── docker-compose.yml
└── frontend/     # React web application
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   └── services/
    └── package.json
```

## Project Rules

### GitHub Pipeline
- CI workflow triggers on push to `main` and pull requests.
- Jobs include **build**, **test**, **coverage**, and **deploy**.
- Status checks required: build success, tests pass, coverage >= 80%.

### Backend Code Formatting
- Enforced with **Spotless** using **Google Java Format (AOSP style)**.
- Run `./mvnw spotless:apply` to format code.
- Formatting is verified in CI.

### Coverage Requirements
- JaCoCo report must show **minimum 80% overall coverage**.
- CI fails if coverage drops below the threshold.

### Frontend UI Tests
- End‑to‑end tests written with **Playwright**.
- Run with `npm run test:e2e`.
- CI ensures all Playwright tests pass before merging.

## Rules & Productivity

To ensure high quality and consistency, ALL agents must follow the established project rules.

### Core Rule Sets

The following rule files define our standards. **Always consult these before generating code.**

-   **[Use Java Spring Boot](docs/ai_guidance/rules/use-java-spring-boot.mdc)**: Backend technology choices and rationale.
-   **[React Frontend](docs/ai_guidance/rules/react.mdc)**: Frontend integration and architecture.
-   **[Code Standards](docs/ai_guidance/rules/code-standards.mdc)**: Naming conventions, clean code principles, and formatting (Java & TS).
-   **[API REST HTTP](docs/ai_guidance/rules/api-rest-http.mdc)**: API design contracts and JSON standards.
-   **[Tests](docs/ai_guidance/rules/tests.mdc)**: Testing strategies and library usage.
-   **[Folder Structure](docs/ai_guidance/rules/folder-structure.mdc)**: Where files should live.
-   **[Firestore NoSQL](docs/ai_guidance/rules/firestore-nosql.mdc)**: NoSQL data modeling and best practices (replaces SQL).
-   **[AI Integration](docs/ai_guidance/rules/ai-integration.mdc)**: Guidelines for using Gemini and Spring AI.
-   **[E2E Testing](docs/ai_guidance/rules/e2e-testing.mdc)**: Frontend validation via MCP and Playwright test updates.
-   **[Internationalization](docs/ai_guidance/rules/internationalization.mdc)**: Handling translations (en/pt).

### How to Apply Rules

1.  **Context Loading**: When starting a task, load the relevant `.mdc` file to understanding specific constraints (e.g., `react.mdc` for frontend work).
2.  **Code Validations**: Before submitting changes, cross-reference with `code-standards.mdc`.
3.  **Architecture Checks**: Ensure new modules align with `folder-structure.mdc`.



