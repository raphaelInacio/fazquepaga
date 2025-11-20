---
alwaysApply: true
---

# Code Standards

## Language Requirements

- All source code must be written in **English**
- The primary language for the backend is **Java**.

## Naming Conventions

### Case Styles

- **camelCase**: Methods and variables.
  - `getUserById()`, `calculateTotal()`, `isActive`
- **PascalCase**: Classes and interfaces.
  - `UserService`, `OrderRepository`, `PaymentGateway`
- **kebab-case**: Resource files (e.g., `.properties`, `.yml`, `.xml`).
- **package_name**: `com.company.project.module`

### Naming Guidelines

- Avoid abbreviations (exception: well-known terms like `API`, `URL`, `DTO`).
- Keep names descriptive but concise.
- Use meaningful, self-documenting names.

### Examples

```java
// Good
String userAuthenticationToken = "...";
void calculateOrderTotal() {}
class CustomerRepository {}

// Avoid
String usrAuthTkn = "...";
void calc() {}
class CustRepo {}
```

## Code Organization

### Constants and Magic Numbers

- Declare constants for all magic numbers using `public static final`.
- Use descriptive names that explain the value's purpose.

```java
// Good
public static final int MAX_RETRY_ATTEMPTS = 3;
public static final int DEFAULT_PAGE_SIZE = 20;
public static final double TAX_RATE = 0.08;

// Avoid
if (retries > 3) { ... }
int pageSize = 20;
```

## Functions and Methods

### Naming Rules

- Start with a verb that clearly describes the action.
- Never start with a noun.

```java
// Good
void createUser() {}
void validateEmail() {}
boolean isValidDate() {}

// Avoid
void userCreation() {}
void emailValidation() {}
```

### Parameter Guidelines

- Limit to 3 parameters maximum.
- Use DTOs or builder patterns for multiple related parameters.

```java
// Good
void createOrder(String customerId, List<OrderItem> items, OrderOptions options) {}

// Better (when many parameters)
class CreateOrderParams {
    private String customerId;
    private List<OrderItem> items;
    private Address shippingAddress;
    // ... getters and setters or builder
}

void createOrder(CreateOrderParams params) {}
```

## Clean Code Principles

### Side Effects

- Avoid side effects in functions where possible.
- Separate queries from commands (CQRS pattern).
- A method should either return data OR cause a side effect, never both.

```java
// Good - Query
public User getUser(String id) {
  return userRepository.findById(id);
}

// Good - Command
public void updateUser(String id, UserData data) {
  // ... logic to update user
  userRepository.save(user);
}

// Avoid - Mixed responsibility
public User getUserAndLog(String id) {
  User user = userRepository.findById(id);
  log.info("User {} accessed", id); // Side effect in query
  return user;
}
```

### Control Flow

#### Early Returns

- Use guard clauses and early returns to avoid deep nesting.
- Maximum nesting level: 2.

```java
// Good
public void processOrder(Order order) {
  if (order == null) {
    throw new IllegalArgumentException("Order is required");
  }

  if (order.getItems().isEmpty()) {
    throw new IllegalArgumentException("Order must have items");
  }

  // Process order
}

// Avoid
public void processOrder(Order order) {
  if (order != null) {
    if (!order.getItems().isEmpty()) {
      // Process order
    } else {
      throw new IllegalArgumentException("Order must have items");
    }
  } else {
    throw new IllegalArgumentException("Order is required");
  }
}
```

#### Flag Parameters

- Never use boolean flags to control method behavior.
- Create separate methods instead.

```java
// Good
void saveUser(User user) {}
void saveUserAsDraft(User user) {}

// Avoid
void saveUser(User user, boolean isDraft) {
  if (isDraft) {
    // Save as draft
  } else {
    // Save normally
  }
}
```

## Code Size Limits

### Methods and Functions

- Maximum 50 lines per method.
- If exceeding, consider extracting private helper methods.

### Classes

- Maximum 300 lines per class.
- If exceeding, consider splitting responsibilities (Single Responsibility Principle).

## Dependency Management

### Dependency Inversion

- Use Spring's Dependency Injection (`@Autowired`, constructor injection) to manage dependencies.
- Define interfaces for services and repositories.

```java
interface PaymentGateway {
  PaymentResult processPayment(double amount);
}

@Service
class PaymentService {
  private final PaymentGateway gateway;

  @Autowired
  public PaymentService(PaymentGateway gateway) {
    this.gateway = gateway;
  }

  public void processOrder(Order order) {
    gateway.processPayment(order.getTotal());
  }
}
```

## Code Formatting

### Blank Lines

- Avoid excessive blank lines within methods.
- Use blank lines to separate logical blocks of code.
- Use blank lines between methods.

### Comments

- Avoid comments when possible; write self-documenting code instead.
- Use Javadoc for public APIs.
- Use comments only for complex algorithms or business rule clarifications.

### Variable Declaration

- Declare one variable per line.
- Declare variables close to where they're used.

```java
// Good
String firstName = user.getFirstName();
String lastName = user.getLastName();

// Avoid
String firstName = user.getFirstName(), lastName = user.getLastName();
```

## Design Principles

### Composition Over Inheritance

- Prefer composition to class inheritance.
- Use interfaces for contracts and dependency injection to compose behaviors.

```java
// Good - Composition
interface Logger {
  void log(String message);
}

@Service
class UserService {
  private final Logger logger;

  @Autowired
  public UserService(Logger logger) {
    this.logger = logger;
  }
}

// Avoid - Inheritance
class BaseService {
  protected void log(String message) { /* ... */ }
}

class UserService extends BaseService {}
```

## Best Practices Summary

1. Write code in English with clear naming.
2. Keep methods small and focused.
3. Minimize nesting with guard clauses.
4. Separate concerns clearly (SRP).
5. Prefer composition over inheritance.
6. Write self-documenting code with Javadoc for public APIs.
7. Apply SOLID principles consistently.
8. Maintain consistent code style throughout the project.