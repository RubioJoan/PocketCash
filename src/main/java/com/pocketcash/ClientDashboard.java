package com.pocketcash;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ClientDashboard extends JFrame {

    private final User user;
    private JLabel balanceLabel;
    private DefaultTableModel transactionsModel;
    private JTable transactionsTable;
    private JLabel notificationDot;
    private JLabel notificationBadge;

    private static final Color APP_BG = new Color(58, 213, 159);

    public ClientDashboard(User user) {
        this.user = user;

        setTitle("PocketCash - Home");
        ImageIcon icon = new ImageIcon("title.png");
        setIconImage(icon.getImage());
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BG);

        add(headerPanel(), BorderLayout.NORTH);
        add(centerPanel(), BorderLayout.CENTER);
        add(bottomNav(), BorderLayout.SOUTH);

        setVisible(true);

        refreshBalanceAndTransactions();
    }

    // header
    private JPanel headerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(APP_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JLabel welcome = new JLabel("Welcome back, " + user.getName());
        welcome.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 22));

        panel.add(welcome, BorderLayout.WEST);
        return panel;
    }

    // center
    private JPanel centerPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(roundedUserCard());
        panel.add(Box.createVerticalStrut(15));
        panel.add(actionButtons());
        panel.add(Box.createVerticalStrut(8));
        panel.add(dividerLine());
        panel.add(transactionsSection());

        return panel;
    }

    //user card
    private JPanel roundedUserCard() {
        RoundedPanel card = new RoundedPanel(25);
        card.setBackground(APP_BG);
        card.setBorderColor(Color.BLACK);
        card.setBorderWidth(1);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(360, 160));
        card.setMaximumSize(new Dimension(360, 160));

        //balancePanel
        JPanel balancePanel = new JPanel();
        balancePanel.setOpaque(false);
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));

        JLabel balTitle = new JLabel("AVAILABLE BALANCE");
        balTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        balTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        String formattedBalance = nf.format(user.getBalance());

        balanceLabel = new JLabel(formattedBalance);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        balancePanel.add(balTitle);
        balancePanel.add(Box.createVerticalStrut(5));
        balancePanel.add(balanceLabel);

        card.add(balancePanel, BorderLayout.NORTH);

        //logo and user info
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        // Logo
        JLabel logo = new JLabel(loadIcon("logo.png", 80));
        mainPanel.add(logo);
        mainPanel.add(Box.createHorizontalStrut(30));

        // line
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 1, getHeight());
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(1, 90));
        mainPanel.add(divider);
        mainPanel.add(Box.createHorizontalStrut(10));

        // User info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("Name: " + user.getName());
        JLabel mobile = new JLabel("Mobile No.: " + user.getMobileNumber());
        JLabel email = new JLabel("Email: " + user.getEmail());

        name.setFont(new Font("Arial", Font.BOLD, 14));
        mobile.setFont(new Font("Arial", Font.PLAIN, 12));
        email.setFont(new Font("Arial", Font.PLAIN, 12));

        info.add(name);
        info.add(Box.createVerticalStrut(8));
        info.add(mobile);
        info.add(Box.createVerticalStrut(8));
        info.add(email);

        mainPanel.add(info);
        card.add(mainPanel, BorderLayout.CENTER);

        return card;
    }

    //action buttons
    private JPanel actionButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 5, 10));

        panel.add(iconWithText("cashin.png", "Cash in", e -> new TransactionForm(user, "Deposit", this)));
        panel.add(iconWithText("withdraw.png", "Withdraw", e -> new TransactionForm(user, "Withdraw", this)));
        panel.add(iconWithText("send.png", "Send Money", this::sendMoney));
        panel.add(iconWithText("transactions.png", "Transactions", e -> new TransactionsWindow(user)));

        // noti
        JPanel notifPanel = iconWithText("notification.png", "Notifications", e -> {
            new NotificationsWindow(user);
            updateNotificationBadge(); // auto hide when opened
        });


        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(notifPanel.getPreferredSize());

        notifPanel.setBounds(0, 0, notifPanel.getPreferredSize().width, notifPanel.getPreferredSize().height);
        layered.add(notifPanel, Integer.valueOf(0));

        notificationBadge = new JLabel();
        notificationBadge.setOpaque(true);
        notificationBadge.setBackground(Color.RED);
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setFont(new Font("Arial", Font.BOLD, 10));
        notificationBadge.setHorizontalAlignment(SwingConstants.CENTER);
        notificationBadge.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        notificationBadge.setBounds(notifPanel.getWidth() - 18, 0, 18, 18);
        layered.add(notificationBadge, Integer.valueOf(2));


        notificationDot = new JLabel("\u2B24"); // small circle ●
        notificationDot.setForeground(Color.RED);
        notificationDot.setFont(new Font("Arial", Font.PLAIN, 16));
        notificationDot.setVisible(hasUnreadNotifications());
        notificationDot.setBounds(notifPanel.getWidth() - 10, 0, 16, 16); // top-right corner
        layered.add(notificationDot, Integer.valueOf(1));

        panel.add(layered);

        updateNotificationBadge();

        return panel;
    }

    private JPanel iconWithText(String icon, String text, ActionListener action) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JButton btn = new JButton(loadIcon(icon, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(action);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(btn);
        p.add(lbl);
        return p;
    }

    private boolean hasUnreadNotifications() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM notifications WHERE mobileNumber=? AND isRead=0");
            stmt.setString(1, user.getMobileNumber());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getUnreadNotificationCount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM notifications WHERE mobileNumber=? AND isRead=0");
            stmt.setString(1, user.getMobileNumber());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateNotificationBadge() {
        int unread = getUnreadNotificationCount();

        if (notificationBadge != null) {
            notificationBadge.setText(String.valueOf(unread));
            notificationBadge.setVisible(unread > 0);
        }

        if (notificationDot != null) {
            notificationDot.setVisible(unread > 0);
        }
    }

    private JComponent dividerLine() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.BLACK);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    //trans details
    private JPanel transactionsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Arial", Font.BOLD, 14));

        String[] cols = {"Date", "Details", "Amount"};
        transactionsModel = new DefaultTableModel(cols, 0);
        transactionsTable = new JTable(transactionsModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < transactionsTable.getColumnCount(); i++) {
            transactionsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        transactionsTable.setEnabled(false);
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(Color.LIGHT_GRAY);
        transactionsTable.setRowHeight(25);
        transactionsTable.setBackground(APP_BG);

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        scrollPane.setPreferredSize(new Dimension(0, 150));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // refresh balance and trans
    public void refreshBalanceAndTransactions() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        balanceLabel.setText(nf.format(user.getBalance()));

        loadTransactions();
        updateNotificationBadge();

    }

    private void loadTransactions() {
        transactionsModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT date, type, amount FROM transactions WHERE mobileNumber=? ORDER BY date DESC LIMIT 10");
            stmt.setString(1, user.getMobileNumber());
            ResultSet rs = stmt.executeQuery();

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            while (rs.next()) {
                transactionsModel.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("type"),
                        nf.format(rs.getDouble("amount"))
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send money
    private void sendMoney(ActionEvent e) {

        // panel form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField mobileField = new JTextField();
        JTextField amountField = new JTextField();

        mobileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        form.add(new JLabel("Recipient Mobile Number(9 digits only)"));
        form.add(mobileField);
        mobileField.setToolTipText("Enter last 9 digits only (e.g. 912345678)");
        form.add(Box.createVerticalStrut(10));

        form.add(new JLabel("Amount"));
        form.add(amountField);

        // custom icon
        ImageIcon icon = loadIcon("send.png", 48);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "PocketCash - Send Money",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                icon
        );

        if (result != JOptionPane.OK_OPTION) return;

        String recipientInput = mobileField.getText().trim();
        String amtStr = amountField.getText().trim();

        if (recipientInput.isEmpty() || amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        String recipientMobile = "+639-" + recipientInput;

        try {
            double amount = Double.parseDouble(amtStr);

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid amount!");
                return;
            }

            if (amount > user.getBalance()) {
                JOptionPane.showMessageDialog(this, "Insufficient balance!");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {

                // check recipient
                PreparedStatement check = conn.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE mobileNumber=?");
                check.setString(1, recipientMobile);
                ResultSet rs = check.executeQuery();
                rs.next();

                if (rs.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(this, "Recipient does not exist!");
                    return;
                }

                // deduct sender
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE users SET balance = balance - ? WHERE mobileNumber=?");
                ps1.setDouble(1, amount);
                ps1.setString(2, user.getMobileNumber());
                ps1.executeUpdate();

                // sender transaction
                PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO transactions(mobileNumber, type, amount, date) VALUES (?, ?, ?, NOW())");
                ps2.setString(1, user.getMobileNumber());
                ps2.setString(2, "Sent to " + recipientMobile);
                ps2.setDouble(3, amount);
                ps2.executeUpdate();

                // add to recipient
                PreparedStatement ps3 = conn.prepareStatement(
                        "UPDATE users SET balance = balance + ? WHERE mobileNumber=?");
                ps3.setDouble(1, amount);
                ps3.setString(2, recipientMobile);
                ps3.executeUpdate();

                // recipient transaction
                PreparedStatement ps4 = conn.prepareStatement(
                        "INSERT INTO transactions(mobileNumber, type, amount, date) VALUES (?, ?, ?, NOW())");
                ps4.setString(1, recipientMobile);
                ps4.setString(2, "Received from " + user.getMobileNumber());
                ps4.setDouble(3, amount);
                ps4.executeUpdate();

                // notification
                PreparedStatement psNotify = conn.prepareStatement(
                        "INSERT INTO notifications(mobileNumber, senderName, message, isRead) VALUES (?, ?, ?, 0)");
                psNotify.setString(1, recipientMobile);
                psNotify.setString(2, user.getName());
                psNotify.setString(3,
                        "You received ₱" + amount + " from " + user.getMobileNumber());
                psNotify.executeUpdate();
            }

            user.setBalance(user.getBalance() - amount);
            refreshBalanceAndTransactions();

            JOptionPane.showMessageDialog(this, "Money sent successfully!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }


    // bot nav
    private JPanel bottomNav() {
        JPanel panel = new JPanel(new GridLayout(1, 4));
        panel.setBackground(APP_BG);

        JButton settingsBtn = navIcon("settings.png");
        settingsBtn.addActionListener(e -> new SettingsWindow(user));
        panel.add(settingsBtn);

        panel.add(navIcon("home.png"));
        panel.add(navIcon("profile.png"));

        JButton logout = navIcon("logout.png");
        logout.addActionListener(e -> {
            dispose();
            new MainDashboard();
        });
        panel.add(logout);

        return panel;
    }

    private JButton navIcon(String icon) {
        JButton btn = new JButton(loadIcon(icon, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        return btn;
    }

    // icon loader
    private ImageIcon loadIcon(String name, int size) {
        java.net.URL url = getClass().getResource("/" + name);
        if (url == null) return new ImageIcon();
        Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // rounded panel
    static class RoundedPanel extends JPanel {
        private final int radius;
        private Color borderColor = Color.BLACK;
        private int borderWidth = 1;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public void setBorderColor(Color color) { this.borderColor = color; }
        public void setBorderWidth(int width) { this.borderWidth = width; }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(borderWidth / 2, borderWidth / 2,
                    getWidth() - borderWidth, getHeight() - borderWidth,
                    radius, radius);
        }
    }

}
