# Software Design Patterns Research

This document outlines six software design patterns, categorised by their types.

## 1. Builder Pattern
* **Category**: Creational
* **Problem it solves**: Constructing complex objects can result in massive constructors with numerous parameters ("telescoping constructors"). This makes code difficult to read, maintain, and prone to errors.
* **How it works**: The Builder pattern separates the construction of a complex object from its representation. It provides a step-by-step approach to build an object, allowing you to create different representations using the same construction process.
* **Real-world example**: Building an `HttpClient` request or configuring a `UserSession` in a backend framework (where optional headers and body parameters exist).
* **Possible use case in our project**: Used in `AuthResponse.java` to construct the authentication response token, refresh token, and user email securely without needing a four-argument constructor that could mix up strings.

## 2. Factory Method Pattern
* **Category**: Creational
* **Problem it solves**: When the exact types and dependencies of objects being created dictate to the system what to do, introducing new types requires modifying existing code, violating the Open-Closed Principle.
* **How it works**: It provides an interface for creating objects in a superclass, but allows subclasses to alter the type of objects that will be created.
* **Real-world example**: Generating different UI buttons (WindowsButton vs MacButton) or mapping different Payment services (Stripe, PayPal).
* **Possible use case in our project**: Used in `NotificationFactory` to instantiate the appropriate `Notification` strategy (e.g., `EmailNotification` vs `SmsNotification`) depending on user preference after registration.

## 3. Adapter Pattern
* **Category**: Structural
* **Problem it solves**: Incompatible interfaces between two classes or systems that prevent them from working together (e.g., integrating a legacy system with a new interface, or wrapping an external SDK).
* **How it works**: The Adapter acts as a wrapper that translates one interface into another that the client expects.
* **Real-world example**: Using external third-party authentication services (like Firebase or Supabase), wrapping them in a standard application interface so that the whole app doesn't need to know the third-party framework's syntax.
* **Possible use case in our project**: Used to create an `AuthClient` interface and a `SupabaseAuthClientAdapter`. This hides the specific `RestTemplate` API routes of Supabase from the rest of the application.

## 4. Facade Pattern
* **Category**: Structural
* **Problem it solves**: A system relies on many interdependent classes or subsystems to perform a simple task. This introduces tight coupling and makes the client code bloated and hard to understand.
* **How it works**: A Facade provides a simplified, higher-level interface to a complex subsystem. It delegates client requests to the appropriate objects within the subsystem.
* **Real-world example**: A "Purchase Order" system that coordinates payment gateways, inventory checking, and shipping services behind a simple `placeOrder()` method.
* **Possible use case in our project**: Used in `AuthFacade` to coordinate `ValidationStrategy`, `AuthClient`, Event Observers, and `NotificationFactory` inside a single simplified registration flow.

## 5. Strategy Pattern
* **Category**: Behavioral
* **Problem it solves**: Having multiple algorithms for a specific task and hardcoding them inside massive `if-else` or `switch` statements makes the class bloated and difficult to extend.
* **How it works**: It defines a family of algorithms, encapsulates each one, and makes them interchangeable. The strategy lets the algorithm vary independently from clients that use it.
* **Real-world example**: Navigation apps offering different routing strategies (Walk, Drive, Transit) or an e-commerce cart that uses different discount calculation strategies.
* **Possible use case in our project**: Used via `ValidationStrategy` to validate a `RegisterRequest` (e.g. `EmailValidationStrategy` and potentially `PasswordValidationStrategy`), allowing new rules to be added without changing the auth flow logic.

## 6. Observer Pattern
* **Category**: Behavioral
* **Problem it solves**: An object needs to notify other objects about its state changes, but keeping direct references to these objects creates tight coupling and monolithic architecture.
* **How it works**: The Observer pattern defines a one-to-many subscription mechanism to notify multiple objects (observers) about any events that happen to the object they're observing (subject).
* **Real-world example**: A newsletter subscription (publish-subscribe) or the classic Model-View-Controller where Views observe changes in the Model.
* **Possible use case in our project**: Using Spring's `ApplicationEventPublisher` to emit a `UserRegisteredEvent` which is non-blockingly consumed by a `UserRegistrationAuditListener` to write to logs without tightly coupling auth logic to logging code.
