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
    public static final UUID STORE_3_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final String STORE_3_CREATE_JSON = """
        {
          "name": "Store 3",
          "address": {
            "country": "DE",
            "city": "Munich"
          },
          "currency": "EUR"
        }""";
    public static final String STORE_3_JSON = """
        {
          "name": "Store 3",
          "address": {
            "country": "DE",
            "city": "Munich"
          },
          "currency": "EUR"
        }""";
    
    public static final UUID STORE_4_UUID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final String STORE_4_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Store 4",
          "address": {
            "country": "DE",
            "city": "Berlin"
          },
          "currency": "USD"
        }""", STORE_4_UUID);

    public static final UUID BAD_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public static final String STORE_SEARCH_RESULT_JSON = String.format("""
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
        }""", STORE_1_JSON, STORE_2_JSON, STORE_3_JSON, STORE_4_JSON);

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
    
    public static final UUID PRODUCT_GROUP_TEST_1_UUID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    public static final UUID PRODUCT_GROUP_TEST_2_UUID = UUID.fromString("10000000-0000-0000-0000-000000000003");

    public static final String PRODUCT_3_JSON = """
        {
          "name": "Product 3",
          "EAN": "654321"
        }""";

    public static final String PRODUCT_3_CREATE_JSON = PRODUCT_3_JSON;

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
          "products": {
            "%s": 1
          }
        }""", PRODUCT_GROUP_1_UUID, PRODUCT_GROUP_TEST_1_UUID);
    public static final String PRODUCT_GROUP_1_UPDATE_JSON = String.format("""
        {
          "name": "Group 1b",
          "products": {
            "%s": 1,
            "%s": 2
          }
        }""", PRODUCT_GROUP_TEST_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    public static final String PRODUCT_GROUP_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "name": "Group 1b",
          "products": {
            "%s": 1,
            "%s": 2
          }
        }""", PRODUCT_GROUP_1_UUID, PRODUCT_GROUP_TEST_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    public static final UUID PRODUCT_GROUP_2_UUID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    public static final String PRODUCT_GROUP_2_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Group 2",
          "products": {}
        }""", PRODUCT_GROUP_2_UUID);
    public static final String PRODUCT_GROUP_3_CREATE_JSON = String.format("""
        {
          "name": "Group 3",
          "products": {
            "%s": 1
          }
        }""", PRODUCT_GROUP_TEST_1_UUID);
    public static final String PRODUCT_GROUP_3_JSON = String.format("""
        {
          "name": "Group 3",
          "products": {
            "%s": 1
          }
        }""", PRODUCT_GROUP_TEST_1_UUID);
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

    public static final UUID PRICE_1_UUID = UUID.fromString("40000000-0000-0000-0000-000000000000");
    public static final String PRICE_1_JSON = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "product": "%s",
          "validFrom": "2024-01-01T01:00:00+01:00",
          "validTo": "2025-01-01T00:59:59+01:00",
          "price": 11
        }""", PRICE_1_UUID, STORE_3_UUID, PRODUCT_GROUP_TEST_1_UUID);
    public static final String PRICE_1_UPDATE_JSON = """
        {
          "validFrom": "2024-02-01T00:00:00Z",
          "validTo": "2024-11-30T23:59:59Z",
          "price": 12.99
        }""";
    public static final String PRICE_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "product": "%s",
          "validFrom": "2024-02-01T00:00:00Z",
          "validTo": "2024-11-30T23:59:59Z",
          "price": 12.99
        }""", PRICE_1_UUID, STORE_3_UUID, PRODUCT_GROUP_TEST_1_UUID);
    
    public static final UUID PRICE_2_UUID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    public static final String PRICE_2_JSON = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "product": "%s",
          "validFrom": "2024-01-01T01:00:00+01:00",
          "validTo": "2025-01-01T00:59:59+01:00",
          "price": 5
        }""", PRICE_2_UUID, STORE_4_UUID, PRODUCT_GROUP_TEST_2_UUID);

    public static final String PRICE_3_JSON = String.format("""
        {
          "store": "%s",
          "product": "%s",
          "validFrom": "2024-03-01T00:00:00Z",
          "validTo": "2024-12-31T23:59:59Z",
          "price": 8.99
        }""", STORE_1_UUID, PRODUCT_GROUP_TEST_2_UUID);

    public static final String PRICE_3_CREATE_JSON = PRICE_3_JSON;

    public static final String PRICE_SEARCH_RESULT_JSON = String.format("""
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
        }""", PRICE_1_JSON, PRICE_2_JSON);

    public static final UUID SHOPPING_LIST_1_UUID = UUID.fromString("30000000-0000-0000-0000-000000000000");
    public static final String SHOPPING_LIST_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "List 1",
          "repeating": false,
          "products": {
            "%s": 2
          },
          "productGroups": {}
        }""", SHOPPING_LIST_1_UUID, PRODUCT_GROUP_TEST_1_UUID);
    public static final String SHOPPING_LIST_1_UPDATE_JSON = String.format("""
        {
          "name": "List 1b",
          "repeating": true,
          "products": {
            "%s": 2,
            "%s": 1
          },
          "productGroups": {}
        }""", PRODUCT_GROUP_TEST_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    public static final String SHOPPING_LIST_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "name": "List 1b",
          "repeating": true,
          "products": {
            "%s": 2,
            "%s": 1
          },
          "productGroups": {}
        }""", SHOPPING_LIST_1_UUID, PRODUCT_GROUP_TEST_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    public static final UUID SHOPPING_LIST_2_UUID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    public static final String SHOPPING_LIST_2_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "List 2",
          "repeating": true,
          "products": {},
          "productGroups": {}
        }""", SHOPPING_LIST_2_UUID);
    public static final String SHOPPING_LIST_3_CREATE_JSON = String.format("""
        {
          "name": "List 3",
          "repeating": false,
          "products": {
            "%s": 3
          },
          "productGroups": {}
        }""", PRODUCT_GROUP_TEST_1_UUID);
    public static final String SHOPPING_LIST_3_JSON = String.format("""
        {
          "name": "List 3",
          "repeating": false,
          "products": {
            "%s": 3
          },
          "productGroups": {}
        }""", PRODUCT_GROUP_TEST_1_UUID);
    public static final String SHOPPING_LIST_SEARCH_RESULT_JSON = String.format("""
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
        }""", SHOPPING_LIST_1_JSON);
    public static final String SHOPPING_LIST_SEARCH_RESULT_USER2_JSON = String.format("""
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
        }""", SHOPPING_LIST_2_JSON);

    public static final UUID SHOPPING_TRIP_1_UUID = UUID.fromString("50000000-0000-0000-0000-000000000000");
    public static final String SHOPPING_TRIP_1_JSON = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "time": "2024-01-15T10:00:00+01:00",
          "products": {
            "%s": 2
          }
        }""", SHOPPING_TRIP_1_UUID, STORE_3_UUID, PRODUCT_GROUP_TEST_1_UUID);
    public static final String SHOPPING_TRIP_1_UPDATE_JSON = String.format("""
        {
          "name": "Trip 1b",
          "store": "%s",
          "time": "2024-01-15T13:00:00Z",
          "products": {
            "%s": 1,
            "%s": 3
          }
        }""", STORE_2_UUID, PRODUCT_GROUP_TEST_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    public static final String SHOPPING_TRIP_1_JSON2 = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "time": "2024-01-15T13:00:00Z",
          "products": {
            "%s": 1,
            "%s": 3
          }
        }""", SHOPPING_TRIP_1_UUID, STORE_2_UUID, PRODUCT_GROUP_TEST_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    
    public static final UUID SHOPPING_TRIP_2_UUID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    public static final String SHOPPING_TRIP_2_JSON = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "time": "2024-01-20T15:30:00+01:00",
          "products": {}
        }""", SHOPPING_TRIP_2_UUID, STORE_3_UUID);
    
    public static final String SHOPPING_TRIP_3_CREATE_JSON = String.format("""
        {
          "name": "Trip 3",
          "store": "%s",
          "time": "2024-01-25T08:00:00Z",
          "products": {
            "%s": 1
          }
        }""", STORE_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    
    public static final String SHOPPING_TRIP_3_JSON = String.format("""
        {
          "store": "%s",
          "time": "2024-01-25T08:00:00Z",
          "products": {
            "%s": 1
          }
        }""", STORE_1_UUID, PRODUCT_GROUP_TEST_2_UUID);
    
    public static final String SHOPPING_TRIP_SEARCH_RESULT_JSON = String.format("""
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
        }""", SHOPPING_TRIP_1_JSON);
    
    public static final String SHOPPING_TRIP_SEARCH_RESULT_USER2_JSON = String.format("""
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
        }""", SHOPPING_TRIP_2_JSON);

    public static final String SHOPPING_TRIP_1_ADD_MULTIPLE_JSON = String.format("""
        {
          "%s": 3,
          "%s": 2
        }""", PRODUCT_GROUP_TEST_2_UUID, PRODUCT_GROUP_TEST_1_UUID);

    public static final String SHOPPING_TRIP_1_ADD_SINGLE_JSON = String.format("""
        {
          "%s": 1.5
        }""", PRODUCT_GROUP_TEST_2_UUID);

    public static final String SHOPPING_TRIP_1_ADD_EXISTING_JSON = String.format("""
        {
          "%s": 1
        }""", PRODUCT_GROUP_TEST_1_UUID);

    public static final String SHOPPING_TRIP_1_ADD_EMPTY_JSON = "{}";

    public static final String SHOPPING_TRIP_1_ADD_ZERO_JSON = String.format("""
        {
          "%s": 0
        }""", PRODUCT_GROUP_TEST_2_UUID);

    public static final String SHOPPING_TRIP_1_ADD_NEGATIVE_JSON = String.format("""
        {
          "%s": -1
        }""", PRODUCT_GROUP_TEST_2_UUID);
}
