# Table & Thyme — Java Food Ordering System

A Java 21 desktop prototype for restaurant ordering, built with Swing, JDBC, MySQL 8 and Maven. The Java source is split into model, view, controller, DAO, service and utility packages.

## What is included

- Customer registration and sign-in; passwords are stored as BCrypt hashes.
- Searchable menu with all six categories, price, descriptions and locally painted food illustrations.
- Persistent shopping cart with quantity controls, checkout and receipt/order IDs.
- Cash on delivery and clearly marked **demo-only UPI**; no real payment is taken.
- Customer order history and status tracking.
- Admin dashboard, food-item management, order-status updates and customer account management.
- MySQL schema and sample menu data in `database/`.

The application requires a running MySQL server. It does not silently switch to sample or in-memory data when the database is unavailable.

## Requirements

- JDK 21 (the Java extension pack for VS Code can help detect it).
- Apache Maven 3.9 or later.
- MySQL Server 8.
- VS Code extensions: **Extension Pack for Java** and **Maven for Java**.

## Database setup

1. Start MySQL Server.
2. In MySQL Workbench or a MySQL command line, run `database/schema.sql`.
3. Then run `database/sample_data.sql` to add categories and sample dishes.
4. Create an application account instead of using the MySQL root account:

   ```sql
   CREATE USER 'food_app'@'localhost' IDENTIFIED BY 'choose-a-long-local-password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON food_ordering_system.* TO 'food_app'@'localhost';
   ```

The schema creates the `food_ordering_system` database and its seven tables. Never commit your own `.env` file or database password.

## Configure and run

1. Copy `.env.example` to `.env` and set your local database password and a unique administrator password. `.env` is ignored by Git.
2. Open the project folder in VS Code.
3. Set the environment values in the VS Code integrated PowerShell terminal. Replace the sample values with your own:

   ```powershell
   $env:FOOD_DB_URL = 'jdbc:mysql://localhost:3306/food_ordering_system?serverTimezone=UTC'
   $env:FOOD_DB_USER = 'food_app'
   $env:FOOD_DB_PASSWORD = 'your-local-database-password'
   $env:FOOD_ADMIN_EMAIL = 'admin@your-restaurant.local'
   $env:FOOD_ADMIN_PASSWORD = 'your-unique-admin-password'
   ```

4. Run `mvn clean test` to compile the project and run the automated tests.
5. Run `mvn exec:java` to start the application, or open **Run and Debug** and select **Food Ordering System**. The VS Code launch configuration reads environment variables from `.env`.
6. Register a customer from the welcome screen. The first run creates an administrator using `FOOD_ADMIN_EMAIL` and `FOOD_ADMIN_PASSWORD` if no administrator account exists. Sign in with that email and password to open the admin workspace.

If MySQL is not running or credentials are wrong, startup shows a database error. Check that the schema and sample data have been loaded and that the `FOOD_DB_*` variables match the MySQL account.

## VS Code setup on Windows

1. Install **Extension Pack for Java** and **Maven for Java** from the Extensions view.
2. Install a 64-bit JDK 21. In VS Code, run **Java: Configure Java Runtime** and select the JDK 21 installation. Confirm in a new terminal with `java -version` and `javac -version`.
3. Install Apache Maven 3.9+ and ensure `mvn -version` reports Java 21.
4. Install MySQL Server 8 and MySQL Workbench, then follow the database setup above.
5. Open this folder in VS Code, create `.env` from `.env.example`, and use the run steps above.

## Important notes

- Demo UPI only records a test payment choice. It does not connect to a bank, create a UPI transaction or process a charge.
- Food illustrations are drawn locally by Swing; the app does not download third-party images.
- This is a learning prototype, not a production deployment. Before production, add HTTPS-backed services, audit logging, stronger account recovery and a managed secrets solution.
- Prices and order totals are calculated from current database prices when checkout runs. SQL uses `PreparedStatement`; user-supplied values are not concatenated into SQL.

## Main folders

```text
src/main/java/com/foodordering/
  controller/   Coordinates requests from the Swing screens
  dao/          PreparedStatement-based database access
  model/        Typed application data and enums
  service/      Authentication, ordering and role checks
  util/         Database configuration and input validation
  view/         Login, customer and administrator Swing screens
database/       Schema and repeatable sample menu data
```
