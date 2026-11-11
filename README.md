# Employee Leave Management System – Backend

A Java Spring Boot microservices-based Employee Leave Management System (ELMS) backend.

The backend consists of three independent microservices: Admin Service, Manager Service, and Employee Service. Each microservice handles specific business responsibilities and communicates with other services when required.

---

# 1. Project Overview

The backend is divided into three microservices.

| Microservice     | Port | Responsibility                                                                                   |
| ---------------- | ---: | ------------------------------------------------------------------------------------------------ |
| Admin Service    | 8083 | Employee management, manager management, leave administration, holidays, audit logs, and reports |
| Manager Service  | 8082 | Leave approvals, rejections, decision history, and notifications                                 |
| Employee Service | 8081 | Employee profile, leave applications, leave history, and cancellation                            |

### Architecture

```text
                 Backend Microservices
                          |
          +---------------+---------------+
          |               |               |
          v               v               v
    Admin Service   Manager Service  Employee Service
       :8083             :8082            :8081
                          |
                          | OpenFeign
                          |
                          v
                   Employee Service
                          |
                          v
                     MySQL DB
                     :3306
```

---

# 2. Technology Stack

### Backend Technologies

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Cloud OpenFeign
- JWT Authentication
- Hibernate ORM
- Maven
- MySQL
- Java Mail / Spring Mail

### Development and Deployment Tools

- Eclipse IDE
- MySQL Workbench
- Postman
- Git and GitHub
- Docker
- Docker Compose

---

# 3. Project Structure

```text
backend/
│
├── admin-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml
│   └── Dockerfile
│
├── manager-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml
│   └── Dockerfile
│
└── employee-service/
    ├── src/
    │   └── main/
    │       ├── java/
    │       └── resources/
    │           └── application.properties
    ├── pom.xml
    └── Dockerfile
```

Each microservice is an independent Maven and Spring Boot application.

---

# 4. Prerequisites

Install the following software before running the backend.

| Software        | Required Version              |
| --------------- | ----------------------------- |
| Java JDK        | 21                            |
| Apache Maven    | 3.9+                          |
| MySQL Server    | 8.0+ recommended              |
| MySQL Workbench | Latest                        |
| Eclipse IDE     | Latest                        |
| Postman         | Latest                        |
| Git             | Latest                        |
| Docker Desktop  | Latest, for Docker deployment |

### Verify installations

```bash
java -version
```

```bash
mvn -version
```

```bash
mysql --version
```

```bash
docker --version
```

```bash
docker compose version
```

---

# 5. Microservice Responsibilities

## 5.1 Admin Service

**Service Name:** admin-service

**Port:** 8083

### Responsibilities

#### Employee Management

- Add employees.
- View employee details.
- Update employee information.
- Delete employees.

#### Manager Management

- Add managers.
- View manager details.
- Update manager information.
- Delete managers.

#### Leave Management

- View all leave records.
- Manage leave records.
- Review manager-approved leave requests.

#### Holiday Management

- Manage organization holidays.
- Maintain holiday information.

#### Audit Logs

- View administrative activity records.
- Monitor important system operations.

#### Reports

- Generate leave management reports.
- Export CSV or Excel reports where implemented.

### Application Configuration

```properties
spring.application.name=admin-service
server.port=8083

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

employee.service.url=${EMPLOYEE_SERVICE_URL:http://employee-service:8081}
leave.service.url=${LEAVE_SERVICE_URL:http://manager-service:8082}
```

---

## 5.2 Manager Service

**Service Name:** manager-service

**Port:** 8082

### Responsibilities

#### Pending Leave Requests

- View pending leave requests.
- View assigned employees' leave requests.
- Review leave application details.

#### Leave Decisions

- Approve leave requests.
- Reject leave requests.
- Provide rejection reasons.
- Maintain decision history.

#### Notifications

- Send employee notifications.
- Maintain notification records.
- Support email notifications through Gmail SMTP, where configured.

#### Decision History

- View previous approval and rejection decisions.
- Track manager actions.

### Service-to-Service Communication

Manager Service communicates with Employee Service using Spring Cloud OpenFeign.

Docker URL:

```text
http://employee-service:8081
```

Local development URL:

```text
http://localhost:8081
```

### Application Configuration

```properties
spring.application.name=manager-service
server.port=8082

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

employee.service.url=${EMPLOYEE_SERVICE_URL:http://employee-service:8081}

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 5.3 Employee Service

**Service Name:** employee-service

**Port:** 8081

### Responsibilities

#### Employee Profile

- View employee profile.
- Access employee information.

#### Leave Application

- Apply for leave.
- Submit leave details.
- Track application status.

#### Leave History

- View previously submitted leave requests.
- Check pending leave requests.
- View approved or rejected leave requests.

#### Leave Cancellation

- Cancel eligible pending leave requests.

#### Authentication

- Support employee authentication and authorization as implemented.

### Application Configuration

```properties
spring.application.name=employee-service
server.port=8081

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

