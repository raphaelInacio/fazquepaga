---
trigger: model_decision
---

# Frontend Integration with Spring Boot

## Architecture

For the frontend, we have two main approaches when working with a Spring Boot backend:

1.  **Decoupled Frontend (Recommended)**: A separate frontend application (e.g., built with React, Angular, or Vue.js) that communicates with the Spring Boot backend via REST APIs.
2.  **Monolithic Frontend**: A traditional server-side rendered application using a template engine like Thymeleaf.

This document focuses on the principles of integrating a **decoupled frontend**.

## Key Principles

### API Contract

- The frontend and backend teams must agree on a clear API contract (see `api-rest-http.mdc`).
- Use tools like Swagger/OpenAPI to document the API and even generate client-side code.

### CORS

- The Spring Boot application must be configured to accept requests from the frontend's origin (e.g., `http://localhost:3000` in development). See `api-rest-http.mdc` for CORS configuration.

### Authentication

- For decoupled frontends, use a token-based authentication mechanism like JWT (JSON Web Tokens).
- The Spring Boot backend will be responsible for issuing, validating, and refreshing tokens. Spring Security provides robust support for this.

## Data Fetching

- The frontend application should use a modern data-fetching library (like React Query, SWR, or Apollo Client) to handle API requests, caching, and state synchronization.
- All API calls from the frontend should be directed to the endpoints exposed by the Spring Boot application.

## Environment Configuration

- The frontend application should have separate configurations for different environments (development, staging, production).
- The API base URL for the Spring Boot backend should be an environment variable in the frontend application.

## Best Practices Summary

- Prefer a decoupled frontend architecture for flexibility and scalability.
- Maintain a clear and versioned API contract.
- Use token-based authentication (e.g., JWT) managed by Spring Security.
- Configure CORS correctly on the backend.
- Use environment variables to manage API endpoints on the frontend.