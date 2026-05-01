-- Music Store Database Initialization Script

-- Create databases for each service
SELECT 'Creating database: musicstore_product' as info;
CREATE DATABASE musicstore_product;

SELECT 'Creating database: musicstore_order' as info;
CREATE DATABASE musicstore_order;

SELECT 'Creating database: musicstore_payment' as info;
CREATE DATABASE musicstore_payment;

SELECT 'Creating database: musicstore_user' as info;
CREATE DATABASE musicstore_user;

SELECT 'Creating database: musicstore_inventory' as info;
CREATE DATABASE musicstore_inventory;