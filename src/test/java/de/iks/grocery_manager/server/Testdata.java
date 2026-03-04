package de.iks.grocery_manager.server;

import java.util.UUID;

public class Testdata {
    public static final String SCRIPT = "classpath:/testdata.sql";

    public static final UUID BAD_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public static final UUID STORE_1_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID STORE_2_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID STORE_3_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID STORE_4_UUID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    public static final UUID PRODUCT_1_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    public static final UUID PRODUCT_2_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    public static final UUID PRODUCT_3_UUID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    public static final UUID PRODUCT_4_UUID = UUID.fromString("10000000-0000-0000-0000-000000000003");

    public static final UUID PRICE_1_UUID = UUID.fromString("40000000-0000-0000-0000-000000000000");
    public static final UUID PRICE_2_UUID = UUID.fromString("40000000-0000-0000-0000-000000000001");
}
