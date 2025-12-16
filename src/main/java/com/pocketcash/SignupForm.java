package com.pocketcash;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignupForm extends JFrame {

    private JTextField nameField;
    private JTextField mobileField;
    private JTextField emailField;
    private JPasswordField pinField;
    private JPasswordField confirmPinField;
    private JTextField depositField;
    private JButton signupBtn;
    private JButton backBtn;

    public SignupForm() {
        setTitle("PocketCash - Signup");
        ImageIcon icon = new ImageIcon("title.png");
        setIconImage(icon.getImage());
        setSize(400, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(58, 213, 159));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Logo
        ImageIcon logoIcon = new ImageIcon("logo.png");
        Image scaled = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Set up your profile. Complete all fields.");
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        nameField = new JTextField();
        nameField.setBorder(BorderFactory.createTitledBorder("Full Name"));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        mobileField = new JTextField("+639-");
        mobileField.setBorder(BorderFactory.createTitledBorder("Mobile Number"));
        mobileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        emailField = new JTextField();
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Pin
        pinField = new JPasswordField();
        pinField.setBorder(BorderFactory.createTitledBorder("4-digit PIN"));
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton togglePin = new JButton("Show");
        togglePin.setPreferredSize(new Dimension(70, 40));
        togglePin.addActionListener(e -> {
            if (pinField.getEchoChar() != (char) 0) {
                pinField.setEchoChar((char) 0);
                togglePin.setText("Hide");
            } else {
                pinField.setEchoChar('*');
                togglePin.setText("Show");
            }
        });

        JPanel pinPanel = new JPanel(new BorderLayout());
        pinPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pinPanel.add(pinField, BorderLayout.CENTER);
        pinPanel.add(togglePin, BorderLayout.EAST);

        // Cofirm Pin
        confirmPinField = new JPasswordField();
        confirmPinField.setBorder(BorderFactory.createTitledBorder("Confirm 4-digit PIN"));
        confirmPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton toggleConfirmPin = new JButton("Show");
        toggleConfirmPin.setPreferredSize(new Dimension(70, 40));
        toggleConfirmPin.addActionListener(e -> {
            if (confirmPinField.getEchoChar() != (char) 0) {
                confirmPinField.setEchoChar((char) 0);
                toggleConfirmPin.setText("Hide");
            } else {
                confirmPinField.setEchoChar('*');
                toggleConfirmPin.setText("Show");
            }
        });

        JPanel confirmPinPanel = new JPanel(new BorderLayout());
        confirmPinPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirmPinPanel.add(confirmPinField, BorderLayout.CENTER);
        confirmPinPanel.add(toggleConfirmPin, BorderLayout.EAST);

        depositField = new JTextField();
        depositField.setBorder(BorderFactory.createTitledBorder("Initial Deposit"));
        depositField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // ================= SIGNUP BUTTON =================
        signupBtn = new JButton("SIGN UP");
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupBtn.setMaximumSize(new Dimension(200, 50));
        signupBtn.setBackground(new Color(0, 123, 255));
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setFocusPainted(false);
        signupBtn.addActionListener(e -> signupAction());

        //Back Btn
        backBtn = new JButton("BACK");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(200, 40));
        backBtn.addActionListener(e -> {
            dispose();
            new MainDashboard();
        });

        //Components
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(nameField);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(mobileField);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(pinPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(confirmPinPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(depositField);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(signupBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(backBtn);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // ================= SIGNUP ACTION =================
    private void signupAction() {
        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String email = emailField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();
        String confirmPin = new String(confirmPinField.getPassword()).trim();
        String depositStr = depositField.getText().trim();

        // ========== VALIDATION ==========
        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || pin.isEmpty() || confirmPin.isEmpty() || depositStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        if (!mobile.matches("\\+639-\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Mobile must be in format +639-XXXXXXXXX");
            return;
        }

        if (pin.length() != 4 || !pin.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(this, "PIN must be 4 digits");
            return;
        }

        if (!pin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(this, "PIN and Confirm PIN do not match");
            return;
        }

        int deposit;
        try {
            deposit = Integer.parseInt(depositStr);
            if (deposit < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid deposit amount");
            return;
        }

        // ========== INSERT INTO DATABASE ==========
        try {
            Connection conn = DatabaseConnection.getConnection(); // XAMPP MySQL connection
            String sql = "INSERT INTO users (name, mobileNumber, email, pinCode, balance, role) " +
                    "VALUES (?, ?, ?, ?, ?, 'CLIENT')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, mobile);
            stmt.setString(3, email);
            stmt.setString(4, pin);
            stmt.setInt(5, deposit);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Signup successful!");
            dispose();
            new LoginForm();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving to database: " + ex.getMessage());
        }
    }
}
