# GlobalTrotter — Travel Planning Web Application

GlobalTrotter is a full-stack travel planning web application built with **Java Spring Boot**, **Spring MVC**, **Thymeleaf**, **Spring Data JPA/Hibernate**, and **MySQL**.

The application provides user authentication, profile management, trip planning, itinerary management, activity discovery, calendar views, community sharing, password recovery through email OTP, and an administrator analytics dashboard.

---

## ✨ Features

### 🔐 Authentication & Account Management
- User registration with personal and travel-related details
- Username/password login
- Logout functionality
- Session-based authentication
- Forgot-password workflow
- Email OTP verification for password reset
- OTP expiry and failed-attempt tracking
- Profile editing
- Optional profile photo upload

### 🧳 Trip Management
- Create and manage trips
- View a user's trip list
- View individual trip details
- Build trip itineraries
- Track trip status such as:
  - Upcoming
  - Ongoing
  - Completed
- Calendar-based trip view

### 🌍 Travel Discovery
- Search activities and destinations
- Filter activities by type
- Browse destination/activity cards
- Explore travel-related information from the home page

### 👥 Community
- Browse community posts
- Search posts by title or place
- Share travel experiences with the community
- Store activity type, location, title, and post content

### 📊 Admin Dashboard
Administrators can access a dedicated dashboard to:
- View total users
- View total trips
- View total community posts
- View active users
- View average trips per user
- Manage users
- Promote/demote administrator privileges
- Delete users
- Review all trips
- View trip-status statistics
- View popular destinations
- View popular activities
- View user-registration trends
- View trip-creation trends

### 🖥️ UI
- Server-rendered Thymeleaf pages
- Responsive HTML/CSS layouts
- Dedicated styling for login, registration, home, trips, itinerary, calendar, community, profile, admin, and password-reset screens
- Custom GlobalTrotter login hero illustration
- Default avatar support

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Backend Framework | Spring Boot 3.2.5 |
| Web Framework | Spring MVC |
| View Engine | Thymeleaf |
| ORM | Hibernate |
| Persistence | Spring Data JPA |
| Database | MySQL |
| Mail | Spring Mail / SMTP |
| Servlet | Jakarta Servlet API via embedded Tomcat |
| Build Tool | Maven |
| Validation | Jakarta Bean Validation |
| Frontend | HTML, CSS, JavaScript |
| Development | Spring Boot DevTools |

---

## 📁 Project Structure

```text
login-register-app/
│
├── pom.xml
├── README.md
├── uploads/
│
└── src/
    └── main/
        ├── java/
        │   └── com/example/loginapp/
        │       ├── LoginRegisterApplication.java
        │       │
        │       ├── config/
        │       │   └── WebConfig.java
        │       │
        │       ├── controller/
        │       │   ├── AuthController.java
        │       │   ├── AdminController.java
        │       │   ├── CommunityController.java
        │       │   ├── ForgotPasswordController.java
        │       │   └── TripController.java
        │       │
        │       ├── entity/
        │       │   ├── User.java
        │       │   ├── Trip.java
        │       │   └── CommunityPost.java
        │       │
        │       ├── model/
        │       │   ├── ActivityCard.java
        │       │   └── DestinationCard.java
        │       │
        │       ├── repository/
        │       │   ├── UserRepository.java
        │       │   ├── TripRepository.java
        │       │   └── CommunityPostRepository.java
        │       │
        │       ├── service/
        │       │   ├── UserService.java
        │       │   └── EmailService.java
        │       │
        │       └── servlet/
        │           └── VisitCounterServlet.java
        │
        └── resources/
            ├── application.properties
            │
            ├── templates/
            │   ├── login.html
            │   ├── register.html
            │   ├── home.html
            │   ├── profile.html
            │   ├── edit-profile.html
            │   ├── create-trip.html
            │   ├── trip-listing.html
            │   ├── itinerary-view.html
            │   ├── build-itinerary.html
            │   ├── calendar.html
            │   ├── activity-search.html
            │   ├── community.html
            │   ├── admin.html
            │   ├── forgot-password.html
            │   ├── verify-otp.html
            │   └── reset-password.html
            │
            └── static/
                ├── css/
                │   ├── common.css
                │   ├── login.css
                │   ├── register.css
                │   ├── home.css
                │   ├── profile.css
                │   ├── edit-profile.css
                │   ├── create-trip.css
                │   ├── trip-listing.css
                │   ├── itinerary-view.css
                │   ├── build-itinerary.css
                │   ├── calendar.css
                │   ├── activity-search.css
                │   ├── community.css
                │   ├── admin.css
                │   ├── forgot-password.css
                │   └── verify-otp.css
                │
                ├── js/
                │   ├── common.js
                │   ├── verify-otp.js
                │   └── itinerary-view.js
                │
                └── images/
                    └── default-avatar.svg
```

