package com.pocketcash;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProfileWindow extends JFrame {

    private final User user;
    private JTextField emailField;
    private boolean isEditing = false;

    public ProfileWindow(User user) {
        this.user = user;

        setTitle("PocketCash - Profile");
        ImageIcon icon = new ImageIcon("title.png");
        setIconImage(icon.getImage());
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);

        Color bg = new Color(58, 213, 159);
        Color card = new Color(230, 245, 255);

        getContentPane().setBackground(bg);

        // ===== CARD =====
        JPanel panel = new JPanel(null);
        panel.setBounds(30, 40, 340, 480);
        panel.setBackground(card);
        add(panel);

        JLabel title = new JLabel("USER PROFILE");
        title.setBounds(20, 15, 200, 25);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(title);

        int y = 60;

        panel.add(label("Name:", 20, y));
        panel.add(value(user.getName(), 150, y));

        y += 40;
        panel.add(label("Mobile Number:", 20, y));
        panel.add(value(user.getMobileNumber(), 150, y));

        y += 40;
        panel.add(label("Role:", 20, y));
        panel.add(value(user.getRole(), 150, y));

        y += 40;
        panel.add(label("Email Address:", 20, y));

        emailField = new JTextField(user.getEmail());
        emailField.setBounds(150, y, 160, 25);
        emailField.setEditable(false); // 🔒 read-only by default
        panel.add(emailField);

        // ===== UPDATE EMAIL BUTTON =====
        JButton updateBtn = new JButton("Update Email");
        updateBtn.setBounds(90, y + 60, 160, 30);
        panel.add(updateBtn);

        // ===== BACK BUTTON =====
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(140, 420, 80, 30);
        backBtn.addActionListener(e -> dispose());
        panel.add(backBtn);

        // ===== BUTTON LOGIC =====
        updateBtn.addActionListener(e -> handleUpdate(updateBtn));

        setVisible(true);
    }

    // ================= METHODS =================

    private void handleUpdate(JButton btn) {

        // Step 1: Enable editing
        if (!isEditing) {
            emailField.setEditable(true);
            emailField.requestFocus();
            btn.setText("Save Email");
            isEditing = true;
            return;
        }

        // Step 2: Ask PIN
        JPasswordField pinField = new JPasswordField();
        int res = JOptionPane.showConfirmDialog(
                this,
                pinField,
                "Enter PIN to confirm",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (res != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword());
        if (!pin.equals(user.getPinCode())) {
            JOptionPane.showMessageDialog(this, "Incorrect PIN!");
            return;
        }

        // Step 3: Update DB
        String newEmail = emailField.getText().trim();
        if (newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty.");
            return;
        }

        if (updateEmailInDB(newEmail)) {
            user.setEmail(newEmail);
            emailField.setEditable(false);
            btn.setText("Update Email");
            isEditing = false;
            JOptionPane.showMessageDialog(this, "Email updated successfully!");
        }
    }

    private boolean updateEmailInDB(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET email=? WHERE id=?"
            );
            ps.setString(1, email);
            ps.setInt(2, user.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating email.");
            return false;
        }
    }

    // ================= UI HELPERS =================

    private JLabel label(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 120, 25);
        return l;
    }

    private JLabel value(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 160, 25);
        return l;
    }
}
