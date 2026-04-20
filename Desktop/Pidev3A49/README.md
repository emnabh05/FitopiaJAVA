# Pidev3A49 - Project README (Oral Exam Version)

This is a JavaFX desktop app built with Maven and JDBC (MySQL).  
It has 2 main business parts:

1. Supplement shop (front store + admin back office + checkout/orders)
2. Fitness module (exercises + meals + diet regimes)

The project follows an MVC-like structure:
- `Model`: Java classes in `Models/`
- `View`: JavaFX FXML files in `src/main/resources`
- `Controller`: JavaFX controllers in `controllers/`
- `Service`: JDBC access layer in `services/`

## 1. Tech Stack and Why

- Java 17 (configured in `pom.xml`)
- Maven (dependency/build tool)
- JavaFX (`javafx-controls`, `javafx-fxml`) for desktop UI
- JDBC with MySQL connector (`mysql-connector-java`) for DB access

Why this stack:
- JavaFX handles UI and screen navigation.
- Services use JDBC with `PreparedStatement` for CRUD and SQL safety.
- Maven centralizes dependencies and run config.

## 2. How Everything Is Linked

The core chain is:

`FXML View -> Controller -> Service -> MyDataBase Connection -> MySQL`

Detailed:

1. Maven starts JavaFX app (`javafx-maven-plugin` main class is `tn.esprit.Pidev3A49.test.SupplementShowcaseApp`).
2. The app loads first screen from `Start.fxml` through `SceneNavigator`.
3. Each FXML has `fx:controller="..."`.  
   JavaFX injects UI fields marked with `@FXML` into that controller.
4. Controller reacts to button clicks (`onAction="#methodName"`).
5. Controller calls Service classes (`ServiceSupplement`, `ServiceSupplementOrder`, etc.).
6. Service gets DB connection from singleton `MyDataBase`.
7. `MyDataBase` opens JDBC connection and runs `SchemaInitializer.initialize(...)`.
8. SQL executes on MySQL tables, results mapped back to model objects, UI refreshes.

## 3. Project Structure (Important Packages)

- `src/main/java/tn/esprit/Pidev3A49/controllers`
  - JavaFX controllers (UI logic, events, validation, navigation calls)
- `src/main/java/tn/esprit/Pidev3A49/services`
  - JDBC CRUD logic (SQL queries, mapping, transactions)
- `src/main/java/tn/esprit/Pidev3A49/Models`
  - Entities (Supplement, SupplementOrder, FitnessExercise, Repas, etc.)
- `src/main/java/tn/esprit/Pidev3A49/utils`
  - Shared utilities:
  - `MyDataBase`: singleton DB connection
  - `SchemaInitializer`: auto create/migrate tables
  - `SceneNavigator`: screen routing + history
  - `CartStore`, `PendingOrderStore`, `AppSession`: shared in-memory state
- `src/main/java/tn/esprit/Pidev3A49/interfaces`
  - `IServices<T>` generic CRUD contract
- `src/main/resources`
  - FXML views + CSS files

## 4. Main Screens and Controllers

- `Start.fxml` -> `LandingController`
- `SupplementCatalogShowcase.fxml` -> `SupplementShowcaseController` (store front + cart)
- `Main.fxml` -> `AdminDashboardController` (supplement admin CRUD)
- `Checkout.fxml` -> `CheckoutController`
- `CardPayment.fxml` -> `CardPaymentController`
- `FrontOrders.fxml` -> `FrontOrdersController` (customer order history by email)
- `OrderedCustomers.fxml` -> `OrderedCustomersController` (admin order list + status updates)
- `FitopiaHome.fxml` -> `FitopiaHomeController` (fitness front CRUD for exercises)
- `FitnessCrudAdmin.fxml` -> `MainController` (fitness back office: regimes, meals, exercises)
- `SupplementProgress.fxml` / `MonthlyRanking.fxml` -> navigation placeholder controllers

Navigation is centralized in `SceneNavigator` (view constants + back stack).

## 5. Database and JDBC

### DB connection

In `MyDataBase`:
- URL: `jdbc:mysql://127.0.0.1:3306/esprit?createDatabaseIfNotExist=true&serverTimezone=UTC`
- Username: `root`
- Password: empty string

`createDatabaseIfNotExist=true` means DB `esprit` can be auto-created.

### Auto-created tables (`SchemaInitializer`)

- `crud_supplement`
- `crud_supplement_order`
- `crud_supplement_order_item`
- `crud_regime_alimentaire`
- `crud_repas`
- `fitness_exercise`

`fitness_exercise` also has migration logic (adds missing columns, handles old `sets` -> `sets_count`).

