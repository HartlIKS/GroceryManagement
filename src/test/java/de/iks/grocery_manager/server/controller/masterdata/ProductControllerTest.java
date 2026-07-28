package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ProductController.class)
@Sql("/testdata.sql")
@WithTestUser
class ProductControllerTest {
    @Inject
    ProductRepository productRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(ProductController.class)
    @WithTestUser
    class GetProduct {
        @Test
        void shouldReturnProductWhenFound() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuid(Testdata.PRODUCT_1_UUID))
                .body("name", is("Product 1"))
                .when()
                .get("{uuid}", Testdata.PRODUCT_1_UUID);
        }

        @Test
        void shouldReturn404WhenProductNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductController.class)
    @WithAdminUser
    class UpdateProduct {
        private static final String PRODUCT_1_UPDATE_JSON = """
        {
          "name": "Product 1b",
          "EAN": "123456"
        }""";

        @Test
        void shouldUpdateProductWhenAuthorizedAndFound() {
            long initialCount = productRepository.count();
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuid(Testdata.PRODUCT_1_UUID))
                .body("name", is("Product 1b"))
                .body("EAN", is("123456"))
                .given()
                .body(PRODUCT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", Testdata.PRODUCT_1_UUID);

            // Verify update was applied and other product unaffected
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_1_UUID)
                           .isPresent());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
            assertEquals(initialCount, productRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProduct() {
            long initialCount = productRepository.count();

            expect()
                .statusCode(404)
                .given()
                .body(PRODUCT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", Testdata.BAD_UUID);

            // Verify no changes to existing products
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_1_UUID)
                           .isPresent());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
            assertEquals(initialCount, productRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingProductWithoutAuthorization() {
            long initialCount = productRepository.count();

            expect()
                .statusCode(403)
                .given()
                .body(PRODUCT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", Testdata.PRODUCT_1_UUID);

            // Verify no changes when unauthorized
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_1_UUID)
                           .isPresent());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
            assertEquals(initialCount, productRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductController.class)
    class CreateProduct {
        private static final String PRODUCT_3_CREATE_JSON = """
        {
          "name": "Product 3",
          "EAN": "654321"
        }""";

        @Test
        @WithAdminUser
        void shouldCreateProductWhenAuthorized() {
            long initialCount = productRepository.count();

            expect()
                .statusCode(201)
                .header(
                    "location", matchesRegex(
                        String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern())
                    )
                )
                .contentType(ContentType.JSON)
                .body("uuid", matchesRegex(Testdata.UUID_PATTERN))
                .body("name", is("Product 3"))
                .body("EAN", is("654321"))
                .given()
                .body(PRODUCT_3_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post();

            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, productRepository.count());
            // Verify existing products unaffected
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_1_UUID)
                           .isPresent());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingProductWithoutAuthorization() {
            long initialCount = productRepository.count();

            expect()
                .statusCode(403)
                .given()
                .body(PRODUCT_3_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post();

            // Verify no changes when unauthorized
            assertEquals(initialCount, productRepository.count());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_1_UUID)
                           .isPresent());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductController.class)
    class DeleteProduct {
        @Test
        @WithAdminUser
        void shouldDeleteProductWhenAuthorized() {
            long initialCount = productRepository.count();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", Testdata.PRODUCT_1_UUID);

            // Verify deletion
            assertFalse(productRepository
                            .findByIdOptional(Testdata.PRODUCT_1_UUID)
                            .isPresent());
            assertEquals(initialCount - 1, productRepository.count());
            // Verify other product unaffected
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingProductWithoutAuthorization() {
            long initialCount = productRepository.count();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", Testdata.PRODUCT_1_UUID);

            // Verify no changes when unauthorized
            assertEquals(initialCount, productRepository.count());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_1_UUID)
                           .isPresent());
            assertTrue(productRepository
                           .findByIdOptional(Testdata.PRODUCT_2_UUID)
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductController.class)
    @WithTestUser
    class SearchProducts {
        @Test
        void shouldReturnAllProductsWhenSearching() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(4))
                .body("page.totalPages", is(1))
                .body("content[0].uuid", isUuid(Testdata.PRODUCT_1_UUID))
                .body("content[0].name", is("Product 1"))
                .body("content[1].uuid", isUuid(Testdata.PRODUCT_2_UUID))
                .body("content[1].name", is("Product 2"))
                .body("content[2].uuid", isUuid(Testdata.PRODUCT_3_UUID))
                .body("content[2].name", is("Product 3"))
                .body("content[3].uuid", isUuid(Testdata.PRODUCT_4_UUID))
                .body("content[3].name", is("Product 4"))
                .when()
                .get();
        }
    }
}