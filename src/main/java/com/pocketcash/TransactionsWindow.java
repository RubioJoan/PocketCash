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

        // top bar (back + title)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        // back button
        JButton backBtn = new JButton(loadIcon("back.png", 24));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> dispose());

        // Title
        JLabel title = new JLabel("All Transactions", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        topBar.add(backBtn, BorderLayout.WEST);
        topBar.add(title, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);



        // Table setup
        String[] cols = {"Date", "Type", "Amount"};
        transactionsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent editing
            }
        };
        transactionsTable = new JTable(transactionsModel);

        // Table style
        transactionsTable.setBackground(new Color(200, 255, 240)); // light green
        transactionsTable.setRowHeight(30);
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(Color.LIGHT_GRAY);

        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < transactionsTable.getColumnCount(); i++) {
            transactionsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Pop-up full transaction
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = transactionsTable.rowAtPoint(evt.getPoint());
                if (row >= 0) {

                    String date = transactionsTable.getValueAt(row, 0).toString();
                    String type = transactionsTable.getValueAt(row, 1).toString();
                    String amount = transactionsTable.getValueAt(row, 2).toString();

                    String details = buildTransactionMessage(type, amount);

                    JOptionPane.showMessageDialog(
                            TransactionsWindow.this,
                            "<html><b>Date:</b> " + date + "<br>" +
                                    "<b>Details:</b><br>" + "You " + details + "</html>",
                            "Transaction Details",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });


        // Scrollable table
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        loadAllTransactions();
        setVisible(true);
    }

    private ImageIcon loadIcon(String name, int size) {
        java.net.URL url = getClass().getResource("/" + name);
        if (url == null) return new ImageIcon();
        Image img = new ImageIcon(url)
                .getImage()
                .getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
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
    private String buildTransactionMessage(String type, String amount) {

        // SEND MONEY
        if (type.startsWith("Send Money to:")) {

            String mobile = type.replace("Send Money to:", "").trim();
            String name = getUserNameByMobile(mobile);

            return "You sent <b>" + amount + "</b> to<br>"
                    + name + " (" + mobile + ")";

        }

        // RECEIVE MONEY
        if (type.startsWith("Receive Money from:")) {

            String mobile = type.replace("Receive Money from:", "").trim();
            String name = getUserNameByMobile(mobile);

            return "You received <b>" + amount + "</b> from<br>"
                    + name + " (" + mobile + ")";
        }

        // WITHDRAW
        if (type.equalsIgnoreCase("Withdraw")) {
            return "You withdrew <b>" + amount + "</b>";
        }

        // CASH IN / DEPOSIT
        if (type.equalsIgnoreCase("Deposit")) {
            return "You deposited <b>" + amount + "</b>";
        }

        return type + " " + amount;
    }
    private String getUserNameByMobile(String mobile) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT name FROM users WHERE mobileNumber=?"
            );
            ps.setString(1, mobile);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }


}