## 6. End-to-End Flows You Should Explain

### Flow A: Admin adds supplement, front store shows it

1. Admin opens `Main.fxml` (`AdminDashboardController`).
2. Click create -> `handleCreateSupplement()`.
3. Controller builds `Supplement` object and calls `ServiceSupplement.add(...)`.
4. SQL insert in `crud_supplement`.
5. Front screen (`SupplementShowcaseController.loadProducts`) calls `ServiceSupplement.getAll()`.
6. New supplement appears in product grid.

Extra:
- If an image is selected, file is copied locally to `supplement-images/` via `SupplementImageStorage`.

### Flow B: Customer checkout and order persistence

1. In store, user adds products; state is kept in singleton `CartStore`.
2. In `CheckoutController`, user fills customer data + payment choice.
3. `buildOrder(...)` creates `SupplementOrder` + `SupplementOrderItem` list.
4. If Visa/Mastercard:
   - order is temporarily stored in `PendingOrderStore`
   - navigate to `CardPayment.fxml`
   - `CardPaymentController.confirmCardPayment()` validates card then saves order.
5. If PayPal/Cash:
   - `CheckoutController` directly calls `ServiceSupplementOrder.placeOrder(order)`.

Inside `ServiceSupplementOrder.placeOrder(...)`:
- Starts SQL transaction (`setAutoCommit(false)`)
- Inserts order header
- Decrements stock with condition (`stock >= quantity`)
- Batch inserts order lines
- Commits or rollbacks on failure

This keeps stock + order data consistent.

### Flow C: Fitness CRUD

- `MainController` (admin fitness) and `FitopiaHomeController` (front fitness) both call `ServiceFitnessExercise`.
- `MainController` also calls:
  - `ServiceRegimeAlimentaire`
  - `ServiceRepas`
- Data is shown in JavaFX `TableView` components with `PropertyValueFactory`.

## 7. Maven Configuration

In `pom.xml`:
- Dependencies:
  - MySQL connector
  - JavaFX FXML
  - JavaFX Controls
- Plugin:
  - `javafx-maven-plugin`
  - main class: `tn.esprit.Pidev3A49.test.SupplementShowcaseApp`

Run command:

```bash
mvn javafx:run
```

## 8. Quick Setup

1. Install JDK 17+ and Maven.
2. Start MySQL server.
3. Make sure credentials in `MyDataBase` are valid for your machine.
4. Configure SMTP if you want automatic order confirmation emails:
   - `FITOPIA_MAIL_USERNAME` (example: `alitouaiti45@gmail.com`)
   - `FITOPIA_MAIL_PASSWORD` (Gmail app password, without spaces)
   - Optional: `FITOPIA_MAIL_FROM`, `FITOPIA_MAIL_HOST`, `FITOPIA_MAIL_PORT`
   - If no vars are provided, the current build falls back to the configured project Gmail sender.
5. Run `mvn javafx:run`.

If MySQL is down, many controllers catch the exception and show a "service unavailable" message.

## 9. Teacher Q&A Cheat Sheet

Q: Why use services between controller and DB?  
A: Separation of concerns. Controllers handle UI/events; services handle SQL/business rules.

Q: How do you avoid SQL injection?  
A: Services use `PreparedStatement` with parameters.

Q: How are tables created?  
A: `MyDataBase` calls `SchemaInitializer.initialize(connection)` at startup.

Q: Where is shared app state stored between screens?  
A: Singletons: `CartStore`, `PendingOrderStore`, `AppSession`, plus navigation history in `SceneNavigator`.

Q: How is transactional consistency handled in orders?  
A: `ServiceSupplementOrder.placeOrder` uses one DB transaction for order header, stock update, and order items.

Q: How is JavaFX linked to controller methods?  
A: `fx:controller` in FXML and event handlers like `onAction="#placeOrder"` call methods annotated with `@FXML`.

Q: How does back-end supplement data appear in front-end automatically?  
A: Both sides read/write same table `crud_supplement` through `ServiceSupplement`.

Q: Why no ORM (Hibernate)?  
A: Project uses direct JDBC for simpler control and educational clarity.

## 10. Important Notes / Limitations

- DB credentials are hardcoded in `MyDataBase` (good for demo, not for production).
- `AppSession` is mocked (always authenticated with fixed roles).
- `ServicePersonne` / `test.Main` are legacy demo parts and not central to current JavaFX flows.
- `AddNewSupplement.fxml` references another controller package and is not part of current navigation flow.

---

If you present the architecture in one line:

`JavaFX FXML UI -> Controllers -> Services (JDBC) -> MySQL, with shared singleton stores for session/cart/navigation.`
