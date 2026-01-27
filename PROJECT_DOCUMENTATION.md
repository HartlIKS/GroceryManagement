# Grocery Management System - Project Documentation

## Overview

This is a full-stack grocery management system consisting of a Spring Boot REST API backend and an Angular frontend. The system manages grocery products, stores, price listings, and product groups with comprehensive CRUD operations.

## Architecture

### Backend (Spring Boot 4.0.1)
- **Language**: Java 21
- **Framework**: Spring Boot with WebMVC
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with OAuth2 Resource Server and WebAuthn
- **Documentation**: OpenAPI 3.0 (SpringDoc)
- **Build Tool**: Maven
- **Migration**: Liquibase

### Frontend (Angular 21)
- **Framework**: Angular 21 with TypeScript
- **State Management**: NgRx Store
- **UI Components**: Angular Material + Angular CDK
- **Build Tool**: Angular CLI
- **Package Manager**: npm 11.6.2

## Project Structure

```
GroceryManagement/
├── pom.xml                           # Maven configuration
├── docker-compose.yaml               # PostgreSQL container
├── compose.yaml                      # Alternative Docker Compose
├── src/                              # Backend source code
│   ├── main/
│   │   ├── java/de/iks/grocery_manager/server/
│   │   │   ├── GroceryManagementApplication.java  # Main application class
│   │   │   ├── config/              # Security and configuration
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── jpa/                 # JPA repositories
│   │   │   └── model/               # JPA entities
│   │   └── resources/               # Application resources
│   └── test/                        # Backend tests
└── frontend/                        # Angular frontend
    ├── package.json                 # Node.js dependencies
    ├── angular.json                 # Angular configuration
    ├── src/
    │   ├── app/                     # Angular application
    │   │   ├── app.config.ts        # Application configuration
    │   │   ├── app.routes.ts        # Routing configuration
    │   │   └── components/          # Angular components
    │   ├── index.html               # Entry HTML
    │   ├── main.ts                  # Application bootstrap
    │   └── styles/                  # Global styles
    └── dist/                        # Build output (generated)
```

## Core Domain Models

### Product
- **uuid**: Primary identifier (UUID)
- **name**: Product name (required, String)
- **EAN**: European Article Number (optional, String)
- **image**: Image reference (optional, String)

### Store
- **uuid**: Primary identifier (UUID)
- **name**: Store name (required, String)
- **address**: Embedded address entity (Address)
- **logo**: Store logo (optional, String)
- **currency**: Store currency (required, Currency)

### PriceListing
- **uuid**: Primary identifier (UUID)
- **product**: Reference to Product (Product, required)
- **store**: Reference to Store (Store, required)
- **validFrom**: Date when price becomes valid (ZonedDateTime, required)
- **validTo**: Date when price expires (ZonedDateTime, required)
- **price**: Monetary value (BigDecimal, required)

### ProductGroup
- **uuid**: Primary identifier (UUID)
- **name**: Group name (required, String)
- **owner**: User who owns the group (required, String)
- **products**: List of products in the group (List<Product>)

Product groups allow users to group similar products together that can be substituted for one another. For example, different brands of milk or various types of pasta could be grouped together, allowing users to choose any product within the same group as a substitute.

### Address
- **country**: Country name (String)
- **city**: City name (String)
- **zip**: Postal/ZIP code (String)
- **street**: Street address (String)
- **number**: House number (String)

## API Endpoints

### Products (`/masterdata/product`)
- `GET /masterdata/product/{uuid}` - Get product by UUID
- `PUT /masterdata/product/{uuid}` - Update product
- `POST /masterdata/product` - Create new product
- `DELETE /masterdata/product/{uuid}` - Delete product
- `GET /masterdata/product` - Search products (with optional name parameter, paginated)

### Stores (`/masterdata/store`)
- `GET /masterdata/store/{uuid}` - Get store by UUID
- `PUT /masterdata/store/{uuid}` - Update store
- `POST /masterdata/store` - Create new store
- `DELETE /masterdata/store/{uuid}` - Delete store
- `GET /masterdata/store` - Search stores (with optional name parameter, paginated)

### Price Listings (`/masterdata/price`)
- `GET /masterdata/price/{uuid}` - Get price listing by UUID
- `PUT /masterdata/price/{uuid}` - Update price listing
- `POST /masterdata/price` - Create new price listing
- `DELETE /masterdata/price/{uuid}` - Delete price listing
- `GET /masterdata/price` - Get paginated price listings
- `GET /masterdata/price?at={timestamp}&products={product_uuids}&stores={store_uuids}` - Get prices by products, stores, and timestamp (returns nested map structure)

### Product Groups (`/productGroups`)
- `GET /productGroups/{uuid}` - Get product group by UUID
- `PUT /productGroups/{uuid}` - Update product group
- `POST /productGroups` - Create new product group
- `DELETE /productGroups/{uuid}` - Delete product group
- `GET /productGroups` - Search product groups (with optional name parameter, paginated)
- `PUT /productGroups/{group}/{product}` - Add product to group
- `DELETE /productGroups/{group}/{product}` - Remove product from group

## Development Setup

### Prerequisites
- Java 21
- Node.js 24.13.0
- Maven 3.x
- PostgreSQL (or Docker)

### Backend Setup
1. Start PostgreSQL using Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`

### Frontend Setup
1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start development server:
   ```bash
   npm start
   ```

The Angular app will be available at `http://localhost:4200`

### Production Build
The Maven build process automatically builds the frontend:
```bash
./mvnw clean package
```

This will:
1. Install Node.js and npm
2. Install frontend dependencies
3. Build the Angular application
4. Copy frontend build to Spring Boot static resources

## Key Technologies & Dependencies

### Backend Dependencies
- Spring Boot Starter WebMVC
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Liquibase
- PostgreSQL Driver
- MapStruct (DTO mapping)
- Lombok (boilerplate reduction)
- SpringDoc OpenAPI (API documentation)
- Micrometer Prometheus (metrics)

### Frontend Dependencies
- Angular 21
- Angular Material 21
- NgRx Store (state management)
- RxJS (reactive programming)
- TypeScript 5.9

## Security Configuration
- OAuth2 Resource Server configuration
- WebAuthn support for passwordless authentication
- CORS configuration for frontend integration

## Database Migration
- Liquibase for database schema management
- Migration files located in `src/main/resources/db/migration/`

## Testing
- Backend: JUnit 5, Spring Boot Test, Testcontainers
- Frontend: Vitest, Angular Testing Utilities

## API Documentation
- Swagger UI available at: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec at: `http://localhost:8080/v3/api-docs`

## Monitoring & Metrics
- Spring Boot Actuator endpoints
- Prometheus metrics integration
- Health checks at `/actuator/health`

## Development Notes
- The project uses MapStruct for DTO-to-entity mapping
- Lombok reduces boilerplate code in entities and DTOs
- Frontend uses Angular Material for consistent UI components
- NgRx Store manages application state on the frontend
- The build process integrates both backend and frontend into a single deployment artifact
