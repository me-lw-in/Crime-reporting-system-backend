# Crime Reporting System Backend

## 📝 Overview
This is the backend for a crime reporting system built with Spring Boot, providing APIs for citizens to submit reports and officers to manage cases.

## 🔧 Version Information
- **Spring Boot**: 3.4.4
- **Java**: 21
- **Hibernate**: Built-in via Spring Boot Starter JPA
- **Database**: MySQL 8
- **Build Tool**: Maven

## Prerequisites
- Java 21 installed
- MySQL 8 running locally
- Maven 3.6+ installed

## 🚀 How to Run This Project

### Option 1: Run Using IntelliJ IDEA
1. Clone the repository:
   ```bash
   git clone https://github.com/me-lw-in/Crime-reporting-system-backend.git
2. Open the project in IntelliJ
3. Navigate to:
   src/main/java/com/crime_reporting_system/ReportingSystemApplication.java
4. Right-click the file and click Run or press the green play (▶️) button.

### Option 2: Run Using VS Code or Terminal

1. Clone the repository:
   ```bash
   git clone https://github.com/me-lw-in/Crime-reporting-system-backend.git
2. Navigate to the project directory:
   `cd crime-reporting-system-backend`
3. Configure the `application.properties` file in `src/main/resources` with your MySQL credentials:
   `spring.datasource.url=jdbc:mysql://localhost:3306/<db_name>
   spring.datasource.username=<username>
   spring.datasource.password=<password>`
4. Build the project:
   `mvn clean install`
5. Run the application:
   `mvn spring-boot:run`

## Access the APIs using a tool like Postman or a frontend client.
### APIs
- `POST /api/reports` -> Used by citizens to submit a new crime report with details like title, description, and location.
- `GET /api/reports/pending` -> Allows officers to retrieve a list of all pending reports for review and action.
- `GET /api/reports/rejected` -> Enables officers to fetch a list of rejected reports for administrative purposes.
- `PUT /api/reports/{reportId}` -> Allows officers to update the details of an assigned report (e.g., title, description) if it’s not rejected.
- `GET /api/reports/officers` -> Provides officers with a list of all registered police officers for assignment or management.
- `GET /api/reports/all` -> Enables citizens to view only their own submitted reports, filtered by the logged-in user.
- `POST /api/reports/register` -> Allows new users (citizens or officers) to register with the system by providing their details.
- `POST /api/cases` -> Allows officers to create a new case with an assigned officer and initial status.
- `POST /api/cases/link` -> Enables officers to link an existing report to a case, updating the report’s status and officer.
- `PUT /api/cases/{caseId}` -> Allows officers to update a case’s status (e.g., to "resolved") or reassign it to another officer.
- `GET /api/cases/all` -> Provides officers with a comprehensive list of all cases for linking reports or administrative tasks.
- `GET /api/cases/filter` -> Allows officers to retrieve cases filtered by a specific status (e.g., "investigating").
- `GET /api/cases/dashboard` -> Offers officers a summarized view of all cases with key details and report counts for dashboard display.
- `POST /api/cases/reports/{reportId}/reject` -> Enables officers to reject a report, setting its status to "rejected".
- `POST /api/cases/reports/{reportId}/accept` -> Allows officers to accept a report, optionally linking it to an existing case or creating a new one.

## 🗒️ Notes
- Ensure the MySQL database `crime_db` is created and accessible.
- Use session cookie (e.g., JSESSIONID) for authenticated API calls.
- Customize application.properties as per your local environment.
- Contact the maintainer for further assistance.

## Maintainer
- **Name**: Melwin Mendonca
- **Email**: melwinmanish30@gmail.com