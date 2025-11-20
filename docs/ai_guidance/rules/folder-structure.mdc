---
alwaysApply: false
---

# Project Folder Structure (Spring Boot)

This repository uses a standard Maven/Gradle project structure for the Spring Boot backend.

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/company/project/
│   │   │       ├── Application.java         # Main Spring Boot application class
│   │   │       ├── config/                  # Spring configuration classes
│   │   │       ├── controller/              # REST controllers
│   │   │       ├── dto/                     # Data Transfer Objects
│   │   │       ├── model/ (or domain, entity) # JPA entities
│   │   │       ├── repository/              # Spring Data repositories
│   │   │       ├── service/                 # Business logic services
│   │   │       └── exception/               # Custom exception classes
│   │   └── resources/
│   │       ├── application.properties       # Main application configuration
│   │       ├── static/                      # Static assets (CSS, JS, images)
│   │       ├── templates/                   # Server-side templates (e.g., Thymeleaf)
│   │       └── db/migration/                # Database migrations (Flyway/Liquibase)
│   │
│   └── test/
│       └── java/
│           └── com/company/project/
│               ├── controller/
│               ├── service/
│               └── repository/
│
├── pom.xml (or build.gradle)      # Project dependencies and build configuration
└── README.md
```

## Principles

- Follow the standard Maven/Gradle directory layout.
- Group classes by feature or layer (e.g., `controller`, `service`, `repository`).
- Use a consistent root package structure (e.g., `com.company.project`).

## Naming Conventions

- Use `PascalCase` for Java class files.
- Use `kebab-case` or `snake_case` for files in `src/main/resources`.
- Package names should be all lowercase.

## Module Organization

- One public class per `.java` file.
- Keep related classes within the same package.