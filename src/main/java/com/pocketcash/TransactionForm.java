package com.pocketcash;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TransactionForm extends JFrame {

    private User user;
    private ClientDashboard dashboard; // reference to refresh dashboard
    private String type;

    public TransactionForm(User user, String type, ClientDashboard dashboard) {
        this.user = user;
        this.type = type;
        this.dashboard = dashboard;

        setTitle("PocketCash - " + type);
        setSize(300, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField amountField = new JTextField();
        JButton confirmBtn = new JButton(type);

        confirmBtn.addActionListener(e -> performTransaction(amountField.getText()));

        panel.add(new JLabel(type + " Amount:"));
        panel.add(amountField);
        panel.add(confirmBtn);

        add(panel);
        setVisible(true);
    }

    private void performTransaction(String amountText) {
        if (amountText == null || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount");
            return;
        }

        try {
            double amount = Integer.parseInt(amountText);
            double newBalance = type.equalsIgnoreCase("Deposit") ? user.getBalance() + amount
                    : user.getBalance() - amount;

            if (type.equalsIgnoreCase("Withdraw") && amount > user.getBalance()) {
                JOptionPane.showMessageDialog(this, "Insufficient balance!");
                return;
            }

            Connection conn = DatabaseConnection.getConnection();

            // ================= UPDATE USER BALANCE =================
            String sql = "UPDATE users SET balance=? WHERE mobileNumber=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newBalance);
            stmt.setString(2, user.getMobileNumber());
            stmt.executeUpdate();

            // ================= LOG TRANSACTION =================
            String transactionSQL = "INSERT INTO transactions(mobileNumber, type, amount, date) VALUES (?, ?, ?, NOW())";
            PreparedStatement tsmt = conn.prepareStatement(transactionSQL);
            tsmt.setString(1, user.getMobileNumber());
            tsmt.setString(2, type);
            tsmt.setDouble(3, amount);
            tsmt.executeUpdate();

            // ================= UPDATE LOCAL USER & DASHBOARD =================
            user.setBalance(newBalance);
            if (dashboard != null) {
                dashboard.refreshBalanceAndTransactions();
            }

            JOptionPane.showMessageDialog(this, type + " successful! New balance: ₱" + newBalance);
            dispose();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
