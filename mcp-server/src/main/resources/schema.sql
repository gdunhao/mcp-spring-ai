-- ============================================================
-- Demo Database Schema
-- ============================================================
-- This schema supports the DatabaseQueryTool MCP demo.
-- It models a simple company with employees, departments,
-- products, and orders — a realistic scenario for natural
-- language to SQL demonstrations.
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    budget      DECIMAL(12, 2),
    manager_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS employees (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150),
    department  VARCHAR(100),
    salary      DECIMAL(10, 2),
    hire_date   DATE
);

CREATE TABLE IF NOT EXISTS products (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(100),
    price           DECIMAL(10, 2),
    stock_quantity  INT
);

CREATE TABLE IF NOT EXISTS orders (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    product_id      INT,
    customer_name   VARCHAR(100),
    quantity        INT,
    order_date      DATE,
    status          VARCHAR(50),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

