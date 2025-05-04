import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class banking extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField nameField, accNumField, balanceField;
    private JTable table;
    private DefaultTableModel tableModel;

    // Database Config
    private static final String URL = "jdbc:postgresql://localhost:5432/Banking_system";
    private static final String USER = "postgres";
    private static final String PASSWORD = "sand87";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                banking frame = new banking();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public banking() {
        setTitle("Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(10, 10));

        // TOP PANEL (Input Fields + Buttons)
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Account Number:"));
        accNumField = new JTextField();
        inputPanel.add(accNumField);

        inputPanel.add(new JLabel("Balance (₹):"));
        balanceField = new JTextField();
        inputPanel.add(balanceField);

        JButton addBtn = new JButton("Add Customer");
        JButton updateBtn = new JButton("Update Customer");
        JButton deleteBtn = new JButton("Delete Customer");
        JButton withdrawBtn = new JButton("Withdraw Amount");

        inputPanel.add(addBtn);
        inputPanel.add(updateBtn);
        inputPanel.add(deleteBtn);
        inputPanel.add(withdrawBtn);

        contentPane.add(inputPanel, BorderLayout.NORTH);

        // TABLE (To display customer data)
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Account Number", "Balance"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Customer List"));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // BOTTOM PANEL (Interest Calculation + Exit)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        JButton simpleInterestBtn = new JButton("Simple Interest");
        JButton compoundInterestBtn = new JButton("Compound Interest");
        JButton exitBtn = new JButton("Exit");

        bottomPanel.add(simpleInterestBtn);
        bottomPanel.add(compoundInterestBtn);
        bottomPanel.add(exitBtn);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        // Load Customers from Database on Start
        loadCustomers();

        // Action Listeners
        addBtn.addActionListener(e -> addCustomer());
        updateBtn.addActionListener(e -> updateCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        withdrawBtn.addActionListener(e -> withdrawAmount());
        simpleInterestBtn.addActionListener(e -> calculateSimpleInterest());
        compoundInterestBtn.addActionListener(e -> calculateCompoundInterest());
        exitBtn.addActionListener(e -> System.exit(0));

        // Table Selection Listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    accNumField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    balanceField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                }
            }
        });
    }

    // Connect to PostgreSQL
    private Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection Failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    // Load Customers into Table
    private void loadCustomers() {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM customers";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("account_number"),
                    rs.getDouble("balance")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add Customer
    private void addCustomer() {
        String name = nameField.getText().trim();
        String accNum = accNumField.getText().trim();
        String balanceText = balanceField.getText().trim();

        if (name.isEmpty() || accNum.isEmpty() || balanceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double balance = Double.parseDouble(balanceText);
            String query = "INSERT INTO customers (name, account_number, balance) VALUES (?, ?, ?)";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, accNum);
                pstmt.setDouble(3, balance);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                loadCustomers();
                clearFields();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid balance value!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to add customer!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update Customer
    private void updateCustomer() {
        String name = nameField.getText().trim();
        String accNum = accNumField.getText().trim();
        String balanceText = balanceField.getText().trim();

        if (name.isEmpty() || accNum.isEmpty() || balanceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double balance = Double.parseDouble(balanceText);
            String query = "UPDATE customers SET name = ?, balance = ? WHERE account_number = ?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, balance);
                pstmt.setString(3, accNum);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Customer updated!");
                    loadCustomers();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Account not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid balance value!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete Customer
    private void deleteCustomer() {
        String accNum = accNumField.getText().trim();

        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "DELETE FROM customers WHERE account_number = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accNum);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Customer deleted!");
                loadCustomers();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Account not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Withdraw Amount
    private void withdrawAmount() {
        String accNum = accNumField.getText().trim();
        String amountText = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");

        if (accNum.isEmpty() || amountText == null || amountText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number and amount are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String query = "UPDATE customers SET balance = balance - ? WHERE account_number = ?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, accNum);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Amount withdrawn!");
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Account not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount value!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Calculate Simple Interest
    private void calculateSimpleInterest() {
        try {
            double principal = Double.parseDouble(JOptionPane.showInputDialog("Enter Principal:"));
            double rate = Double.parseDouble(JOptionPane.showInputDialog("Enter Rate of Interest:"));
            double time = Double.parseDouble(JOptionPane.showInputDialog("Enter Time (years):"));

            double interest = (principal * rate * time) / 100;
            JOptionPane.showMessageDialog(this, "Simple Interest: ₹" + interest);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Calculate Compound Interest
    private void calculateCompoundInterest() {
        try {
            double principal = Double.parseDouble(JOptionPane.showInputDialog("Enter Principal:"));
            double rate = Double.parseDouble(JOptionPane.showInputDialog("Enter Rate of Interest:"));
            double time = Double.parseDouble(JOptionPane.showInputDialog("Enter Time (years):"));

            double amount = principal * Math.pow(1 + (rate / 100), time);
            JOptionPane.showMessageDialog(this, "Compound Interest: ₹" + (amount - principal));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clear Input Fields
    private void clearFields() {
        nameField.setText("");
        accNumField.setText("");
        balanceField.setText("");
    }
}