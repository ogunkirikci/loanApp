# Loan Management API

A Spring Boot application that enables bank employees to manage customer loans, including creation, listing, and payment processing.

## Technologies

- Java 11
- Spring Boot 2.7.x
- Spring Security
- H2 Database
- Lombok
- Maven

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/ogunkirikci/loanApp
```

2. Build the project:
```bash
mvn clean install
```
3. Run the application:
```bash
nvm spring-boot:run
```

# API Reference

## Customer Operations

### Create Customer

```http
POST /api/customers
Authorization: Basic admin password
```

| Parameter | Type  | Description                |
| :-------- | :---- | :------------------------- |
| `name`    | `string`| **Required**. Customer Name |
| `surname`    | `string`| **Required**. Customer Surname |
| `creditLimit`    | `integer`| **Required**. Customer's Credit Limit |
| `usedCreditLimit`    | `integer`| **Required**. Customer's Used Credit Limit |

#### Request Example

**URL:**  
`http://localhost:8080/api/customers`

**Authorization:**  
`Authorization: Basic admin password`

**Body (JSON):**  

```json
{
    "name": "Ogün",
    "surname": "Kırıkçı",
    "creditLimit": 500000,
    "usedCreditLimit": 0
}
```

#### Example cURL Command

```bash
curl -X POST http://localhost:8080/api/customers \
-H "Authorization: Basic admin password" \
-H "Content-Type: application/json" \
-d '{
    "name": "Ogün",
    "surname": "Kırıkçı",
    "creditLimit": 500000,
    "usedCreditLimit": 0
}'
```

## Loan Operations

### Create Loan

```http
POST /api/loans
```

| Parameter       | Type     | Description                         |
| :-------------- | :------- | :---------------------------------- |
| `customerId`    | `integer`| **Required**. ID of the customer    |
| `amount`        | `number` | **Required**. Loan amount           |
| `interestRate`  | `number` | **Required**. Interest rate         |
| `numberOfInstallments` | `integer` | **Required**. Number of installments |

#### Request Example

**URL:**  
`http://localhost:8080/api/loans`

**Body (JSON):**  

```json
{
    "customerId": 1,
    "amount": 10000,
    "interestRate": 0.2,
    "numberOfInstallments": 12
}
```

### Get Loan Installments

```http
GET /api/loans/{id}/installments
```

#### Response Example

```json
[
    {
        "id": 1,
        "amount": 1000.00,
        "paidAmount": 0.00,
        "dueDate": "2025-01-01",
        "paymentDate": null,
        "paid": false
    },
    ...
]
```

### Get Loans by Customer

```http
GET /api/loans/customer/{customerId}
```

#### Response Example

```json
[
    {
        "id": 1,
        "loanAmount": 10000.00,
        "remainingAmount": 12000.00,
        "numberOfInstallments": 12,
        "createDate": "2024-12-23T17:38:57.084146",
        "paid": false
    }
]
```

### Pay Loan

```http
POST /api/loans/pay
```

| Parameter | Type  | Description              |
| :-------- | :---- | :----------------------- |
| `loanId`  | `integer` | **Required**. Loan ID   |
| `amount`  | `number` | **Required**. Amount to pay |

#### Request Example

**Body (JSON):**  

```json
{
    "loanId": 1,
    "amount": 3000
}
```

### Get Loan Payment History

```http
GET /api/loans/{id}/history
```

#### Response Example

```json
[
    {
        "transactionDate": "2024-12-23T00:00:00",
        "transactionType": "PAYMENT",
        "amount": 991.00,
        "remainingDebt": 9000.00,
        "description": "Early payment with discount"
    },
    ...
]
```

### Get Loan Payment Plan

```http
GET /api/loans/{id}/payment-plan
```

#### Response Example

```json
[
    {
        "installmentNumber": 1,
        "dueDate": "2025-01-01",
        "installmentAmount": 1000.00,
        "principalAmount": 833.33,
        "interestAmount": 166.67,
        "remainingPrincipal": 10000.00,
        "paid": true
    },
    ...
]
```

### Get Customer Risk Analysis

```http
GET /api/customers/{customerId}/risk-analysis
```

#### Response Example

```json
{
    "customerId": 1,
    "riskLevel": "LOW",
    "totalDebt": 9000.00,
    "unusedCreditLimit": 490000.00,
    "activeLoans": 1,
    "latePayments": 0,
    "creditScore": 91.0,
    "recommendation": "Customer is eligible for new credit applications."
}
```

### Early Loan Closure Calculation

```http
GET /api/loans/{id}/early-closure-calculation
```

#### Response Example

```json
{
    "earlyClosureAmount": 9500.00,
    "discount": 500.00
}
```


## Business Rules

1. Loan Creation Rules:
   - Number of installments must be 6, 9, 12, or 24
   - Interest rate must be between 0.1 and 0.5
   - Customer credit limit is checked
   - All installments must be equal
   - First installment date is the first day of next month

2. Payment Rules:
   - Installments must be paid in full
   - Oldest due installment is paid first
   - Cannot pay installments due more than 3 months ahead
   - Early payment discount: amount * 0.001 * (days paid early)
   - Late payment penalty: amount * 0.001 * (days overdue)

3. Security:
   - All endpoints are protected with Basic Auth
   - Only users with ADMIN role can access

# Database Schema

## Customer Table

| Column          | Type         | Constraints        |
|------------------|--------------|--------------------|
| `id`            | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| `name`          | VARCHAR(255) | NOT NULL           |
| `surname`       | VARCHAR(255) | NOT NULL           |
| `creditLimit`   | DECIMAL(15, 2) | NOT NULL         |
| `usedCreditLimit` | DECIMAL(15, 2) | NOT NULL       |

## Loan Table

| Column              | Type         | Constraints                      |
|----------------------|--------------|----------------------------------|
| `id`                | BIGINT       | PRIMARY KEY, AUTO_INCREMENT      |
| `customerId`        | BIGINT       | NOT NULL, FOREIGN KEY REFERENCES `Customer(id)` |
| `loanAmount`        | DECIMAL(15, 2) | NOT NULL                       |
| `numberOfInstallment` | INT          | NOT NULL                       |
| `createDate`        | DATETIME     | NOT NULL                        |
| `isPaid`            | BOOLEAN      | NOT NULL                        |

## LoanInstallment Table

| Column          | Type         | Constraints                      |
|------------------|--------------|----------------------------------|
| `id`            | BIGINT       | PRIMARY KEY, AUTO_INCREMENT      |
| `loanId`        | BIGINT       | NOT NULL, FOREIGN KEY REFERENCES `Loan(id)` |
| `amount`        | DECIMAL(15, 2) | NOT NULL                       |
| `paidAmount`    | DECIMAL(15, 2) | NOT NULL                       |
| `dueDate`       | DATE         | NOT NULL                        |
| `paymentDate`   | DATE         | NULLABLE                        |
| `isPaid`        | BOOLEAN      | NOT NULL                        |


# Development Environment

## 1. H2 Console Access

| Property     | Value                           |
|--------------|---------------------------------|
| URL          | `http://localhost:8080/h2-console` |
| JDBC URL     | `jdbc:h2:mem:testdb`           |
| Username     | `sa`                           |
| Password     | `password`                     |


# Error Codes

| Code | Description                              |
|------|------------------------------------------|
| 400  | Bad Request - Invalid request parameters |
| 401  | Unauthorized - Authentication failure    |
| 404  | Not Found - Resource not found           |
| 500  | Internal Server Error - Server-side error |

# Testing

To run the tests, use the following command:

```bash
mvn test
```