---

# 6. Database Setup

**Database:** MySQL

**Database Name:** manager_db

**Port:** 3306

The current configuration uses one shared MySQL database for all three microservices.

### Step 1: Start MySQL Server

Start MySQL Server from Windows Services or MySQL Workbench.

### Step 2: Create Database

Execute:

```sql
CREATE DATABASE IF NOT EXISTS manager_db;
```

### Step 3: Create Database User

Create a dedicated application user:

```sql
CREATE USER IF NOT EXISTS 'elms_user'@'%'
IDENTIFIED BY 'YOUR_STRONG_PASSWORD';

GRANT ALL PRIVILEGES ON manager_db.*
TO 'elms_user'@'%';

FLUSH PRIVILEGES;
```

### Step 4: Verify Database Connection

Use MySQL Workbench:

| Configuration | Value                    |
| ------------- | ------------------------ |
| Host          | localhost                |
| Port          | 3306                     |
| Username      | elms_user                |
| Password      | Your configured password |
| Database      | manager_db               |

### Database Properties

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Note:** All three services currently connect to the same database. Ensure that entity mappings and table ownership are compatible with this shared database arrangement.

---

# 7. Environment Variables

Use environment variables to configure database access, JWT authentication, service communication, and email credentials.

### Required Environment Variables

| Variable             | Description                           |
| -------------------- | ------------------------------------- |
| DB_URL               | MySQL JDBC connection URL             |
| DB_USERNAME          | MySQL application username            |
| DB_PASSWORD          | MySQL application password            |
| JWT_SECRET           | JWT signing and validation secret     |
| JWT_EXPIRATION       | Token expiration time in milliseconds |
| MAIL_USERNAME        | Gmail account used for sending emails |
| MAIL_PASSWORD        | Gmail App Password                    |
| EMPLOYEE_SERVICE_URL | Employee Service base URL             |
| LEAVE_SERVICE_URL    | Manager/leave service base URL        |

### Local Development Example

Configure these variables in your terminal or Eclipse run configuration:

```text
DB_URL=jdbc:mysql://localhost:3306/manager_db
DB_USERNAME=elms_user
DB_PASSWORD=YOUR_STRONG_PASSWORD

JWT_SECRET=YOUR_NEW_BASE64_SECRET
JWT_EXPIRATION=86400000

MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

EMPLOYEE_SERVICE_URL=http://localhost:8081
LEAVE_SERVICE_URL=http://localhost:8082
```

### Docker Environment Example

Create a `.env` file in the parent project directory:

```dotenv
DB_NAME=manager_db
DB_USERNAME=elms_user
DB_PASSWORD=YOUR_STRONG_PASSWORD

JWT_SECRET=YOUR_NEW_BASE64_SECRET
JWT_EXPIRATION=86400000

MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

For Docker, the database URL is constructed using:

```text
jdbc:mysql://host.docker.internal:3306/manager_db
```

**Security:**

- Never commit `.env` to GitHub.
- Never hardcode passwords or JWT secrets in source code.
- Use a dedicated MySQL user instead of root.
- Rotate credentials if they have been exposed.

---

# 8. JWT Authentication

The backend uses JSON Web Tokens (JWT) for authentication.

### Authentication Flow

1. User submits login credentials.
2. Backend validates credentials.
3. Backend generates a JWT token.
4. Client stores the token.
5. Client sends the token in subsequent API requests.
6. Backend security filters validate the token.
7. Access is granted according to the user's role and permissions.

### Authorization Header

```http
Authorization: Bearer <JWT_TOKEN>
```

### User Roles

- ADMIN
- MANAGER
- EMPLOYEE

The backend should enforce role-based access restrictions for protected endpoints.

### JWT Configuration

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

Token expiration:

```text
86400000 milliseconds = 24 hours
```

Use the same JWT signing secret across services that must validate tokens issued by another service.

---

# 9. Email Configuration – Gmail SMTP

The Manager Service supports email notification configuration through Gmail SMTP.

### SMTP Properties

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587

spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

### Gmail App Password Setup

1. Open your Google Account.
2. Go to Security.
3. Enable 2-Step Verification.
4. Open Google's App Passwords page.
5. Generate an App Password.
6. Copy the generated password.
7. Configure it in `MAIL_PASSWORD`.

Example:

```dotenv
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

Use an App Password rather than your regular Gmail password. Availability depends on your Google Account security settings.

---

# 10. API Documentation

The three microservices expose REST APIs.

### Service Base URLs

