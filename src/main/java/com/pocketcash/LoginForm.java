package com.pocketcash;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {

    private final String MOBILE_PREFIX = "+639-";
    private JTextField mobileField;
    private JPasswordField pinField;
    private JButton loginBtn;
    private JDialog keypadDialog;

    public LoginForm() {
        setTitle("PocketCash - LOG IN");
        setIconImage(new ImageIcon("title.png").getImage());
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== MAIN PANEL =====
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(58, 213, 159)); // green background
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));

        // ===== BACK BUTTON (left-aligned) =====
        ImageIcon backIcon = new ImageIcon("aa.png");
        Image scaledBack = backIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        backIcon = new ImageIcon(scaledBack);
        JButton backBtn = new JButton(backIcon);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            dispose();
            new MainDashboard();
        });
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backPanel.setOpaque(false);
        backPanel.add(backBtn);
        mainPanel.add(backPanel);
        mainPanel.add(Box.createVerticalStrut(5));

        // ===== LOGO =====
        ImageIcon logoIcon = new ImageIcon("logo.png");
        Image scaledLogo = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== WELCOME =====
        JLabel welcome = new JLabel("Welcome back!");
        welcome.setFont(new Font("Arial", Font.BOLD, 18));
        welcome.setForeground(Color.WHITE);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== MOBILE FIELD =====
        mobileField = new JTextField(MOBILE_PREFIX);
        mobileField.setHorizontalAlignment(JTextField.CENTER);
        mobileField.setFont(new Font("Arial", Font.PLAIN, 14));
        mobileField.setBorder(BorderFactory.createTitledBorder("Mobile Number"));
        mobileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        mobileField.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showKeypadBelow(mobileField, 9);
            }
        });
        mobileField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
                if (mobileField.getText().length() >= MOBILE_PREFIX.length() + 9) e.consume();
            }
        });

        // ===== MPIN FIELD =====
        pinField = new JPasswordField();
        pinField.setEditable(false);
        pinField.setHorizontalAlignment(JPasswordField.CENTER);
        pinField.setFont(new Font("Arial", Font.BOLD, 18));
        pinField.setBorder(BorderFactory.createTitledBorder("MPIN"));
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pinField.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showKeypadBelow(pinField, 4);
            }
        });

        // ===== REMINDER =====
        JLabel reminder = new JLabel("Never share your MPIN with anyone");
        reminder.setFont(new Font("Arial", Font.ITALIC, 12));
        reminder.setForeground(Color.RED);
        reminder.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== LOGIN BUTTON =====
        loginBtn = new JButton("LOGIN");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(200, 50));
        loginBtn.setBackground(new Color(0, 123, 255));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> authenticateUser());

        // ===== FORGOT MPIN (right-aligned) =====
        JLabel forgotLabel = new JLabel("<HTML><U>Forgot MPIN?</U></HTML>");
        forgotLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotLabel.setForeground(Color.WHITE);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginForm.this, "Please contact support to reset MPIN.");
            }
        });
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotPanel.setOpaque(false);
        forgotPanel.add(forgotLabel);

        // ===== NO ACCOUNT YET (centered) =====
        JLabel signupLabel = new JLabel("<HTML><U>No Account Yet?</U></HTML>");
        signupLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        signupLabel.setForeground(Color.WHITE);
        signupLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new SignupForm();
            }
        });
        JPanel signupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        signupPanel.setOpaque(false);
        signupPanel.add(signupLabel);

        // ===== ADD COMPONENTS =====
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(welcome);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(mobileField);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(pinField);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(forgotPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(loginBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(signupPanel);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // ===== KEYPAD BELOW FIELD =====
    private void showKeypadBelow(JTextComponent field, int maxDigits) {
        if (keypadDialog != null && keypadDialog.isVisible()) keypadDialog.dispose();

        keypadDialog = new JDialog(this, false);
        keypadDialog.setUndecorated(true);
        keypadDialog.setSize(220, 300);
        keypadDialog.setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("X");
        close.addActionListener(e -> keypadDialog.dispose());
        top.add(close);

        JPanel grid = new JPanel(new GridLayout(4, 3, 5, 5));
        for (int i = 1; i <= 9; i++) addKey(grid, field, String.valueOf(i), maxDigits);
        JButton del = new JButton("DEL");
        del.setFont(new Font("Arial", Font.BOLD, 16));
        del.addActionListener(e -> deleteLast(field));
        grid.add(del);
        addKey(grid, field, "0", maxDigits);
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Arial", Font.BOLD, 16));
        okBtn.addActionListener(e -> keypadDialog.dispose());
        grid.add(okBtn);

        keypadDialog.add(top, BorderLayout.NORTH);
        keypadDialog.add(grid, BorderLayout.CENTER);

        try {
            Point p = field.getLocationOnScreen();
            keypadDialog.setLocation(
                    p.x + field.getWidth() / 2 - keypadDialog.getWidth() / 2,
                    p.y + field.getHeight() + 5
            );
        } catch (Exception ignored) {}

        keypadDialog.setVisible(true);
    }

    private void addKey(JPanel panel, JTextComponent field, String val, int max) {
        JButton btn = new JButton(val);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.addActionListener(e -> {
            String current = field.getText();
            if (field == mobileField) {
                if (!current.startsWith(MOBILE_PREFIX)) field.setText(MOBILE_PREFIX);
                if (current.length() >= MOBILE_PREFIX.length() + max) return;
                field.setText(field.getText() + val);
                return;
            }
            if (current.length() < max) field.setText(current + val);
        });
        panel.add(btn);
    }

    private void deleteLast(JTextComponent field) {
        String text = field.getText();
        if (field == mobileField && text.length() <= MOBILE_PREFIX.length()) return;
        if (!text.isEmpty()) field.setText(text.substring(0, text.length() - 1));
    }

    private void authenticateUser() {
        String mobile = mobileField.getText();
        String pin = new String(pinField.getPassword());

        if (!mobile.matches("\\+639-\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Invalid mobile number format");
            return;
        }
        if (pin.length() != 4) {
            JOptionPane.showMessageDialog(this, "MPIN must be 4 digits");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE mobileNumber = ?");
            stmt.setString(1, mobile);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Mobile number not registered. Please sign up.");
                dispose();
                new SignupForm();
                return;
            }

            String dbPin = rs.getString("pinCode");
            if (!dbPin.equals(pin)) {
                JOptionPane.showMessageDialog(this, "Incorrect MPIN");
                pinField.setText("");
                return;
            }

            User user = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("mobileNumber"),
                    rs.getString("email"),
                    rs.getString("pinCode"),
                    rs.getInt("balance"),
                    rs.getString("role")
            );

            JOptionPane.showMessageDialog(this, "Welcome, " + user.getName() + "!");
            dispose();
            if ("ADMIN".equalsIgnoreCase(user.getRole())) new AdminDashboard(user);
            else new ClientDashboard(user);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error. Please try again.");
        }
    }
}
