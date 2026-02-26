FROM node:24-alpine AS frontend-build
WORKDIR /opt/build
ADD frontend/package.json .
ADD frontend/package-lock.json .
RUN npm install
ADD frontend/tsconfig.* .
ADD frontend/angular.json .
ADD frontend/public/ public
ADD frontend/src src
RUN npm run build

FROM maven:3-eclipse-temurin-21-alpine AS backend-build
WORKDIR /opt/build
ADD pom.xml .
RUN mvn dependency:go-offline dependency:resolve-plugins
ADD src/ src
RUN mvn package

FROM eclipse-temurin:21-alpine AS run
WORKDIR /opt/grocery-manager
COPY --from=backend-build /opt/build/target/grocery_manager.jar .
COPY --from=frontend-build /opt/build/dist/frontend/browser/ public
CMD ["/opt/java/openjdk/bin/java", "-jar", "grocery_manager.jar"]