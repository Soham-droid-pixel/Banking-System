import java.sql.*;
import java.util.Scanner;

public class banking_jdbc {
    private static final String URL = "jdbc:postgresql://localhost:5432/Banking_system";
    private static final String USER = "postgres";
    private static final String PASSWORD = "sand87";  // Change as per your setup

    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static void createTable(Connection conn) {
        String query = """
            CREATE TABLE IF NOT EXISTS customers (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100),
                account_number VARCHAR(20) UNIQUE,
                balance NUMERIC(10,2) CHECK (balance >= 0)
            )
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("✅ 'customers' table created.");
        } catch (SQLException e) {
            System.out.println("❌ Failed to create table.");
        }
    }

    public static void addCustomer(Connection conn, Scanner scanner) {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine();
        System.out.print("Enter initial balance (₹): ");
        double balance = scanner.nextDouble();
        scanner.nextLine();

        String query = "INSERT INTO customers (name, account_number, balance) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, accNum);
            pstmt.setDouble(3, Math.max(balance, 0));
            pstmt.executeUpdate();
            System.out.println("✅ Customer added.");
        } catch (SQLException e) {
            System.out.println("❌ Failed to add customer.");
        }
    }

    public static void updateCustomer(Connection conn, Scanner scanner) {
        System.out.print("Enter account number to update: ");
        String accNum = scanner.nextLine();
        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();
        System.out.print("Enter new balance (₹): ");
        double newBalance = scanner.nextDouble();
        scanner.nextLine();

        String query = "UPDATE customers SET name = ?, balance = ? WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newName);
            pstmt.setDouble(2, newBalance);
            pstmt.setString(3, accNum);
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "✅ Customer updated." : "❌ Account not found.");
        } catch (SQLException e) {
            System.out.println("❌ Update failed.");
        }
    }

    public static void deleteCustomer(Connection conn, Scanner scanner) {
        System.out.print("Enter account number to delete: ");
        String accNum = scanner.nextLine();

        String query = "DELETE FROM customers WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accNum);
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "✅ Customer deleted." : "❌ Account not found.");
        } catch (SQLException e) {
            System.out.println("❌ Deletion failed.");
        }
    }

    public static void viewCustomers(Connection conn) {
        String query = "SELECT id, name, account_number, CONCAT('₹', balance) AS balance FROM customers";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n🔍 Customers:");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Acc#: %s | Balance: %s\n",
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("account_number"), rs.getString("balance"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to fetch customers.");
        }
    }

    public static void withdrawAmount(Connection conn, Scanner scanner) {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine();
        System.out.print("Enter amount to withdraw (₹): ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        String query = "SELECT balance FROM customers WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accNum);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                if (amount > balance) {
                    System.out.println("⚠️ Withdrawal exceeds balance. Bank permission required.");
                } else {
                    String updateQuery = "UPDATE customers SET balance = balance - ? WHERE account_number = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setDouble(1, amount);
                        updateStmt.setString(2, accNum);
                        updateStmt.executeUpdate();
                        System.out.println("✅ Amount withdrawn.");
                    }
                }
            } else {
                System.out.println("❌ Account not found.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Withdrawal failed.");
        }
    }

    public static void calculateSimpleInterest(Scanner scanner) {
        System.out.print("Enter principal (₹): ");
        double principal = scanner.nextDouble();
        System.out.print("Enter rate of interest (%): ");
        double rate = scanner.nextDouble();
        System.out.print("Enter time (years): ");
        double time = scanner.nextDouble();

        double si = (principal * rate * time) / 100;
        System.out.printf("✅ Simple Interest: ₹%.2f\n", si);
    }

    public static void calculateRecurringDeposit(Scanner scanner) {
        System.out.print("Enter monthly deposit (₹): ");
        double deposit = scanner.nextDouble();
        System.out.print("Enter rate of interest (%): ");
        double rate = scanner.nextDouble();
        System.out.print("Enter time (months): ");
        int time = scanner.nextInt();

        double maturityAmount = deposit * time + (deposit * time * (time + 1) * rate) / (2 * 12 * 100);
        System.out.printf("✅ Maturity Amount: ₹%.2f\n", maturityAmount);
    }

    public static void calculateCompoundInterest(Scanner scanner) {
        System.out.print("Enter principal (₹): ");
        double principal = scanner.nextDouble();
        System.out.print("Enter rate of interest (%): ");
        double rate = scanner.nextDouble();
        System.out.print("Enter time (years): ");
        double time = scanner.nextDouble();
        System.out.print("Enter number of times interest applied per year: ");
        int n = scanner.nextInt();

        double amount = principal * Math.pow(1 + (rate / (100 * n)), n * time);
        System.out.printf("✅ Compound Interest: ₹%.2f\n", amount - principal);
    }

    public static void main(String[] args) {
        Connection conn = connect();
        if (conn != null) {
            createTable(conn);
            Scanner scanner = new Scanner(System.in);
            int choice;

            do {
                System.out.println("\n📋 Menu:");
                System.out.println("1️⃣ Add Customer");
                System.out.println("2️⃣ View Customers");
                System.out.println("3️⃣ Update Customer");
                System.out.println("4️⃣ Delete Customer");
                System.out.println("5️⃣ Withdraw Amount");
                System.out.println("6️⃣ Simple Interest");
                System.out.println("7️⃣ Recurring Deposit");
                System.out.println("8️⃣ Compound Interest");
                System.out.println("0️⃣ Exit");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> addCustomer(conn, scanner);
                    case 2 -> viewCustomers(conn);
                    case 3 -> updateCustomer(conn, scanner);
                    case 4 -> deleteCustomer(conn, scanner);
                    case 5 -> withdrawAmount(conn, scanner);
                    case 6 -> calculateSimpleInterest(scanner);
                    case 7 -> calculateRecurringDeposit(scanner);
                    case 8 -> calculateCompoundInterest(scanner);
                    case 0 -> System.out.println("👋 Exiting.");
                    default -> System.out.println("❌ Invalid choice.");
                }
            } while (choice != 0);
        }
    }
}
