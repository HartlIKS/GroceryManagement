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
- **Framework**: Angular 21 with TypeScript 5.9
- **State Management**: Angular Signals (reactive state management)
- **UI Components**: Angular Material 21.1.1 + Angular CDK
- **Build Tool**: Angular CLI 21.1.0
- **Package Manager**: npm 11.6.2
- **Routing**: Lazy loading with feature-based route modules
- **Performance**: Optimized initial bundle size with on-demand component loading

## Project Structure

```
GroceryManagement/
├── pom.xml                           # Maven configuration
├── docker-compose.yaml               # PostgreSQL container
├── src/                              # Backend source code
│   ├── main/
│   │   ├── java/de/iks/grocery_manager/server/
│   │   │   ├── GroceryManagementApplication.java  # Main application class
│   │   │   ├── config/              # Security and configuration
│   │   │   ├── controller/          # REST controllers
│   │   │   │   └── masterdata/      # Master data controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   └── masterdata/      # Master data DTOs
│   │   │   ├── jpa/                 # JPA repositories
│   │   │   │   └── masterdata/      # Master data repositories
│   │   │   └── model/               # JPA entities
│   │   │       └── masterdata/      # Master data entities
│   │   └── resources/               # Application resources
│   └── test/                        # Backend tests
│       └── java/de/iks/grocery_manager/server/
│           ├── Testdata.java                          # Test data constants
│           └── controller/          # Controller tests
└── frontend/                        # Angular frontend
    ├── package.json                 # Node.js dependencies
    ├── angular.json                 # Angular configuration
    ├── src/
    │   ├── app/                     # Angular application
    │   │   ├── app.config.ts        # Application configuration
    │   │   ├── app.routes.ts        # Main routing with lazy loading
    │   │   ├── components/          # Shared components
    │   │   │   └── navigation/      # Navigation components
    │   │   ├── master-data/        # Master data interface
    │   │   │   ├── components/      # Master data components
    │   │   │   │   ├── dashboard/   # Dashboard component
    │   │   │   │   ├── products/    # Product management
    │   │   │   │   ├── stores/      # Store management
    │   │   │   │   └── prices/      # Price management
    │   │   │   ├── models/          # Master data models
    │   │   │   ├── services/        # Master data services
    │   │   │   ├── products.routes.ts    # Lazy-loaded product routes
    │   │   │   ├── stores.routes.ts      # Lazy-loaded store routes
    │   │   │   └── prices.routes.ts      # Lazy-loaded price routes
    │   │   ├── user-interface/     # User interface
    │   │   │   ├── components/      # User interface components
    │   │   │   │   ├── product-groups/  # Product group management
    │   │   │   │   ├── shopping-lists/  # Shopping list management
    │   │   │   │   ├── shopping-trips/  # Shopping trip management
    │   │   │   │   ├── planboard/       # Shopping planning interface
    │   │   │   │   ├── product-listing/ # Product price listing component
    │   │   │   │   └── user-dashboard.component.*  # User dashboard (eagerly loaded)
    │   │   │   ├── models/          # User interface models
    │   │   │   ├── services/        # User interface services
    │   │   │   ├── product-groups.routes.ts   # Lazy-loaded product group routes
    │   │   │   ├── shopping-lists.routes.ts   # Lazy-loaded shopping list routes
    │   │   │   └── shopping-trips.routes.ts  # Lazy-loaded shopping trip routes
    │   │   ├── models/              # Global models
    │   │   └── services/            # Global services
    │   ├── environments/            # Environment configurations
    │   ├── index.html               # Entry HTML
    │   ├── main.ts                  # Application bootstrap
    │   ├── material-theme.scss      # Angular Material theme
    │   └── styles.scss              # Global styles
    ├── public/                      # Public assets
    │   └── favicon.ico              # Favicon
    ├── .angular/                    # Angular CLI cache
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

### ShoppingList
- **uuid**: Primary identifier (UUID)
- **name**: List name (required, String)
- **repeating**: Whether the list repeats periodically (required, boolean, default: false)
- **owner**: User who owns the list (required, String)
- **products**: Map of products to quantities (Map<Product, BigDecimal>)
- **productGroups**: Map of product groups to quantities (Map<ProductGroup, BigDecimal>)

Shopping lists allow users to create personalized grocery lists with specific quantities for individual products or product groups. Users can only access their own shopping lists. The repeating flag indicates whether this is a recurring shopping list that should be periodically recreated or reminded.

### ShoppingTrip
- **uuid**: Primary identifier (UUID)
- **store**: Reference to Store (Store, required)
- **time**: When the shopping trip occurred (ZonedDateTime, required)
- **products**: Map of products to quantities purchased (Map<Product, BigDecimal>)
- **owner**: User who owns the trip (required, String)

Shopping trips record actual purchases made by users at specific stores, including quantities and prices paid. Users can only access their own shopping trips.

## User Interface Features

### User Dashboard
- **Main landing page** for regular users (eagerly loaded for optimal performance)
- **Navigation** to product groups, shopping lists, and other features
- **Real-time statistics** for product groups and shopping lists
- **Modern Angular Material design** with responsive layout
- **Fast initial load** - Critical component loaded in main bundle

### Shopping Lists Management
- **Complete CRUD operations** for shopping lists (user-scoped)
- **Repeating list support** - Flag to indicate recurring shopping lists for periodic reminders
- **Unified item management** - Single table for both products and product groups
- **Visual item distinction** - Background colors differentiate item types
- **Sample images** - Product groups display sample images from contained products
- **Unified dropdown** - Single dropdown for adding both products and groups
- **Real-time updates** with reactive form controls and optimistic UI changes
- **Amount management** - Editable quantities with proper validation
- **Type-aware interactions** - Contextual tooltips and actions per item type

### Shopping Trips Management
- **Complete CRUD operations** for shopping trips (user-scoped)
- **Trip checklist interface** for tracking items during shopping
- **Store and date selection** with proper validation
- **Product quantity management** with real-time updates
- **Historical trip tracking** with purchase records

### Planboard (Multi-Stage Shopping Planning)
- **Stage 1 - Interactive Planning**: Main planning interface for organizing shopping trips (`/planboard`)
- **Stage 2 - Trip Finalization**: Trip planning with existing trip selection and date assignment (`/planboard/trip-planning`)
- **Shopping list integration** - Import and plan items from existing lists
- **Store assignment** - Assign items to specific stores based on prices
- **Price comparison** - View and compare prices across different stores
- **Visual item management** - Organized component structure with dedicated cards for each trip
- **Planned trip generation** - Create optimized shopping trips
- **Component Architecture**: Modular design with shared product display components

### Product Listing Component
- **Price display component** for showing product prices across stores
- **Store-specific pricing** - Filter prices by store and date
- **Real-time price fetching** - Integrated with backend price search API
- **Currency formatting** - Proper display of prices with store currency

### Product Groups Management
- **Complete CRUD operations** for product groups
- **Product assignment** - Add/remove products from groups
- **Visual product selection** with images in dropdowns
- **Real-time updates** with optimistic UI changes
- **Responsive table design** with proper column widths

### Key Features
- **Lazy Loading Architecture**: Feature-based route modules reduce initial bundle size
- **Product Images**: Displayed in dropdowns and tables when available
- **Signal-based State Management**: Using Angular 21+ signals for reactive updates
- **Unified Interface**: Single table and dropdown for mixed item types (products + groups)
- **Visual Type Distinction**: CSS-based color coding for different item types
- **Sample Image Display**: Product groups show sample images from contained products
- **Reactive Form Controls**: Real-time validation and updates without event handlers
- **Consistent Styling**: Matches master data interface patterns with global design system
- **Modern Control Flow**: Uses `@if` and `@for` instead of deprecated `*ngIf` and `*ngFor`
- **Global Design System**: Centralized styling with CSS custom properties and inheritance
- **Hex Alpha Colors**: Modern color format with transparency support
- **Component CSS Optimization**: Leverages global styles to reduce redundancy
- **On-Demand Loading**: Components loaded only when navigating to specific features
- **Optimized Bundle Size**: Initial load contains only essential components and navigation
- **Planboard Integration**: Advanced multi-stage shopping planning with price optimization and store assignment
- **Multi-Stage Architecture**: Stage 1 for planning, Stage 2 for trip finalization
- **Component Modularity**: Shared product display components across different planboard stages
- **Shopping Trip Checklists**: Interactive tracking for in-store shopping experience
- **Real-time Price Fetching**: Dynamic price comparison across multiple stores

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

### Shopping Lists (`/shoppingLists`)
- `GET /shoppingLists/{uuid}` - Get shopping list by UUID (user-owned only)
- `PUT /shoppingLists/{uuid}` - Update shopping list (user-owned only)
- `POST /shoppingLists` - Create new shopping list
- `DELETE /shoppingLists/{uuid}` - Delete shopping list (user-owned only)
- `GET /shoppingLists` - Search shopping lists (with optional name parameter, paginated, user-owned only)

### Shopping Trips (`/shoppingTrips`)
- `GET /shoppingTrips/{uuid}` - Get shopping trip by UUID (user-owned only)
- `PUT /shoppingTrips/{uuid}` - Update shopping trip (user-owned only)
- `POST /shoppingTrips` - Create new shopping trip
- `DELETE /shoppingTrips/{uuid}` - Delete shopping trip (user-owned only)
- `GET /shoppingTrips` - Search shopping trips (with optional from/to date parameters, paginated, user-owned only)
- `POST /shoppingTrips/{uuid}/add` - Add products to existing shopping trip (user-owned only)

### Planboard (Frontend Feature)
- **Route**: `/planboard` - Interactive shopping planning interface
- **Features**: Shopping list integration, price comparison, store assignment, planned trip generation

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
- Angular 21.1.0
- Angular Material 21.1.1
- Angular Signals (state management)
- TypeScript 5.9.2
- RxJS 7.8.0
- Modern Control Flow (`@if`, `@for`)
- CSS Custom Properties (design system)
- Hex Alpha Color Format (modern colors)
- Lazy Loading with feature-based routing
- On-demand component loading for performance optimization

## Security Configuration
- OAuth2 Resource Server configuration
- WebAuthn support for passwordless authentication
- CORS configuration for frontend integration

## Database Migration
- Liquibase for database schema management
- Migration files located in `src/main/resources/db/migration/`

## Testing
- Backend: JUnit 5, Spring Boot Test, MockMvc for controller testing
- Frontend: Vitest, Angular Testing Utilities
- Comprehensive test coverage for all CRUD operations
- Security testing with JWT authentication
- Data integrity testing with foreign key constraints
- ShoppingTripController tests with full CRUD coverage, ownership validation, and add-to-trip functionality

## API Documentation
- Swagger UI available at: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec at: `http://localhost:8080/v3/api-docs`

