package com.pocketcash;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class TransactionsWindow extends JFrame {

    private final User user;
    private DefaultTableModel transactionsModel;
    private JTable transactionsTable;

    public TransactionsWindow(User user) {
        this.user = user;

        setTitle("PocketCash - All Transactions");
        ImageIcon icon = new ImageIcon("title.png");
        setIconImage(icon.getImage());
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Main panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(58, 213, 159));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 15));

        // Title
        JLabel title = new JLabel("All Transactions", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"Date", "Type", "Amount"};
        transactionsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent editing
            }
        };
        transactionsTable = new JTable(transactionsModel);

        // Table styling
        transactionsTable.setBackground(new Color(200, 255, 240)); // light green background
        transactionsTable.setRowHeight(30);
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(Color.LIGHT_GRAY);

        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < transactionsTable.getColumnCount(); i++) {
            transactionsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Scrollable table
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        loadAllTransactions();
        setVisible(true);
    }

    private void loadAllTransactions() {
        transactionsModel.setRowCount(0); // clear table

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT date, type, amount FROM transactions WHERE mobileNumber=? ORDER BY date DESC");
            stmt.setString(1, user.getMobileNumber());
            ResultSet rs = stmt.executeQuery();

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            while (rs.next()) {
                String date = rs.getString("date");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");

                transactionsModel.addRow(new Object[]{
                        date,
                        type,
                        nf.format(amount)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
    }
}
