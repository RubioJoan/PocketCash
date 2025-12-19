package com.pocketcash;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class NotificationsWindow extends JFrame {

    private final User user;
    private DefaultTableModel model;
    private JLabel badgeLabel;

    public NotificationsWindow(User user) {
        this.user = user;

        setTitle("Notifications");
        ImageIcon icon = new ImageIcon("title.png");
        setIconImage(icon.getImage());

        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(230, 245, 255)); // window background

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(58, 213, 159));
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton backBtn = new JButton(loadIcon("back.png", 24));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> dispose());

        JLabel title = new JLabel("Notifications", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 14));

        badgeLabel = new JLabel();
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setBackground(Color.RED);
        badgeLabel.setOpaque(true);
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        badgeLabel.setVisible(false);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(badgeLabel);

        topBar.add(backBtn, BorderLayout.WEST);
        topBar.add(title, BorderLayout.CENTER);
        topBar.add(rightPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Date", "Message", "isRead", "senderName"};
        model = new DefaultTableModel(cols, 0);

        JTable table = new JTable(model);
        table.setEnabled(false);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);

        // hide isRead and senderName columns
        table.getColumnModel().getColumn(2).setMinWidth(0);
        table.getColumnModel().getColumn(2).setMaxWidth(0);
        table.getColumnModel().getColumn(2).setWidth(0);

        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setWidth(0);

        // Custom renderer for bold unread messages and row color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                int isRead = Integer.parseInt(table.getModel().getValueAt(row, 2).toString());

                if (isRead == 0) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setBackground(new Color(230, 255, 245)); // unread: light green
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    c.setBackground(new Color(245, 245, 245)); // read: light gray
                }

                setHorizontalAlignment(column == 0
                        ? SwingConstants.CENTER
                        : SwingConstants.LEFT);

                return c;
            }
        });

        // Full message popup
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    String date = table.getValueAt(row, 0).toString();
                    String message = table.getValueAt(row, 1).toString();
                    String senderName = table.getValueAt(row, 3).toString();


                    String senderNumber = "";
                    int idx = message.indexOf("from");
                    if (idx != -1) {
                        senderNumber = message.substring(idx + 5).trim();
                    }

                    JOptionPane.showMessageDialog(
                            NotificationsWindow.this,
                            "<html><b>Date:</b> " + date + "<br>" +
                                    "<b>From:</b> " + senderName +
                                    (senderNumber.isEmpty() ? "" : " (" + senderNumber + ")") + "<br>" +
                                    "<b>Message:</b> " + message + "</html>",
                            "Full Message",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });


        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(230, 245, 255));
        add(scrollPane, BorderLayout.CENTER);

        loadNotifications();
        updateUnreadBadge();
        markAllAsRead(); // hide unread badge

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

    // Load notifications from DB including senderName
    private void loadNotifications() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT date, message, isRead, senderName FROM notifications " +
                            "WHERE mobileNumber=? ORDER BY date DESC"
            );
            ps.setString(1, user.getMobileNumber());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("message"),
                        rs.getInt("isRead"),
                        rs.getString("senderName")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markAllAsRead() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE notifications SET isRead=1 WHERE mobileNumber=?"
            );
            ps.setString(1, user.getMobileNumber());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUnreadBadge() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM notifications WHERE mobileNumber=? AND isRead=0"
            );
            ps.setString(1, user.getMobileNumber());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    badgeLabel.setText(String.valueOf(count));
                    badgeLabel.setVisible(true);
                } else {
                    badgeLabel.setVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
