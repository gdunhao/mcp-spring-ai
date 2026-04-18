-- ============================================================
-- Demo Sample Data
-- ============================================================

-- Departments
INSERT INTO departments (name, budget, manager_name) VALUES ('Engineering', 2500000.00, 'Alice Chen');
INSERT INTO departments (name, budget, manager_name) VALUES ('Marketing', 1200000.00, 'Bob Martinez');
INSERT INTO departments (name, budget, manager_name) VALUES ('Sales', 1800000.00, 'Carol Williams');
INSERT INTO departments (name, budget, manager_name) VALUES ('Human Resources', 800000.00, 'David Kim');
INSERT INTO departments (name, budget, manager_name) VALUES ('Finance', 950000.00, 'Eve Johnson');
INSERT INTO departments (name, budget, manager_name) VALUES ('Research', 3000000.00, 'Frank Zhang');

-- Employees
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Alice Chen', 'alice.chen@example.com', 'Engineering', 145000.00, '2019-03-15');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Bob Martinez', 'bob.martinez@example.com', 'Marketing', 120000.00, '2018-07-22');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Carol Williams', 'carol.williams@example.com', 'Sales', 130000.00, '2017-11-01');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('David Kim', 'david.kim@example.com', 'Human Resources', 110000.00, '2020-01-10');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Eve Johnson', 'eve.johnson@example.com', 'Finance', 125000.00, '2019-09-05');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Frank Zhang', 'frank.zhang@example.com', 'Research', 155000.00, '2016-06-20');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Grace Lee', 'grace.lee@example.com', 'Engineering', 135000.00, '2020-04-12');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Henry Brown', 'henry.brown@example.com', 'Engineering', 128000.00, '2021-02-28');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Iris Patel', 'iris.patel@example.com', 'Marketing', 95000.00, '2022-08-15');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Jack Wilson', 'jack.wilson@example.com', 'Sales', 105000.00, '2021-11-30');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Karen Davis', 'karen.davis@example.com', 'Engineering', 140000.00, '2018-05-14');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Leo Garcia', 'leo.garcia@example.com', 'Research', 142000.00, '2019-12-01');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Maya Thompson', 'maya.thompson@example.com', 'Finance', 98000.00, '2023-01-09');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Nathan Scott', 'nathan.scott@example.com', 'Sales', 88000.00, '2023-06-20');
INSERT INTO employees (name, email, department, salary, hire_date) VALUES ('Olivia Robinson', 'olivia.robinson@example.com', 'Human Resources', 92000.00, '2022-03-11');

-- Products
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Laptop Pro 15', 'Electronics', 1299.99, 150);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Wireless Mouse', 'Electronics', 29.99, 500);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Mechanical Keyboard', 'Electronics', 89.99, 300);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('4K Monitor 27"', 'Electronics', 449.99, 80);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('USB-C Hub', 'Accessories', 54.99, 420);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Standing Desk', 'Furniture', 599.99, 45);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Ergonomic Chair', 'Furniture', 399.99, 60);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Noise-Canceling Headphones', 'Electronics', 249.99, 200);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Webcam HD', 'Electronics', 79.99, 350);
INSERT INTO products (name, category, price, stock_quantity) VALUES ('Desk Lamp LED', 'Accessories', 34.99, 180);

-- Orders
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (1, 'TechCorp Inc.', 25, '2024-01-15', 'DELIVERED');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (2, 'StartupXYZ', 100, '2024-01-20', 'DELIVERED');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (3, 'BigBank Ltd.', 50, '2024-02-01', 'DELIVERED');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (1, 'University Labs', 10, '2024-02-10', 'SHIPPED');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (6, 'DesignStudio', 15, '2024-02-15', 'SHIPPED');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (8, 'MusicProd Co.', 30, '2024-02-20', 'PROCESSING');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (4, 'TechCorp Inc.', 20, '2024-03-01', 'PROCESSING');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (7, 'HealthClinic', 8, '2024-03-05', 'PENDING');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (5, 'StartupXYZ', 200, '2024-03-10', 'PENDING');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (9, 'RemoteTeam LLC', 45, '2024-03-12', 'PENDING');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (10, 'HomeOffice Pro', 60, '2024-03-15', 'CANCELLED');
INSERT INTO orders (product_id, customer_name, quantity, order_date, status) VALUES (2, 'BigBank Ltd.', 75, '2024-03-18', 'DELIVERED');

