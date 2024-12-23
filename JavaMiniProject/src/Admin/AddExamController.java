package Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AddExamController extends JPanel {
    private JTable controllerTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, emailField, phoneField, usernameField;
    private JPasswordField passwordField;
    private JProgressBar loadingBar;

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ExamSeat";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mprasad";

    public AddExamController() {
        setLayout(new GridLayout(1, 2));

        // Left Panel - Add Exam Controller Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(45, 45, 45));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Add New Exam Controller");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        formPanel.add(titleLabel, gbc);

        // Form Fields
        nameField = new JTextField(15);
        emailField = new JTextField(15);
        phoneField = new JTextField(15);
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        String[] labels = {"Name:", "Email:", "Phone:", "Username:", "Password:"};
        JComponent[] components = {nameField, emailField, phoneField, usernameField, passwordField};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.insets = new Insets(5, 0, 5, 10);
            formPanel.add(label, gbc);

            gbc.gridx = 1;
            formPanel.add(components[i], gbc);
        }

        JButton addButton = new JButton("Add Exam Controller");
        addButton.setBackground(new Color(35, 155, 86));
        addButton.setForeground(Color.WHITE);
        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addExamController();
            }
        });

        // Right Panel - Exam Controller List
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(45, 45, 45));

        JLabel listTitleLabel = new JLabel("Exam Controller List");
        listTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        listTitleLabel.setForeground(Color.WHITE);
        listPanel.add(listTitleLabel, BorderLayout.NORTH);

        // Exam Controller Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Username"}, 0);
        controllerTable = new JTable(tableModel);
        controllerTable.setFillsViewportHeight(true);
        controllerTable.setBackground(new Color(60, 60, 60));
        controllerTable.setForeground(Color.WHITE);
        controllerTable.setGridColor(new Color(35, 155, 86));

        JScrollPane scrollPane = new JScrollPane(controllerTable);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        // Loading Bar and Refresh Button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        loadingBar = new JProgressBar();
        loadingBar.setVisible(false);
        bottomPanel.add(loadingBar, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh List");
        refreshButton.setBackground(new Color(35, 155, 86));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadExamControllerList();
            }
        });
        bottomPanel.add(refreshButton, BorderLayout.EAST);

        listPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add panels to main panel
        add(formPanel);
        add(listPanel);

        // Initial Load of Exam Controller List
        loadExamControllerList();
    }

    private void addExamController() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        String hashedPassword = password;

        String query = "INSERT INTO exam_controllers (name, email, phone, username, password) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, username);
            stmt.setString(5, hashedPassword);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Exam Controller added successfully!");

            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            usernameField.setText("");
            passwordField.setText("");

            loadExamControllerList();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding exam controller.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadExamControllerList() {
        loadingBar.setVisible(true);
        loadingBar.setValue(0);

        String query = "SELECT controller_id, name, email, phone, username FROM exam_controllers";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                int id = rs.getInt("controller_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String username = rs.getString("username");

                tableModel.addRow(new Object[]{id, name, email, phone, username});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exam controller list.");
        }

        Timer timer = new Timer(100, new ActionListener() {
            int progress = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 20;
                loadingBar.setValue(progress);

                if (progress >= 100) {
                    loadingBar.setVisible(false);
                    loadingBar.setValue(0);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }
}

