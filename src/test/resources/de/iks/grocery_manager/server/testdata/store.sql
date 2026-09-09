TRUNCATE TABLE store CASCADE;

INSERT INTO store(uuid, name, currency, country, city, zip, street_and_number)
VALUES
    (UUID '00000000-0000-0000-0000-000000000000', 'Store 1', 'EUR', 'DE', 'Düsseldorf', NULL, NULL),
    (UUID '00000000-0000-0000-0000-000000000001', 'Store 2', 'USD', 'DE', 'Hilden', NULL, NULL),
    (UUID '00000000-0000-0000-0000-000000000002', 'Store 3', 'EUR', 'DE', 'Munich', NULL, NULL),
    (UUID '00000000-0000-0000-0000-000000000003', 'Store 4', 'USD', 'DE', 'Berlin', NULL, NULL);
