# Refactoring Report

## Introduction
The objective of this refactoring was to apply Design Patterns to improve the existing software architecture of our DineBook application. By shifting the codebase towards known patterns, we aim to enhance code maintainability, decouple components, and allow for easier scaling in the future.

## Code Before Refactoring

### Original Implementation
Originally, our `AuthController` directly called an `AuthService`. The `AuthService` handled the following responsibilities in a single tightly-coupled file:
1. Constructing request options to Supabase.
2. Managing REST templates to invoke third-party routes (`/auth/v1/signup`).
3. Returning standard HTTP entities.

### Identified Problems
* **Violates Single Responsibility Principle**: `AuthService` handled too much logic related to Supabase networking, making it hard to maintain.
* **Tight Coupling**: Hardcoding external dependencies (Supabase) in the primary service means migrating to Firebase or Auth0 later would require modifying core service logic.
* **Lack of Extensibility**: Adding features like email validation, notifications upon registration, or audit logging would bloat the `AuthService` with unrelated business rules.

---

## Applied Design Patterns

We introduced **6 Design Patterns** to address these architectural concerns. 

### 1. Adapter Pattern (Structural)
* **Where it was applied**: Extracted Supabase logic into `SupabaseAuthClientAdapter` implementing an `AuthClient` interface.
* **Justification**: This decouples the application from a specific vendor. If we choose to move away from Supabase, we can just create a `FirebaseAuthClientAdapter` without modifying business logic.
* **Improvement**: Clean separation of vendor communication from internal business rules.

### 2. Facade Pattern (Structural)
* **Where it was applied**: Created an `AuthFacade`. 
* **Justification**: `AuthFacade` acts as the primary orchestrator that coordinates validation, persistence (using the adapter), event publishing, and notification dispatches. 
* **Improvement**: `AuthController` regains its simplicity, just talking to one clean interface while complex multi-step workflows are coordinated safely in the Facade.

### 3. Strategy Pattern (Behavioral)
* **Where it was applied**: Created a `ValidationStrategy` interface and implemented `EmailValidationStrategy`.
* **Justification**: We need to validate inputs before proceeding with registration. By using strategies, we can cleanly inject multiple validation tests inside a List and throw exceptions if they fail, completely eliminating massive if/else validation chains.
* **Improvement**: Adding a `PasswordLengthValidationStrategy` now takes zero edits to the core authentication flow.

### 4. Builder Pattern (Creational)
* **Where it was applied**: Replaced the record/constructor approach for `AuthResponse` with a Builder: `AuthResponse.Builder()`.
* **Justification**: Creating complex responses or internal objects that require many optional or String parameters reduces readability and causes input errors (e.g., swapping refresh token with access token).
* **Improvement**: Immutability and explicit field definitions when creating responses.

### 5. Factory Method Pattern (Creational)
* **Where it was applied**: Created `NotificationFactory` and `EmailNotification` implementing `Notification`.
* **Justification**: Upon registration, we want to welcome the user. Different users might need Email vs SMS alerts. The factory determines which implementation class to instantiate.
* **Improvement**: Follows the Open-Closed Principle; adding an `SmsNotification` does not require changing the Facade.

### 6. Observer Pattern (Behavioral)
* **Where it was applied**: Added `UserRegisteredEvent` and `UserRegistrationAuditListener` using Spring's App Events mechanism.
* **Justification**: We need to perform non-critical operations (like Audit Logs, Metrics) immediately after registration. Putting these inside the `AuthFacade` breaks separation of concerns.
* **Improvement**: True decoupling. Observers can be added transparently to react to the system without mutating the publisher logic.

---

## Conclusion
By breaking up the original `AuthService`, the authentication component was upgraded from a monolith-like class into highly modular, testable, and robust enterprise patterns.
