---
trigger: model_decision
---

# Logging Standards (Spring Boot)

## Runtime Logging

- Use **SLF4J** with **Logback** (the default in Spring Boot) for all logging.
- Configure logging in `application.properties` or `application.yml`. For more advanced configuration, use `logback-spring.xml`.
- Log in a structured format (like JSON) in production environments for easier parsing.

## Log Levels

### DEBUG

- Detailed diagnostics for development: method entry/exit, variable values, detailed state.
- Should be disabled in production.

### INFO

- Normal application flow: server startup, key business operations, configuration details.

### WARN

- Potential issues that don't break the application: recoverable errors, deprecated API usage, configuration problems.

### ERROR

- Critical errors requiring attention: unhandled exceptions, failed business transactions, connectivity issues.

## Best Practices

### Output Configuration

- Log to `stdout`/`stderr`. Rely on the deployment environment (e.g., Docker, Kubernetes) to handle log aggregation and storage.
- Avoid logging to files directly from the application.

### Sensitive Data

- **Never** log secrets, API keys, passwords, or Personally Identifiable Information (PII).
- Use Mapped Diagnostic Context (MDC) to add contextual information (like a request trace ID) to logs.

### Message Clarity

- Write clear, concise log messages.
- Use parameterized logging (`log.info("User {} created", userId);`) to improve performance and prevent log injection vulnerabilities.

## Spring Boot Examples

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private static final Logger log = LoggerFactory.getLogger(MyService.class);

    public void doSomething(String input) {
        log.debug("Starting operation with input: {}", input);

        try {
            // ... business logic
            log.info("Operation completed successfully for input: {}", input);
        } catch (Exception e) {
            log.error("Operation failed for input: {}", input, e);
            // Re-throw a custom exception or handle it
        }
    }
}
```

### Configuration (`application.properties`)

```properties
# Set the default log level for the application
logging.level.root=WARN

# Set a specific log level for your application's packages
logging.level.com.company.project=INFO

# Example of setting a specific level for a class
# logging.level.com.company.project.service.MyService=DEBUG
```

## Environments

- **Development**: `INFO` or `DEBUG`
- **Staging**: `INFO`
- **Production**: `WARN` or `ERROR`, with specific packages set to `INFO` as needed.