---

## ⚙️ Prerequisites

Before running the application, install:

1. **Java 17 or later**
2. **Maven 3.6+**
3. **MySQL 8.x recommended**
4. A Gmail account with **2-Step Verification** enabled if you want to test password-reset emails

Verify Java and Maven:

```bash
java -version
mvn -version
```

---

## 🗄️ Database Configuration

The application uses MySQL with the database name:

```text
loginappdb
```

You can create it manually:

```sql
CREATE DATABASE loginappdb;
```

The project is also configured with:

```text
createDatabaseIfNotExist=true
```

so MySQL can create the database automatically when the configured account has sufficient privileges.

### Configure MySQL

Open:

```text
src/main/resources/application.properties
```

Update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loginappdb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Therefore, the required tables are created/updated automatically from the JPA entities.

---

## 📧 Email / Forgot Password Configuration

The forgot-password feature sends an OTP through SMTP.

For Gmail, configure an **App Password** rather than your normal Gmail password.

Update:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.mail.from=YOUR_EMAIL@gmail.com
app.otp.expiry-minutes=10
```

### Important

Do **not** commit real passwords, SMTP credentials, or other secrets to GitHub.

For a production application, move credentials to environment variables or a secure secret-management system.

---

## 🚀 Running the Application

Clone or extract the project and enter the project directory:

```bash
cd login-register-app
```

Build the project:

```bash
mvn clean package
```

Run the application:

```bash
mvn spring-boot:run
```

Alternatively, run the generated JAR:

```bash
java -jar target/login-register-app.jar
```

The application runs on:

```text
http://localhost:8080
```

---

## 🌐 Main Routes

| Route | Purpose |
|---|---|
| `/login` | Login page |
| `/register` | User registration |
| `/home` | Main dashboard/home page |
| `/profile` | User profile |
| `/profile/edit` | Edit profile |
| `/trips` | User's trips |
| `/trips/new` | Create a trip |
| `/trips/itinerary` | Build/view itinerary flow |
| `/trips/view` | View trip details |
| `/calendar` | Calendar view |
| `/activities` | Activity search/discovery |
| `/community` | Community travel posts |
| `/forgot-password` | Request password-reset OTP |
| `/verify-otp` | Verify OTP |
| `/reset-password` | Set a new password |
| `/admin` | Administrator dashboard |
| `/visits` | Servlet visit-counter demonstration |

Most application pages require an authenticated user. The admin dashboard additionally requires the user's `admin` flag to be enabled.

---

## 👤 Creating an Administrator

There is intentionally no public admin-registration form.

First register a normal account through:

```text
http://localhost:8080/register
```

Then update the account in MySQL:

```sql
UPDATE users
SET admin = true
WHERE username = 'your_username';
```

Log out and log in again.

The **Admin Panel** option will then be available to the administrator.

---

## 🔄 Application Flow

```text
Register
   ↓
Login
   ↓
Home Dashboard
   ├── Profile
   ├── Search Activities
   ├── My Trips
   │     ├── Create Trip
   │     ├── Build Itinerary
   │     ├── View Trip
   │     └── Calendar
   ├── Community
   └── Admin Panel (admins only)
```

### Password Reset Flow

```text
Forgot Password
       ↓
Enter Email
       ↓
Generate OTP
       ↓
Send OTP via SMTP
       ↓
Verify OTP
       ↓
Reset Password
       ↓
Login
```

---

## 🧩 Architecture

The project follows a conventional layered Spring Boot architecture:

```text
Browser
   ↓
