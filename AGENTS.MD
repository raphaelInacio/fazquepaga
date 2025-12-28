# Agents Guide

## Project Overview

This is a full-stack application called "TaskAndPay" designed to help parents and children manage chores and allowances.

The project is structured as a monorepo with a `frontend` and a `backend` directory.

### Frontend

The frontend is a React application built with Vite, TypeScript, and Tailwind CSS. It uses the shadcn-ui component library and Playwright for end-to-end testing. The application is served by the backend.

### Backend

The backend is a Java application built with Spring Boot and Maven. It uses several Google Cloud services, including Firestore as a NoSQL database, Pub/Sub for messaging, and Vertex AI (Gemini) for AI-powered features like task suggestions and proof validation. It also integrates with Twilio for WhatsApp notifications.

**Task Scheduling**: The system ignores local cron jobs for task resets. Instead, it listens for a Google Cloud Pub/Sub message `{"action": "RESET_TASKS"}` to trigger the daily task reset process.

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
│   │   ├── notification/ # Notification Hub (Pub/Sub events)
│   │   ├── payment/      # Asaas integration (subscriptions)
│   │   ├── security/     # Spring Security setup
│   │   ├── shared/       # Shared utilities & exceptions
│   │   ├── subscription/ # Plan limits & logic
│   │   ├── tasks/        # Task management domain
│   │   └── whatsapp/     # Twilio/WhatsApp integration
│   ├── pom.xml
│   └── docker-compose.yml
└── frontend/     # React web application
    ├── src/
    │   ├── components/   # Reusable UI components
    │   ├── context/      # AuthContext
    │   ├── contexts/     # SubscriptionContext
    │   ├── hooks/        # Custom React hooks
    │   ├── lib/          # Utilities (API client, utils)
    │   ├── locales/      # i18n translations (pt/en)
    │   ├── pages/        # Application pages/routes
    │   ├── services/     # API service layers
    │   └── types/        # TypeScript definitions
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
- JaCoCo report must show **minimum 60% overall coverage**.
- CI fails if coverage drops below the threshold.

### Frontend UI Tests
- End‑to‑end tests written with **Playwright**.
- Run with `npm run test:e2e`.
- CI ensures all Playwright tests pass before merging.

## Rules & Productivity

To ensure high quality and consistency, ALL agents must follow the established project rules.

### Core Rule Sets

The following rule files define our standards. **Always consult these before generating code.**

-   **[Use Java Spring Boot](.agent/rules/use-java-spring-boot.md)**: Backend technology choices and rationale.
-   **[React Frontend](.agent/rules/react.md)**: Frontend integration and architecture.
-   **[Code Standards](.agent/rules/code-standards.md)**: Naming conventions, clean code principles, and formatting (Java & TS).
-   **[API REST HTTP](.agent/rules/api-rest-http.md)**: API design contracts and JSON standards.
-   **[Tests](.agent/rules/tests.md)**: Testing strategies and library usage.
-   **[Folder Structure](.agent/rules/folder-structure.md)**: Where files should live.
-   **[Firestore NoSQL](.agent/rules/firestore-nosql.md)**: NoSQL data modeling and best practices.
-   **[AI Integration](.agent/rules/ai-integration.md)**: Guidelines for using Gemini and Spring AI.
-   **[E2E Testing](.agent/rules/e2e-testing.md)**: Frontend validation via MCP and Playwright test updates.
-   **[Internationalization](.agent/rules/internationalization.md)**: Handling translations (en/pt).
-   **[Asaas Integration](.agent/rules/asaas-integration.md)**: Payment gateway integration patterns.
-   **[Logging](.agent/rules/logging.md)**: Structured logging and observability.
-   **[Frontend Testing](.agent/rules/frontend-testing.md)**: Unit and component testing strategies.

### How to Apply Rules

1.  **Context Loading**: When starting a task, load the relevant `.md` file to understanding specific constraints (e.g., `react.md` for frontend work).
2.  **Code Validations**: Before submitting changes, cross-reference with `code-standards.md`.
3.  **Architecture Checks**: Ensure new modules align with `folder-structure.md`.



