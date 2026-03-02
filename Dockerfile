FROM --platform=$BUILDPLATFORM node:24-alpine AS frontend-build
WORKDIR /opt/build
COPY frontend/package.json .
COPY frontend/package-lock.json .
RUN npm install
COPY frontend/tsconfig.* .
COPY frontend/angular.json .
COPY frontend/public/ public
COPY frontend/src src
RUN npm run build

FROM --platform=$BUILDPLATFORM maven:3-eclipse-temurin-17-alpine AS backend-build
WORKDIR /opt/build
COPY pom.xml .
RUN mvn dependency:go-offline dependency:resolve-plugins
COPY --parents src/main/java/de/iks/grocery_manager/server/GroceryManagementApplication.java .
COPY --parents src/test/java/de/iks/grocery_manager/server/GroceryManagementApplicationTests.java .
RUN mvn test -fn
COPY --parents src/ .
RUN mvn package

FROM eclipse-temurin:17-jre AS run
WORKDIR /opt/grocery-manager
COPY --from=backend-build /opt/build/target/grocery_manager.jar .
COPY --from=frontend-build /opt/build/dist/frontend/browser/ public
CMD ["/opt/java/openjdk/bin/java", "-jar", "grocery_manager.jar"]