## Monitoring & Metrics
- Spring Boot Actuator endpoints
- Prometheus metrics integration
- Health checks at `/actuator/health`

## Development Notes
- The project uses MapStruct 1.6.3 for DTO-to-entity mapping
- Lombok reduces boilerplate code in entities and DTOs
- Frontend uses Angular Material 21.1.1 for consistent UI components
- Angular Signals manage reactive state in the frontend (replaced NgRx for simplicity)
- Modern control flow (`@if`, `@for`) replaces deprecated structural directives
- Global design system with CSS custom properties for consistent styling
- Separated architecture: `master-data` for admin functions, `user-interface` for user-facing features
- **Lazy Loading Implementation**: Feature-based route modules optimize initial bundle size
- **Performance Optimization**: UserDashboard eagerly loaded for fast landing page experience
- Shopping Lists feature implements unified table for mixed item types with visual distinction
- CSS inheritance pattern with base classes (.item-type) and type-specific extensions
- Hex alpha color format (#RRGGBBAA) for modern color management with transparency
- Component CSS optimization leverages global styles to minimize redundancy
- Reactive form controls use valueChanges subscriptions instead of (input) event handlers
- Sample image logic displays product previews for product groups in both dropdowns and tables
- Type-aware UI interactions provide contextual tooltips and actions based on item type
- Planboard component provides advanced shopping planning with price optimization and store assignment
- Shopping trip checklist feature enables interactive in-store shopping experience
- Product listing component offers real-time price comparison across multiple stores
