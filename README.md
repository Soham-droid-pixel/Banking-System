# 💳 Banking System (Java + Swing + PostgreSQL)

A simple yet functional **Banking System GUI application** built using **Java Swing** and **PostgreSQL**. It allows you to manage customer banking records, perform basic operations such as adding, updating, and deleting customer details, calculating interest, and handling withdrawals.

---

## 🚀 Features

* Add, update, and delete customer details
* Display customer list in a table view
* Withdraw money from a customer account
* Calculate **Simple Interest** and **Compound Interest**
* JDBC integration with **PostgreSQL**
* Smooth and interactive GUI using **Java Swing**

---

## 🖼️ GUI Overview

The interface includes:

* Customer Details input panel
* Functional buttons (Add, Update, Delete, Withdraw)
* Dynamic JTable to display customers
* Operations panel for interest calculations and exit

---

## 🛠️ Technologies Used

* **Java** (Swing for GUI)
* **PostgreSQL** (Database)
* **JDBC** (Database connectivity)
* **Eclipse IDE** (Development Environment)

---

## 🧾 Database Setup

Create a PostgreSQL database called `Banking_system`, and run the following SQL to create the necessary table:

```sql
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance NUMERIC(10,2) NOT NULL
);
```

> 📌 **Important:** Update the database credentials in the code as needed:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/Banking_system";
private static final String USER = "postgres";
private static final String PASSWORD = "your_password_here";
```

---

## 🔧 How to Run

1. Make sure PostgreSQL is installed and running.
2. Create the database and table as described above.
3. Clone this repository or copy the code into your IDE.
4. Compile and run the `banking.java` file.
5. The GUI will appear. Start managing customers!

---

## 📦 Folder Structure

```
banking/
│
├── banking.java            # Main Java file with GUI + database logic
├── README.md               # Project documentation
```

---

## 🧠 Future Enhancements

* Transaction history for each account
* Login authentication (Admin/Employee)
* Export reports to PDF/CSV
* Pagination and search in customer list

---
