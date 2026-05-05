# Grocery Management System - Project Documentation

## Overview

This is a full-stack grocery management system consisting of a Spring Boot REST API backend and an Angular frontend. The system manages grocery products, stores, price listings, and product groups with comprehensive CRUD operations.

## Architecture

### Backend (Spring Boot 4.0.3)
- **Language**: Java 17
- **Framework**: Spring Boot with WebMVC
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with OAuth2 Resource Server and ShareFilter
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
│   │   │   │   ├── masterdata/      # Master data controllers
│   │   │   │   ├── mdi/             # External API integration controllers
│   │   │   │   └── share/           # Share functionality controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── masterdata/      # Master data DTOs
│   │   │   │   ├── mdi/             # External API integration DTOs
│   │   │   │   └── share/           # Share DTOs
│   │   │   ├── jpa/                 # JPA repositories
│   │   │   │   ├── masterdata/      # Master data repositories
│   │   │   │   ├── mdi/             # External API integration repositories
│   │   │   │   └── share/           # Share repositories
│   │   │   ├── mapping/             # MapStruct mappers
│   │   │   ├── model/               # JPA entities
│   │   │   │   ├── masterdata/      # Master data entities
│   │   │   │   ├── mdi/             # External API integration entities
│   │   │   │   └── share/           # Share entities
│   │   │   └── util/                # Utility classes
│   │   └── resources/               # Application resources
│   └── test/                        # Backend tests
│       └── java/de/iks/grocery_manager/server/
│           ├── Testdata.java                          # Test data constants
│           └── controller/          # Controller tests
│               ├── masterdata/      # Master data controller tests
│               ├── mdi/             # External API integration controller tests
│               └── share/           # Share controller tests
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
    │   │   │   │   ├── prices/      # Price management
    │   │   │   │   └── endpoints/   # External API endpoint management
    │   │   │   │       ├── endpoint-list/       # Endpoint list component
    │   │   │   │       ├── endpoint-test/       # Endpoint testing interface
    │   │   │   │       ├── external-api/       # External API management
    │   │   │   │       ├── mapping-list/       # Mapping table management
    │   │   │   │       ├── price-endpoint/     # Price endpoint configuration
    │   │   │   │       ├── product-endpoint/   # Product endpoint configuration
    │   │   │   │       └── store-endpoint/     # Store endpoint configuration
    │   │   │   ├── models/          # Master data models
    │   │   │   ├── services/        # Master data services
    │   │   │   ├── products.routes.ts    # Lazy-loaded product routes
    │   │   │   ├── stores.routes.ts      # Lazy-loaded store routes
    │   │   │   ├── prices.routes.ts      # Lazy-loaded price routes
    │   │   │   └── externalAPIRoutes.ts  # Lazy-loaded external API routes
    │   │   ├── user-interface/     # User interface
    │   │   │   ├── components/      # User interface components
    │   │   │   │   ├── product-groups/  # Product group management
    │   │   │   │   ├── shopping-lists/  # Shopping list management
    │   │   │   │   ├── shopping-trips/  # Shopping trip management
    │   │   │   │   ├── planboard/       # Shopping planning interface
    │   │   │   │   ├── product-listing/ # Product price listing component
    │   │   │   │   ├── store-listing/   # Store price listing component
    │   │   │   │   ├── share-admin/     # Share administration interface
    │   │   │   │   ├── join-link-form/  # Join link creation/editing form
    │   │   │   │   ├── join-share/      # Join link usage interface
    │   │   │   │   └── user-dashboard.component.*  # User dashboard (eagerly loaded)
    │   │   │   ├── models/          # User interface models
    │   │   │   ├── services/        # User interface services
    │   │   │   ├── product-groups.routes.ts   # Lazy-loaded product group routes
    │   │   │   ├── shopping-lists.routes.ts   # Lazy-loaded shopping list routes
    │   │   │   ├── shopping-trips.routes.ts  # Lazy-loaded shopping trip routes
    │   │   │   ├── share-admin.routes.ts    # Share administration routes
    │   │   │   └── join-link.routes.ts      # Join link form routes
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

