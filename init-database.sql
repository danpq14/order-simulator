-- MySQL initialization script for Docker
-- Note: The database 'order_simulator' is already created by MYSQL_DATABASE env var

SELECT 'Starting Order Simulator database initialization...' AS message;

USE order_simulator;

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL(19,8) NOT NULL,
    price DECIMAL(19,8) NOT NULL,
    status VARCHAR(20) NOT NULL,
    side VARCHAR(10) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_orders_symbol (symbol)
);

-- Create events table
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    INDEX idx_event_order_id (order_id)
);

-- Insert some sample data for testing
INSERT INTO orders (symbol, quantity, price, status, side) VALUES
('FPT', 100, 50000, 'PENDING', 'BUY'),
('VIC', 10, 3000, 'PENDING', 'SELL'),
('VCB', 999, 10000, 'EXECUTED', 'BUY');

-- Show tables and their structure
SELECT 'Database initialization completed successfully!' AS message;
SHOW TABLES;
DESCRIBE orders;
DESCRIBE events;

SELECT 'Sample data inserted. Ready for Order Simulator!' AS message;
