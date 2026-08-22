# Login / Registration – Spring Boot Project

A small full-stack Java web app implementing the two wireframe screens
(Login and Registration) using:

- **Spring Boot** – app bootstrap & auto-configuration
- **Spring MVC** – `@Controller` / `DispatcherServlet` request handling
- **Spring Core (IoC)** – `@Service`, `@Repository`, `@Autowired` beans
- **Hibernate / Spring Data JPA** – `User` entity persisted to a **MySQL** database
- **Servlet API** – a raw `HttpServlet` (`VisitCounterServlet`) registered via
  `@WebServlet` + `@ServletComponentScan`, running alongside Spring MVC in the
  same embedded Tomcat container
- **Thymeleaf** – server-rendered HTML views styled to match the provided
  wireframe (dark background, outlined boxes, circular photo placeholder)

## Project layout

```
src/main/java/com/example/loginapp/
  LoginRegisterApplication.java   # main() / @SpringBootApplication
  controller/AuthController.java  # /login, /register, /home, /logout
  controller/TripController.java  # /trips, /trips/new, /trips/itinerary, /trips/view, /calendar
  controller/CommunityController.java # /community (Screen 10)
  controller/AdminController.java # /admin (Screen 12, admin users only)
  entity/User.java                # JPA entity (Hibernate)
  entity/Trip.java                # JPA entity for planned trips
  entity/CommunityPost.java       # JPA entity for community shares
  repository/UserRepository.java  # Spring Data JPA repository
  repository/TripRepository.java
  repository/CommunityPostRepository.java
  service/UserService.java        # business logic (Spring bean)
  servlet/VisitCounterServlet.java# plain Servlet example -> GET /visits
  config/WebConfig.java           # serves uploaded photos from /uploads/**
src/main/resources/
  application.properties
  templates/login.html, register.html, home.html, ...
  templates/community.html        # Screen 10
  templates/calendar.html         # Screen 11
  templates/admin.html            # Screen 12
  static/css/style.css
```

## Requirements

- Java 17+
- Maven 3.6+ (or use the included `mvnw` wrapper if you add one)
- A running MySQL server (local install, or Docker)

## Database setup

1. Make sure MySQL is running and reachable (default `localhost:3306`).
2. Create the database (optional — the app can also auto-create it, see below):
   ```sql
   CREATE DATABASE loginappdb;
   ```
3. Open `src/main/resources/application.properties` and set your real
   MySQL username/password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/loginappdb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```
   `createDatabaseIfNotExist=true` in the URL means step 2 is optional — MySQL
   will create the schema automatically the first time the app connects
   (as long as the user has permission to create databases).
4. Hibernate will automatically create the `users` table on first run
   (`spring.jpa.hibernate.ddl-auto=update`).

### Using Docker instead of a local MySQL install

```bash
docker run --name loginapp-mysql -e MYSQL_ROOT_PASSWORD=your_mysql_password \
  -e MYSQL_DATABASE=loginappdb -p 3306:3306 -d mysql:8
```

## Running

```bash
mvn spring-boot:run
```

Then open:

- http://localhost:8080/login     — Login screen
- http://localhost:8080/register  — Registration screen
- http://localhost:8080/trips     — User Trip Listing (Screen 6)
- http://localhost:8080/calendar  — Calendar View of your trips (Screen 11)
- http://localhost:8080/community — Community tab, share & browse trips (Screen 10)
- http://localhost:8080/admin     — Admin Panel (Screen 12, admin users only)
- http://localhost:8080/visits    — plain-Servlet demo endpoint

### Making a user an admin

There's no sign-up flow for admins on purpose. After registering normally,
promote your account directly in the database:

```sql
UPDATE users SET admin = true WHERE username = 'your_username';
```

Once promoted, an "Admin Panel" link appears on the home screen and
`/admin` becomes reachable (any other logged-in user is redirected back to
`/home` if they try to visit it).


You can inspect the data with any MySQL client (MySQL Workbench, DBeaver,
`mysql` CLI, etc.) pointed at the `loginappdb` database, table `users`.

## Flow

1. Go to **/register**, fill in username, password, first/last name, email,
   phone, city, country, additional info, and optionally pick a profile
   photo. Submit "Register Users".
2. You're redirected to **/login** with a success message.
3. Log in with the username/password you just registered.
4. You land on **/home**, which shows your saved details and photo.

## Notes / things to harden for production

- Passwords are stored **in plain text** in this demo for simplicity. Add
  `spring-boot-starter-security` and hash passwords with `BCryptPasswordEncoder`
  before storing them.
- There's no CSRF protection or Spring Security session management — add
  `spring-boot-starter-security` if you need real authentication guarantees.
- The wireframe's registration screen didn't show explicit username/password
  fields, so they were added at the top of the form since a login step
  requires credentials to check against.
- Uploaded photos are written to a local `uploads/` folder next to the app —
  swap this for S3/Cloud storage in a real deployment.