Shopping lists allow users to create personalized grocery lists with specific quantities for individual products or product groups. Users can only access their own shopping lists. The repeating flag determines whether the list should be preserved after being planned to shopping trips - non-repeating lists are automatically deleted after successful trip creation, while repeating lists persist for future use.

### ShoppingTrip
- **uuid**: Primary identifier (UUID)
- **store**: Reference to Store (Store, required)
- **time**: When the shopping trip occurred (ZonedDateTime, required)
- **products**: Map of products to quantities purchased (Map<Product, BigDecimal>)
- **owner**: User who owns the trip (required, String)

Shopping trips record actual purchases made by users at specific stores, including quantities and prices paid. Users can only access their own shopping trips.

### Share
- **uuid**: Primary identifier (UUID)
- **name**: Share name (required, String)
- **links**: List of join links for the share (List<JoinLink>)

Shares allow users to collaborate on grocery management with different permission levels. Each share can have multiple join links with different access permissions.

### JoinLink
- **uuid**: Primary identifier (UUID)
- **share**: Reference to Share (Share, required)
- **name**: Link name (required, String)
- **permissions**: Permission level for users joining through this link (Permissions, required)
- **active**: Whether the link is currently active (required, boolean)
- **singleUse**: Whether the link becomes inactive after first use (required, boolean)
- **validTo**: Optional expiration time for the link (Instant)
- **users**: Set of users who have joined through this link (Set<String>)

Join links provide a mechanism for users to gain access to shares with specific permission levels. Links can be configured as single-use or time-limited for security.

### Permissions
Enum defining access levels: **NONE**, **READ**, **WRITE**, **ADMIN**

### ExternalAPI
- **uuid**: Primary identifier (UUID)
- **name**: API name (required, String)
- **endpoints**: List of endpoints for this API (List<Endpoint>)
- **productMappings**: Map of products to remote IDs (Map<Product, String>)
- **storeMappings**: Map of stores to remote IDs (Map<Store, String>)

ExternalAPIs represent external systems that the grocery management system can integrate with for syncing product and store data.

### Endpoint (Base Class)
- **uuid**: Primary identifier (UUID)
- **api**: Reference to ExternalAPI (ExternalAPI, required)
- **name**: Endpoint name (required, String)
- **baseUrl**: Base URL for the endpoint (required, String)
- **pageSize**: Parameter configuration for page size (Parameter)
- **page**: Parameter configuration for page number (Parameter)
- **itemCount**: Parameter configuration for item count (Parameter)
- **basePath**: Base path for the endpoint (required, String)

Base class for all endpoint types.

### PriceEndpoint (extends Endpoint)
- **productHandlingType**: How products are handled in requests (ProductHandlingType)
- **productParameters**: Parameter configuration for product IDs (Parameter)
- **productPath**: Path configuration for product IDs (Path)
- **storeHandlingType**: How stores are handled in requests (StoreHandlingType)
- **storeParameters**: Parameter configuration for store IDs (Parameter)
- **storePath**: Path configuration for store IDs (Path)
- **pricePath**: JSON path to price in response (required, String)
- **timeFormat**: Date format for time fields (required, String)
- **validFromPath**: JSON path to validFrom date in response (required, String)
- **validUntilPath**: JSON path to validUntil date in response (required, String)

PriceEndpoints define how to fetch price data from external APIs.

### ProductEndpoint (extends Endpoint)
- **productIdPath**: JSON path to product ID in response (required, String)
- **productNamePath**: JSON path to product name in response (optional, String)
- **productImagePath**: JSON path to image URL in response (optional, String)
- **productEANPath**: JSON path to EAN in response (optional, String)

ProductEndpoints define how to fetch product data from external APIs.

### StoreEndpoint (extends Endpoint)
- **storeIdPath**: JSON path to store ID in response (required, String)
- **storeNamePath**: JSON path to store name in response (optional, String)
- **storeLogoPath**: JSON path to logo URL in response (optional, String)
- **addressPath**: JSON path to address in response (optional, String)
- **addressPaths**: JSON paths to address components in response (AddressPaths)
- **storeCurrencyPath**: JSON path to currency in response (required, String)

