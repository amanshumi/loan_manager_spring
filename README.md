# Loan Manager Backend Service
This is a Loan Manager application built with Spring Boot. The application allows users to manage loans, including loan applications, approvals, disbursements, repayments, and viewing repayment histories.

## Overview
The Loan Manager application is designed to handle various aspects of loan management, including applying for loans, approving or rejecting applications, disbursing approved loans, recording repayments, and viewing repayment history.

## Prerequisites
- Java 17 or higher
- Maven 3.6.3 or higher
- A database (currently using MySQL, you can change it to any DB of your choice like PostgreSQL etc.)

## Running the Application
1. Clone the repository:
    ```sh
    git clone https://github.com/amanshumi/loan_manager_spring.git
    cd loan_manager_spring
    ```

2. Build the application using Maven:
    ```sh
    mvn clean install
    ```

3. Run the application:
    ```sh
    mvn spring-boot:run
    ```

The application will start on the default port `8000`. You can access it at `http://localhost:8000`.

## Configuration
### Database
I have used MySQL for the database, but you can configure a different database by updating the `application.properties` file located in `src/main/resources`.

### Swagger
Check the swagger ui for testing the endpoints at `http://localhost:8000/swagger-ui/index.html`

Checkout the attached postman collection to test the individual API endpoints.