Thymeleaf HTML / Forms
   ↓
Spring MVC Controllers
   ↓
Service Layer
   ↓
Spring Data JPA Repositories
   ↓
Hibernate
   ↓
MySQL
```

A standalone `VisitCounterServlet` is also registered with the embedded Tomcat server to demonstrate traditional Servlet API integration alongside Spring MVC.

---

## 🗃️ Main Data Models

### User

Stores:
- Username
- Password
- First name
- Last name
- Email
- Phone number
- City
- Country
- Additional information
- Profile photo path
- Admin status
- Account creation time
- Password-reset OTP information

### Trip

Stores information associated with planned trips and their itinerary/status.

### CommunityPost

Stores travel experiences shared by users, including:
- Author
- Title
- Content
- Place
- Activity type
- Creation information

---

## 📸 Profile Photo Uploads

Uploaded profile photos are stored under:

```text
uploads/
```

The application exposes uploaded files through the configured web mapping in `WebConfig.java`.

The application currently limits uploads to:

```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

For production, cloud object storage such as Amazon S3 or another managed storage service is recommended.

---

## 🧪 Development

Thymeleaf caching is disabled during development:

```properties
spring.thymeleaf.cache=false
```

Spring Boot DevTools is included to improve the development experience.

After changing Java, HTML, or CSS files, restart/reload the application as required by the type of change.

---

## 🔒 Security Considerations

This project is suitable as a learning/demo application, but several areas should be hardened before production deployment.

### Password Storage

The current implementation stores passwords directly for demonstration purposes.

For production:

- Add Spring Security
- Hash passwords with BCrypt or Argon2
- Never store plaintext passwords

### Authentication

The current application uses session-based login handling without a complete Spring Security configuration.

For production, consider:

- Spring Security
- Secure session management
- CSRF protection
- Authorization rules
- Secure cookies
- Login rate limiting
- Account lockout protection

### Secrets

Do not store:

```text
MySQL passwords
SMTP passwords
API keys
application secrets
```

directly in source control.

Use environment variables or a secret manager.

### File Uploads

Production deployments should additionally validate:

- File type
- File extension
- File content
- File size
- Filename/path safety

---

## 🐳 Optional MySQL Docker Setup

If you prefer Docker:

```bash
docker run --name globaltrotter-mysql   -e MYSQL_ROOT_PASSWORD=your_mysql_password   -e MYSQL_DATABASE=loginappdb   -p 3306:3306   -d mysql:8
```

Then configure the same credentials in `application.properties`.

---

## 🛠️ Common Problems

### MySQL connection refused

Make sure MySQL is running and that port `3306` is available.

Check:

```text
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

### Port 8080 already in use

Change:

```properties
server.port=8080
```

to another port, for example:

```properties
server.port=8081
```

Then access:

```text
http://localhost:8081
```

### Password-reset email is not sent

Verify:

- Gmail 2-Step Verification is enabled
- A Gmail App Password was created
- `spring.mail.username` is correct
- `spring.mail.password` contains the App Password
- SMTP port `587` is not blocked
- `app.mail.from` is configured correctly

### Login does not work

Make sure:

1. The application is connected to MySQL.
2. The registration was successful.
3. The username exists in the `users` table.
4. The password entered matches the registered password.

---

## 📦 Build Output

After running:

```bash
mvn clean package
```

the packaged application will be generated under:

```text
target/login-register-app.jar
```

Run it with:

```bash
java -jar target/login-register-app.jar
```

---

## 🎯 Project Purpose

GlobalTrotter demonstrates how a complete server-side travel application can be developed using the Spring ecosystem. It combines authentication, database persistence, trip management, itinerary planning, community functionality, email-based password recovery, file uploads, and administrative analytics in a single web application.

---

## 📄 License

This project does not currently specify a separate open-source license. If the project is intended for public distribution, add an appropriate `LICENSE` file and update this section accordingly.

---

## 👨‍💻 Project Status

**Type:** Full-Stack Java Web Application  
**Framework:** Spring Boot  
**Database:** MySQL  
**Frontend:** Thymeleaf + HTML/CSS/JavaScript  
**Build:** Maven  
**Java:** 17+