StoreEndpoints define how to fetch store data from external APIs.

### Parameter
- **header**: Header parameter name (optional, String)
- **queryParameter**: Query parameter name (optional, String)

Parameter configuration for API requests (used for header or query parameter placement).

### Path
- **path**: Path parameter name (optional, String)

Path configuration for API requests (used for path parameter placement).

### AddressPaths
- **countryPath**: JSON path to country (required, String)
- **cityPath**: JSON path to city (required, String)
- **zipPath**: JSON path to ZIP code (required, String)
- **streetPath**: JSON path to street (required, String)
- **numberPath**: JSON path to house number (required, String)

JSON path configuration for address components in API responses.

## User Interface Features

### User Dashboard
- **Main landing page** for regular users (eagerly loaded for optimal performance)
- **Navigation** to product groups, shopping lists, and other features
- **Real-time statistics** for product groups and shopping lists
- **Modern Angular Material design** with responsive layout
- **Fast initial load** - Critical component loaded in main bundle

### Shopping Lists Management
- **Complete CRUD operations** for shopping lists (user-scoped)
- **Repeating list support** - Flag to control list persistence after trip planning
- **Automatic cleanup** - Non-repeating lists are deleted after successful trip creation
- **Visual indicators** - Repeat icons and labels throughout the UI for repeating lists
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
- **Repeating flag handling** - Non-repeating lists automatically deleted after trip creation
- **Store assignment** - Assign items to specific stores based on prices
- **Price comparison** - View and compare prices across different stores
- **Visual item management** - Organized component structure with dedicated cards for each trip
- **Planned trip generation** - Create optimized shopping trips
- **Automatic navigation** - Redirect to shopping trips view after successful planning
- **Component Architecture**: Modular design with shared product display components

### Product Listing Component
- **Price display component** for showing product prices across stores
- **Store-specific pricing** - Filter prices by store and date
- **Real-time price fetching** - Integrated with backend price search API
- **Currency formatting** - Proper display of prices with store currency

### Store Listing Component
- **Store display component** for showing store information
- **Real-time store data fetching** - Integrated with backend store API
- **Loading states** - Visual feedback during data retrieval

### Endpoint Management (Master Data)
- **External API management** - Complete CRUD operations for external APIs (`/master-data/external-api`)
- **Endpoint configuration** - Configure price, product, and store endpoints
- **Mapping table management** - Manage product and store mappings between local and remote IDs
- **Endpoint testing interface** - Test endpoints with diff comparison for products and stores
- **Endpoint list component** - View and manage all endpoints for an external API
- **Mapping list component** - Table-based interface for managing ID mappings with add/delete operations
- **Product diff component** - Compare local products with remote API product data
- **Store diff component** - Compare local stores with remote API store data

### Share Administration
- **Share management interface** for creating and editing shares (`/share-admin`)
- **Join link management** with complete CRUD operations for join links
- **Material table interface** for displaying join links with actions
- **Permission-based access control** with ADMIN, WRITE, READ, and NONE levels
- **Copy join link functionality** - One-click copying of full join URLs to clipboard
- **Join link creation form** - Dedicated form for creating new join links with configurable properties
- **Join link editing** - Update existing join link properties and permissions
- **Real-time status indicators** - Visual indicators for active/inactive links and user counts
- **Color-coded permission chips** - Visual distinction between permission levels
- **Global styling integration** - Uses global design system for consistent appearance

### Join Link Usage Interface
- **Join link processing** - Automatic join processing when navigating to `/join/{uuid}`
- **Real-time feedback** - Loading states, success messages, and error handling
- **Automatic share switching** - Sets the joined share as the current share
- **Seamless navigation** - Auto-redirect to home page after successful joining
- **Error handling** - Specific error messages for invalid/expired links
- **Modern clipboard integration** - Uses Angular CDK Clipboard service for reliable copying

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
- **Repeating List Management**: Automatic cleanup of non-repeating shopping lists after trip planning
- **Visual Repeating Indicators**: Repeat icons and labels throughout the UI for easy identification
- **Async Operation Handling**: Proper observable chaining for trip creation and list deletion
- **Automatic Navigation**: Seamless user flow from planning to shopping trips view

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
- `GET /masterdata/price` - Get paginated price listings (with optional store and product filters)
- `GET /masterdata/price?at={timestamp}&products={product_uuids}&stores={store_uuids}` - Get prices by products, stores, and timestamp (returns nested map structure optimized for frontend price comparison)

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