| Microservice     | Base URL                                       |
| ---------------- | ---------------------------------------------- |
| Admin Service    | [http://localhost:8083](http://localhost:8083) |
| Manager Service  | [http://localhost:8082](http://localhost:8082) |
| Employee Service | [http://localhost:8081](http://localhost:8081) |

### Confirmed Manager API

The following endpoint has been used in the Manager Service:

| HTTP Method | Endpoint                         | Description                        |
| ----------- | -------------------------------- | ---------------------------------- |
| GET         | `/api/manager/decisions/pending` | Retrieve pending manager decisions |

Example:

```http
GET http://localhost:8082/api/manager/decisions/pending
```

### API Functional Groups

The following table describes the expected business operations. Consult the corresponding controller classes for exact paths, methods, request bodies, and response formats.

#### Admin Service

| Module              | Operations                             |
| ------------------- | -------------------------------------- |
| Employee Management | Create, view, update, delete employees |
| Manager Management  | Create, view, update, delete managers  |
| Leave Management    | View and manage leave records          |
| Holiday Management  | Manage holidays                        |
| Audit Logs          | View audit records                     |
| Reports             | Generate and export reports            |

#### Manager Service

| Module            | Operations                       |
| ----------------- | -------------------------------- |
| Pending Decisions | View pending leave requests      |
| Leave Decisions   | Approve or reject leave requests |
| Decision History  | View previous decisions          |
| Notifications     | Manage employee notifications    |

#### Employee Service

| Module             | Operations                       |
| ------------------ | -------------------------------- |
| Employee Profile   | View employee details            |
| Leave Application  | Apply for leave                  |
| Leave History      | View leave records               |
| Leave Cancellation | Cancel eligible pending requests |

### Testing with Postman

1. Open Postman.
2. Select the HTTP method.
3. Enter the service URL.
4. Add the Authorization header when required.
5. Provide the request body when required.
6. Send the request.
7. Verify the response and database changes.

For protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

# 11. Run Backend Locally

Each microservice can be started independently.

### Step 1: Start MySQL

Ensure MySQL Server is running and the `manager_db` database exists.

### Step 2: Configure Environment Variables

Configure all required environment variables before starting the services.

For local development, use:

```text
DB_URL=jdbc:mysql://localhost:3306/manager_db
DB_USERNAME=elms_user
DB_PASSWORD=YOUR_STRONG_PASSWORD
```

### Step 3: Run Employee Service

Open a terminal:

```bash
cd employee-service
mvn clean install
mvn spring-boot:run
```

Employee Service:

```text
http://localhost:8081
```

### Step 4: Run Manager Service

Open another terminal:

```bash
cd manager-service
mvn clean install
mvn spring-boot:run
```

Manager Service:

```text
http://localhost:8082
```

### Step 5: Run Admin Service

Open another terminal:

```bash
cd admin-service
mvn clean install
mvn spring-boot:run
```

Admin Service:

```text
http://localhost:8083
```

**Important:** Run each command from its respective service directory. If using Eclipse, configure the required environment variables in each Spring Boot Run Configuration.

---

# 12. Docker Deployment

The backend supports Docker deployment using Docker Compose from the project root.

### Docker Containers

| Container        | Port |
| ---------------- | ---: |
| Admin Service    | 8083 |
| Manager Service  | 8082 |
| Employee Service | 8081 |

The current setup uses MySQL installed on the Windows host machine rather than a MySQL container.

### Step 1: Start Docker Desktop

Make sure Docker Desktop is running.

### Step 2: Configure `.env`

Create the root `.env` file with the database, JWT, and email variables.

### Step 3: Build and Start All Services

Run from the project root:

```bash
docker compose up -d --build admin-service manager-service employee-service
```

Alternatively, start the complete application:

```bash
docker compose up -d --build
```

### Step 4: Check Running Containers

```bash
docker compose ps
```

### Step 5: View Logs

All services:

```bash
docker compose logs -f
```

Admin Service:

```bash
docker compose logs -f admin-service
```

Manager Service:

```bash
docker compose logs -f manager-service
```

Employee Service:

```bash
docker compose logs -f employee-service
```

### Step 6: Stop Services

```bash
docker compose down
```

Stopping or rebuilding the backend containers does not delete the MySQL database running on Windows.

---

# 13. Future Enhancements

Potential future improvements:

- API Gateway.
- Centralized configuration management.
- Redis caching.
- Kafka-based event communication.
- Centralized logging and monitoring.
- Automated unit and integration testing.
- CI/CD pipeline.
- AWS cloud deployment.

These are potential enhancements, not claims about currently implemented features.

---

# 14. Conclusion

The Employee Leave Management System backend follows a microservices architecture using Java 21 and Spring Boot.

The Admin Service handles administrative operations, the Manager Service handles leave decisions and notifications, and the Employee Service provides employee self-service functionality.

MySQL provides persistent data storage, JWT supports authentication, Spring Cloud OpenFeign enables service-to-service communication, and Docker Compose simplifies deployment.

**Project:** Employee Leave Management System (ELMS)
**Architecture:** Microservices
**Backend:** Java 21 + Spring Boot
**Database:** MySQL
**Authentication:** JWT
**Inter-service Communication:** Spring Cloud OpenFeign
**Email:** Gmail SMTP
**Build Tool:** Maven
**Containerization:** Docker + Docker Compose
