# TheKnife Project Analysis

## Project Overview

TheKnife is a Java-based restaurant information and review system built with JavaFX. The application appears to focus on Michelin restaurants, providing users with detailed restaurant information, reviews, and ratings. The system uses a Model-View-Controller (MVC) architecture and leverages modern Java features (Java 21) with Lombok for reduced boilerplate code.

## Architecture Analysis

### System Architecture

```mermaid
C4Context
    title System Context Diagram - TheKnife

    Person(user, "User", "A user of the restaurant system")
    System(theKnife, "TheKnife", "Allows users to browse restaurant information, read and write reviews, and manage restaurant data")

    Rel(user, theKnife, "Uses")
```

### Component Architecture

```mermaid
C4Component
    title Component Diagram - TheKnife

    Container_Boundary(app, "TheKnife Application") {
        Component(ui, "UI Layer", "JavaFX", "Provides the user interface via FXML")
        Component(controller, "Controller Layer", "Java", "Handles user interactions and updates the UI")
        Component(service, "Service Layer", "Java", "Implements business logic")
        Component(repository, "Repository Layer", "Java", "Handles data persistence and retrieval")
        Component(model, "Model Layer", "Java", "Defines data structures")
    }

    Rel(ui, controller, "Invokes")
    Rel(controller, service, "Uses")
    Rel(service, repository, "Uses")
    Rel(repository, model, "Manages")
    Rel(controller, model, "Uses")
```

### Class Diagram

```mermaid
classDiagram
    class Restaurant {
        +String name
        +String address
        +String location
        +String price
        +String cuisine
        +float longitude
        +float latitude
        +String phone
        +String michelinUrl
        +String websiteUrl
        +String award
        +int greenStar
        +String facilities
        +String description
        +double distance
        +List~Review~ reviews
        +User user
    }

    class Coordinate {
        +float longitude
        +float latitude
    }

    class User {
        +String username
        +String firstName
        +String lastName
        +String password
        +LocalDate birthDate
        +String city
        +Role role
        +List~Review~ reviews
        +HashSet~Restaurant~ restaurants
    }

    class Review {
        +User user
        +Restaurant restaurant
        +String content
        +Integer stars
        +String answer
    }

    class Role {
        // Role properties
    }

    Restaurant *-- Coordinate : has
    Restaurant "1" -- "*" Review : has
    User "1" -- "*" Review : writes
    User "1" -- "*" Restaurant : saves
    User "1" -- "1" Role : has
```

## Technical Analysis

### Technology Stack

```mermaid
flowchart TD
    java21["Java 21"] --> theknife["TheKnife Application"]
    javafx["JavaFX 23.0.1"] --> theknife
    lombok["Lombok 1.18.34"] --> theknife
    opencsv["OpenCSV 5.10"] --> theknife
    jackson["Jackson 2.18.2"] --> theknife
    springsecurity["Spring Security Core 6.4.4"] --> theknife
    maven["Apache Maven"] --> build["Build System"]
```

### Design Patterns

Based on the analyzed code, the project implements several design patterns:

1. **MVC (Model-View-Controller)** - Separates the application into Model (data), View (UI), and Controller (business logic)
2. **Repository Pattern** - Abstracts data access through repository classes
3. **Builder Pattern** - Implemented via Lombok's @Accessors(chain = true) for fluent APIs
4. **Dependency Injection** - Likely used for service and repository dependencies

## Data Model Analysis

### Core Entities

- **Restaurant**: Represents a restaurant with detailed information
- **User**: Represents a system user with personal information and authentication details
- **Review**: Contains a user's review of a restaurant including ratings and content
- **Role**: Represents user roles for authorization
- **FilterOptions**: Represents the active set of filters applied by the user to the list of restaurants

### Detailed Entity Analysis

#### Restaurant
The Restaurant class is the central entity in the application, containing comprehensive information about Michelin restaurants:
- **Basic Information**: name, address, location, price range, cuisine type
- **Geographical Data**: longitude, latitude (used for distance calculations)
- **Contact Information**: phone number, website URL, Michelin URL
- **Michelin-specific Data**: award status, green star rating (for sustainability)
- **Additional Details**: facilities, description
- **Relationships**: associated reviews, owner (if applicable)

The class includes a nested Coordinate class for handling geographical positioning.

#### User
The User class represents application users with different roles:
- **Authentication Data**: username (unique identifier), password (stored in encrypted form)
- **Personal Information**: first name, last name, birth date, city
- **Role**: determines user permissions (customer, restaurant owner)
- **Relationships**: saved favorite restaurants

#### Review
The Review class connects users and restaurants through reviews:
- **Core Data**: content (text review), star rating (numerical score)
- **Relationships**: user (author), restaurant (subject)
- **Owner Response**: answer field for restaurant owner responses
- **Unique Identifier**: id field to uniquely identify each review

