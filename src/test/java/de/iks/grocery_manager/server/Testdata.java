package de.iks.grocery_manager.server;

import java.util.UUID;

public class Testdata {
    public static final String SCRIPT = "classpath:/testdata.sql";
    public static final UUID STORE_1_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String STORE_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Store 1",
          "address": {
            "country": "DE",
            "city": "Düsseldorf"
          },
          "currency": "EUR"
        }""", STORE_1_UUID);
    public static final String STORE_1_UPDATE_JSON = """
        {
          "name": "Store 1b",
          "address": {
            "city": "Hilden"
          }
        }""";
    public static final String STORE_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "name": "Store 1b",
          "address": {
            "country": "DE",
            "city": "Hilden"
          },
          "currency": "EUR"
        }""", STORE_1_UUID);
    public static final UUID STORE_2_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String STORE_2_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Store 2",
          "address": {
            "country": "DE",
            "city": "Hilden"
          },
          "currency": "USD"
        }""", STORE_2_UUID);
    public static final String STORE_3_CREATE_JSON = """
        {
          "name": "Store 3",
          "address": {
            "country": "DE",
            "city": "Neuss"
          },
          "currency": "EUR"
        }""";
    public static final String STORE_3_JSON = """
        {
          "name": "Store 3",
          "address": {
            "country": "DE",
            "city": "Neuss"
          },
          "currency": "EUR"
        }""";

    public static final UUID BAD_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public static final String STORE_SEARCH_RESULT_JSON = String.format("""
        {
          "page": {
            "number": 0,
            "size": 10,
            "totalElements": 2,
            "totalPages": 1
          },
          "content": [
            %s,
            %s
          ]
        }""", STORE_1_JSON, STORE_2_JSON);

    public static final UUID PRODUCT_1_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    public static final String PRODUCT_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Product 1"
        }""", PRODUCT_1_UUID);
    public static final String PRODUCT_1_UPDATE_JSON = """
        {
          "name": "Product 1b",
          "EAN": "123456"
        }""";
    public static final String PRODUCT_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "name": "Product 1b",
          "EAN": "123456"
        }""", PRODUCT_1_UUID);
    public static final UUID PRODUCT_2_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    public static final String PRODUCT_2_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Product 2"
        }""", PRODUCT_2_UUID);
    public static final String PRODUCT_3_CREATE_JSON = """
        {
          "name": "Product 3",
          "EAN": "654321"
        }""";
    
    public static final UUID PRODUCT_GROUP_TEST_1_UUID = UUID.fromString("30000000-0000-0000-0000-000000000000");
    public static final UUID PRODUCT_GROUP_TEST_2_UUID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    public static final String PRODUCT_3_JSON = """
        {
          "name": "Product 3",
          "EAN": "654321"
        }""";

    public static final String PRODUCT_GROUP_TEST_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Product Group Test 1"
        }""", PRODUCT_GROUP_TEST_1_UUID);
    
    public static final String PRODUCT_GROUP_TEST_2_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Product Group Test 2"
        }""", PRODUCT_GROUP_TEST_2_UUID);

    public static final String PRODUCT_SEARCH_RESULT_JSON = String.format("""
        {
          "page": {
            "number": 0,
            "size": 10,
            "totalElements": 4,
            "totalPages": 1
          },
          "content": [
            %s,
            %s,
            %s,
            %s
          ]
        }""", PRODUCT_1_JSON, PRODUCT_2_JSON, PRODUCT_GROUP_TEST_1_JSON, PRODUCT_GROUP_TEST_2_JSON);

    public static final UUID PRODUCT_GROUP_1_UUID = UUID.fromString("20000000-0000-0000-0000-000000000000");
    public static final String PRODUCT_GROUP_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Group 1",
          "products": ["30000000-0000-0000-0000-000000000000"]
        }""", PRODUCT_GROUP_1_UUID);
    public static final String PRODUCT_GROUP_1_UPDATE_JSON = """
        {
          "name": "Group 1b"
        }""";
    public static final String PRODUCT_GROUP_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "name": "Group 1b",
          "products": ["30000000-0000-0000-0000-000000000000"]
        }""", PRODUCT_GROUP_1_UUID);
    public static final UUID PRODUCT_GROUP_2_UUID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    public static final String PRODUCT_GROUP_2_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Group 2",
          "products": []
        }""", PRODUCT_GROUP_2_UUID);
    public static final String PRODUCT_GROUP_3_CREATE_JSON = """
        {
          "name": "Group 3"
        }""";
    public static final String PRODUCT_GROUP_3_JSON = """
        {
          "name": "Group 3",
          "products": []
        }""";
    public static final String PRODUCT_GROUP_SEARCH_RESULT_JSON = String.format("""
        {
          "page": {
            "number": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
          },
          "content": [
            %s
          ]
        }""", PRODUCT_GROUP_1_JSON);

    public static final String PRODUCT_GROUP_SEARCH_RESULT_USER2_JSON = String.format("""
        {
          "page": {
            "number": 0,
            "size": 10,
            "totalElements": 1,
            "totalPages": 1
          },
          "content": [
            %s
          ]
        }""", PRODUCT_GROUP_2_JSON);
}