### Shares (`/share`)
- `POST /share` - Create new share (authenticated users only)
- `GET /share` - Get paginated list of user's shares (authenticated users only)
- `POST /share/join/{uuid}` - Join a share using a join link UUID (authenticated users only)

### Current Share (`/share/current`)
- `GET /share/current?share={uuid}` - Get current share information (requires share parameter and authentication)
- `PUT /share/current?share={uuid}` - Update current share name (requires ADMIN permission on share)
- `DELETE /share/current?share={uuid}` - Delete current share and cleanup all associated data (requires ADMIN permission on share)

### Join Links (`/share/current/links`)
- `POST /share/current/links` - Create new join link for current share (requires ADMIN permission on share)
- `GET /share/current/links` - Get all join links for current share (requires READ permission on share)
- `GET /share/current/links/{uuid}` - Get specific join link for current share (requires READ permission on share)
- `PUT /share/current/links/{uuid}` - Update join link properties (requires ADMIN permission on share)
- `DELETE /share/current/links/{uuid}` - Delete join link (requires ADMIN permission on share)

### Planboard (Frontend Feature)
- **Route**: `/planboard` - Interactive shopping planning interface
- **Features**: Shopping list integration, price comparison, store assignment, planned trip generation

### External APIs (`/masterdata/interface`)
- `GET /masterdata/interface/{uuid}` - Get external API by UUID
- `PUT /masterdata/interface/{uuid}` - Update external API
- `POST /masterdata/interface` - Create new external API
- `DELETE /masterdata/interface/{uuid}` - Delete external API
- `GET /masterdata/interface` - Search external APIs (with optional name parameter, paginated)

### Price Endpoints (`/masterdata/interface/{parentUuid}/endpoint/price`)
- `GET /masterdata/interface/{parentUuid}/endpoint/price/{uuid}` - Get price endpoint by UUID
- `PUT /masterdata/interface/{parentUuid}/endpoint/price/{uuid}` - Update price endpoint
- `POST /masterdata/interface/{parentUuid}/endpoint/price` - Create new price endpoint
- `DELETE /masterdata/interface/{parentUuid}/endpoint/price/{uuid}` - Delete price endpoint
- `GET /masterdata/interface/{parentUuid}/endpoint/price` - Search price endpoints (with optional name parameter, paginated)

### Product Endpoints (`/masterdata/interface/{parentUuid}/endpoint/product`)
- `GET /masterdata/interface/{parentUuid}/endpoint/product/{uuid}` - Get product endpoint by UUID
- `PUT /masterdata/interface/{parentUuid}/endpoint/product/{uuid}` - Update product endpoint
- `POST /masterdata/interface/{parentUuid}/endpoint/product` - Create new product endpoint
- `DELETE /masterdata/interface/{parentUuid}/endpoint/product/{uuid}` - Delete product endpoint
- `GET /masterdata/interface/{parentUuid}/endpoint/product` - Search product endpoints (with optional name parameter, paginated)

### Store Endpoints (`/masterdata/interface/{parentUuid}/endpoint/store`)
- `GET /masterdata/interface/{parentUuid}/endpoint/store/{uuid}` - Get store endpoint by UUID
- `PUT /masterdata/interface/{parentUuid}/endpoint/store/{uuid}` - Update store endpoint
- `POST /masterdata/interface/{parentUuid}/endpoint/store` - Create new store endpoint
- `DELETE /masterdata/interface/{parentUuid}/endpoint/store/{uuid}` - Delete store endpoint
- `GET /masterdata/interface/{parentUuid}/endpoint/store` - Search store endpoints (with optional name parameter, paginated)

