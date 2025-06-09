# TheKnife Project Analysis

## Project Overview

TheKnife is a Java-based desktop application for restaurant discovery and review management, with a focus on Michelin-rated establishments. Built using JavaFX for the user interface, the application provides a platform for users to browse restaurant information, read and write reviews, and manage restaurant data.

## Technical Stack

- Java 21 
- JavaFX 
- Lombok for reducing boilerplate code
- JSON and CSV for data persistence
- Maven for build management

## Architecture

The application follows a layered architecture pattern:

### Layers
1. **View Layer (UI)**
   - JavaFX FXML for UI layouts
   - Custom components like ReviewCell for specialized display
   - CSS styling for consistent look and feel

2. **Controller Layer**
   - Handles user interactions
   - Manages view state and updates
   - Coordinates between view and service layer

3. **Service Layer**
   - Implementation of business logic
   - Session management
   - User authentication and authorization

4. **Repository Layer**
   - Data access and persistence
   - File-based storage using JSON and CSV

5. **Model Layer**
   - Core domain entities (Restaurant, Review, User)
   - Data validation and business rules

## Core Features

1. **User Management**
   - User registration and authentication
   - Role-based access control (Regular users vs Restaurant owners)
   - Session management

2. **Restaurant Management**
   - Detailed restaurant information display
   - Location-based restaurant discovery
   - Restaurant search functionality

3. **Review System**
   - Review creation and display
   - Star rating system
   - Restaurant owner responses to reviews
   - Custom review display with ReviewCell

4. **User Interface**
   - Responsive layout
   - Custom styling
   - Scrollable lists and detailed views

## Data Model

### Key Entities

1. **User**
   ```java
   - username: String
   - firstName: String
   - lastName: String
   - password: String
   - birthDate: LocalDate
   - city: String
   - role: Role
   - reviews: List<Review>
   - restaurants: Set<Restaurant>
   ```

2. **Restaurant**
   ```java
   - name: String
   - description: String
   - address: String
   - location: String
   - price: String
   - cuisine: String
   - coordinates: {longitude: float, latitude: float}
   - phone: String
   - michelinUrl: String
   - websiteUrl: String
   - award: String
   - greenStar: int
   - facilities: String
   - reviews: List<Review>
   ```

3. **Review**
   ```java
   - user: User
   - restaurant: Restaurant
   - content: String
   - stars: Integer
   - answer: String
   ```

## File Structure