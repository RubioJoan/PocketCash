package com.pocketcash;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {

    public MainDashboard() {

        setTitle("PocketCash");
        ImageIcon icon = new ImageIcon("title.png");
        setIconImage(icon.getImage());
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main panel bg color
        JPanel panel = new JPanel();
        panel.setBackground(new Color(58, 213, 159));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(100, 20, 30, 20));

        // Logo
        ImageIcon logoIcon = new ImageIcon("logo.png");
        Image scaledImage = logoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);


        // subtitle
        JLabel subtitle = new JLabel("Your Companion for Managing money");
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Buttons
        JButton loginBtn = new JButton("LOGIN");
        JButton registerBtn = new JButton("REGISTER");

        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);

        loginBtn.setMaximumSize(new Dimension(200, 200));
        registerBtn.setMaximumSize(new Dimension(200, 200));

        // Make LOGIN button blue
        loginBtn.setBackground(new Color(0, 123, 255));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);

        // Action listeners
        loginBtn.addActionListener(e -> new LoginForm());
        registerBtn.addActionListener(e -> new SignupForm());

        // Disclaimer text
        JLabel disclaimer = new JLabel("<html><b>Disclaimer:</b> <br> <em> Pocket Cash is a Java-based online banking application created for educational and demonstration purposes ONLY. "
                + "It is not a licensed financial institution and does not provide real banking services or handle actual money.<em></html>");
        disclaimer.setFont(new Font("Arial", Font.PLAIN, 10));
        disclaimer.setForeground(Color.WHITE);
        disclaimer.setAlignmentX(CENTER_ALIGNMENT);

        //components
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(70));
        panel.add(loginBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(disclaimer);
        panel.add(Box.createVerticalStrut(10));

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new MainDashboard();
    }
}