### Product Mapping Table (`/masterdata/interface/{uuid}/mapping/product`)
- `GET /masterdata/interface/{uuid}/mapping/product` - Get all product mappings as Map<UUID, String>
- `GET /masterdata/interface/{uuid}/mapping/product/in/{remoteId}` - Get local product ID for remote ID
- `PUT /masterdata/interface/{uuid}/mapping/product/in/{remoteId}` - Set mapping from remote ID to local product ID
- `GET /masterdata/interface/{uuid}/mapping/product/out/{localId}` - Get remote ID for local product
- `PUT /masterdata/interface/{uuid}/mapping/product/out/{localId}` - Set mapping from local product to remote ID

### Store Mapping Table (`/masterdata/interface/{uuid}/mapping/store`)
- `GET /masterdata/interface/{uuid}/mapping/store` - Get all store mappings as Map<UUID, String>
- `GET /masterdata/interface/{uuid}/mapping/store/in/{remoteId}` - Get local store ID for remote ID
- `PUT /masterdata/interface/{uuid}/mapping/store/in/{remoteId}` - Set mapping from remote ID to local store ID
- `GET /masterdata/interface/{uuid}/mapping/store/out/{localId}` - Get remote ID for local store
- `PUT /masterdata/interface/{uuid}/mapping/store/out/{localId}` - Set mapping from local store to remote ID

## Development Setup

### Prerequisites
- Java 17
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
- **OAuth2 Resource Server configuration** for JWT token validation
- **ShareFilter** for share-based authentication and authorization (placed before AuthorizationFilter)
- **Authority-based access control** with mixed JWT scopes and ShareFilter-granted authorities:
  - **JWT**
    - `SCOPE_{masterdata}` - required for master data operations
  - **ShareFilter**
    - `USER_SCOPED`
    - `SHARE_SCOPED`
    - `READ_SCOPED`
    - `WRITE_SCOPED`
    - `ADMIN_SCOPED`
    - **ShareFilter authority granting**:
      - When share parameter present:
        - ADMIN permission: grants ADMIN_SCOPED + WRITE_SCOPED + READ_SCOPED + SHARE_SCOPED
        - WRITE permission: grants WRITE_SCOPED + READ_SCOPED + SHARE_SCOPED
        - READ permission: grants READ_SCOPED + SHARE_SCOPED
        - NONE permission: grants no authorities (AccessDenied)
      - When no share parameter (regular user context): grants ADMIN_SCOPED + WRITE_SCOPED + READ_SCOPED + USER_SCOPED
- **Permission-based access control** with four levels: NONE, READ, WRITE, ADMIN
- **OwnerUtils** for consistent owner identification across user and share contexts
- **CORS configuration** for frontend integration
- **Share-based security model**:
  - Users authenticate with JWT tokens
  - Share parameter in requests switches context to share-based authentication
  - ShareFilter wraps authentication with ShareAuthenticationToken containing SharePrincipal
  - Permission levels are determined by user's highest permission across all join links
  - Owner format for share-scoped data: `"share: {uuid}"`
  - Owner format for user-scoped data: `"sub: {subject}"`
- **Endpoint security rules**:
  - Master data GET endpoints: authenticated users only
  - Master data write endpoints: require masterdata scope authority
  - Share endpoints: require USER_SCOPED authority
  - Current share endpoints: require SHARE_SCOPED (read) or ADMIN_SCOPED + SHARE_SCOPED (write/admin)
  - Join link management: require ADMIN_SCOPED + SHARE_SCOPED
  - Default fallback: require WRITE_SCOPED authority

## Database Migration
- Liquibase for database schema management
- Migration files located in `src/main/resources/db/migration/`

## Testing
- **Backend**: JUnit 5, Spring Boot Test, MockMvc for controller testing
- **Frontend**: Vitest, Angular Testing Utilities
- **Comprehensive test coverage** for all CRUD operations
- **Security testing** with JWT authentication
- **Data integrity testing** with foreign key constraints
- **ShoppingTripController tests** with full CRUD coverage, ownership validation, and add-to-trip functionality
- **ShoppingListController tests** with complete CRUD operations, user-scoped access control, and repeating list management
- **ProductGroupController tests** with full CRUD coverage and user ownership validation
- **PriceListController tests** with comprehensive price search functionality including:
  - Basic price CRUD operations with authorization testing
  - Advanced price search with date, store, and product parameters
  - Boundary condition testing for validFrom/validTo dates
  - Multiple store and product search capabilities
  - Edge case handling for non-existent entities and out-of-range dates