#### Role
The Role enum defines user permission levels:
- **CLIENTE**: Regular user who can browse restaurants and write reviews
- **RISTORATORE**: Restaurant owner who can manage restaurant information and respond to reviews

#### FilterOptions
The FilterOptions class enables restaurant filtering:
- **Filter Criteria**: cuisine, location, price, star rating
- **Service Filters**: delivery availability, online booking availability
- **Functionality**: provides methods to check if a restaurant matches all active filters

### Service Layer Analysis

The application implements several services to handle business logic:

#### SessionService
Central service managing application state:
- **User Session**: tracks the currently logged-in user
- **Navigation State**: manages the current view/scene and selected restaurant
- **Filter State**: stores active filter options
- **Data Cache**: maintains the list of loaded restaurants

#### SecurityService
Handles authentication security:
- **Password Encoding**: uses BCrypt hashing for secure password storage
- **Password Validation**: verifies user credentials during login

#### AlertService
Provides a unified interface for user notifications:
- **Alert Display**: shows information, warnings, and error messages
- **Consistent UI**: ensures uniform appearance of notification dialogs

### Repository Layer Analysis

The application uses repository classes to abstract data access:

#### RestaurantRepository
Manages restaurant data persistence:
- **Data Loading**: loads restaurant data from CSV (initial) and JSON (ongoing)
- **CRUD Operations**: provides methods to create, read, update, and delete restaurants
- **Search Functionality**: implements filtering and searching of restaurants

#### UserRepository
Handles user data persistence:
- **User Management**: stores and retrieves user information
- **Authentication Support**: provides methods for user lookup during login
- **Favorites Management**: handles saving and retrieving user's favorite restaurants

#### ReviewsRepository
Manages review data:
- **Review Storage**: persists review data to JSON files
- **Restaurant-Review Association**: maintains the relationship between restaurants and their reviews
- **Owner Responses**: supports storing restaurant owner responses to reviews

### Data Flow

```mermaid
flowchart LR
    subgraph CSV["CSV Data"] 
      restaurants_csv["Initial Restaurant Data"]
    end

    subgraph JSON["JSON Data"] 
      restaurants_json["Restaurant Data"]
      users["User Data"]
      reviews["Review Data"]
    end

    subgraph Application
      subgraph repos["Repository Layer"]
        restaurant_repo["RestaurantRepository"]
        user_repo["UserRepository"]
        reviews_repo["ReviewsRepository"]
      end

      subgraph services["Service Layer"]
        session_service["SessionService"]
        security_service["SecurityService"]
        alert_service["AlertService"]
      end

      subgraph controllers["Controller Layer"]
        home_controller["HomeController"]
        login_controller["LoginController"]
        register_controller["RegisterController"]
        restaurant_controller["RestaurantController"]
        filters_controller["FiltersController"]
      end

      views["Views (FXML)"] 
    end

    restaurants_csv --> restaurant_repo
    restaurants_json --> restaurant_repo
    users --> user_repo
    reviews --> reviews_repo

    restaurant_repo --> restaurants_json
    user_repo --> users
    reviews_repo --> reviews

    restaurant_repo --> session_service
    user_repo --> security_service
    reviews_repo --> restaurant_controller

    session_service --> home_controller
    session_service --> restaurant_controller
    session_service --> filters_controller
    security_service --> login_controller
    security_service --> register_controller
    alert_service --> controllers

    controllers --> views
    views --> controllers
```

## Feature Analysis

### Core Features

1. **Restaurant Browsing**: Users can view a list of restaurants with details
2. **Restaurant Details**: Detailed view of restaurant information
3. **User Management**: Registration, login, and profile management
4. **Review System**: Users can read and write restaurant reviews
5. **Favorites/Bookmarks**: Users can save favorite restaurants

### User Flow

```mermaid
stateDiagram-v2
    [*] --> Login
    Login --> Browse: Successful login
    Login --> Register: New user
    Register --> Login: Registration complete

    Browse --> RestaurantDetails: Select restaurant
    RestaurantDetails --> AddReview: Write review
    RestaurantDetails --> SaveFavorite: Add to favorites

    AddReview --> RestaurantDetails: Review submitted
    SaveFavorite --> RestaurantDetails: Restaurant saved

    RestaurantDetails --> Browse: Back to list
    Browse --> Profile: View profile
    Profile --> Favorites: View favorites
    Profile --> UserReviews: View user reviews

    Favorites --> RestaurantDetails: Select favorite
    UserReviews --> RestaurantDetails: Select reviewed restaurant

    Profile --> Browse: Back to browsing
```