- **ShareController tests** with complete share and join link management:
  - Share creation with automatic owner link generation
  - Join link functionality with single-use and expiration handling
  - User-scoped share listing with proper permission levels
  - Comprehensive pagination and filtering support
- **CurrentShareController tests** with ShareFilter integration:
  - Current share retrieval with different permission levels (ADMIN, WRITE, READ)
  - Permission-based update operations (ADMIN only)
  - Share deletion with automatic cleanup of associated data
  - ShareFilter authentication and authorization testing
  - Owner-based data cleanup verification
  - Comprehensive error handling for unauthorized access
- **ExternalAPIController tests** with complete CRUD operations and search functionality:
  - Basic CRUD operations with authorization testing
  - Search functionality with name filtering and pagination
  - Transactional test pattern for data isolation
- **PriceEndpointController tests** with full CRUD coverage and parent-child relationship validation:
  - Parent-child relationship with ExternalAPI
  - Complete CRUD operations with authorization testing
  - Search functionality with name filtering and pagination
  - Transactional test pattern for data isolation
- **ProductEndpointController tests** with full CRUD coverage and parent-child relationship validation:
  - Parent-child relationship with ExternalAPI
  - Complete CRUD operations with authorization testing
  - Search functionality with name filtering and pagination
  - Transactional test pattern for data isolation
- **StoreEndpointController tests** with full CRUD coverage and parent-child relationship validation:
  - Parent-child relationship with ExternalAPI
  - Complete CRUD operations with authorization testing
  - Search functionality with name filtering and pagination
  - Transactional test pattern for data isolation
- **ProductMappingTableController tests** with bidirectional mapping operations:
  - Inbound mapping (remote ID to local product ID)
  - Outbound mapping (local product ID to remote ID)
  - Authorization testing for mapping operations
  - Error handling for non-existent entities
- **StoreMappingTableController tests** with bidirectional mapping operations:
  - Inbound mapping (remote ID to local store ID)
  - Outbound mapping (local store ID to remote ID)
  - Authorization testing for mapping operations
  - Error handling for non-existent entities
- **Enhanced test architecture** with canary pattern for data isolation and comprehensive validation

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
- **Share Functionality**: Comprehensive sharing system with permission-based access control
  - ShareFilter provides seamless switching between user and share contexts
  - Join links support single-use and time-limited access for security
  - CleanupService ensures proper data cleanup when shares are deleted
  - Share-scoped data uses `"share: {uuid}"` owner format for isolation
  - Permission levels (NONE, READ, WRITE, ADMIN) provide granular access control
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
- **Repeating Flag Implementation**: Frontend logic handles automatic deletion of non-repeating lists after trip planning
- **Observable Chaining**: Proper async handling ensures operations complete before navigation
- **Global CSS Optimization**: Table column styling and repeating indicators moved to global styles.scss
- **Material Design Integration**: Leveraging Angular Material's built-in styling and CSS variables
- **Test-Driven Bug Fixes**: Comprehensive testing identified and validated fixes for boundary condition issues in price search functionality
- **Enhanced Price Search API**: Improved repository methods with inclusive boundary conditions for accurate date-based price queries
- **Share-First Testing**: CurrentShareController tests verify ShareFilter integration and permission-based access control
- **Data Cleanup Verification**: Tests ensure proper cleanup of share-associated data when shares are deleted
- **External API Integration**: New controllers for managing external API integrations with comprehensive test coverage
- **Endpoint Configuration**: Support for configuring price, product, and store endpoints with flexible parameter and path configuration
- **Mapping Table Functionality**: Bidirectional mapping between local IDs and remote IDs for products and stores
- **Transactional Test Pattern**: Consistent use of @Transactional annotation with deleteAll() in @BeforeEach for test data isolation
- **Persistence Context Management**: clearAutomatically = true on @Modifying queries to ensure proper entity visibility in tests
