package Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class AddStudent extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, emailField, phoneField, usnField;
    private JComboBox<String> branchComboBox, semesterComboBox;
    private JProgressBar loadingBar;

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ExamSeat";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mprasad";

    public AddStudent() {
        setLayout(new GridLayout(1, 2));

        // Left Panel - Add Student Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(45, 45, 45));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Add New Student");
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
        usnField = new JTextField(15);
        
        semesterComboBox = new JComboBox<>(new String[] {"1", "2", "3", "4", "5", "6"});
        branchComboBox = new JComboBox<>();

        String[] labels = {"Name:", "Email:", "Phone:", "USN:", "Semester:", "Branch:"};
        JComponent[] components = {nameField, emailField, phoneField, usnField, semesterComboBox, branchComboBox};

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
            if (i == 5) {
                formPanel.add(branchComboBox, gbc); // For branch combobox
            } else if (i == 4) {
                formPanel.add(semesterComboBox, gbc); // For semester combobox
            } else {
                formPanel.add(components[i], gbc);
            }
        }

        // Populate Branch ComboBox
        populateBranchComboBox();

        JButton addButton = new JButton("Add Student");
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
                addStudent();
            }
        });

        // Right Panel - Student List
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(45, 45, 45));

        JLabel listTitleLabel = new JLabel("Student List");
        listTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        listTitleLabel.setForeground(Color.WHITE);
        listPanel.add(listTitleLabel, BorderLayout.NORTH);

        // Student Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "USN", "Semester", "Branch"}, 0);
        studentTable = new JTable(tableModel);
        studentTable.setFillsViewportHeight(true);
        studentTable.setBackground(new Color(60, 60, 60));
        studentTable.setForeground(Color.WHITE);
        studentTable.setGridColor(new Color(35, 155, 86));

        JScrollPane scrollPane = new JScrollPane(studentTable);
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
                loadStudentList();
            }
        });
        bottomPanel.add(refreshButton, BorderLayout.EAST);

        listPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add panels to main panel
        add(formPanel);
        add(listPanel);

        // Initial Load of Student List
        loadStudentList();
    }

    private void populateBranchComboBox() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT branch_name FROM branch")) {

            while (rs.next()) {
                branchComboBox.addItem(rs.getString("branch_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching branches.");
        }
    }

    private void addStudent() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String usn = usnField.getText();
        int semester = Integer.parseInt((String) semesterComboBox.getSelectedItem());
        String branch = (String) branchComboBox.getSelectedItem();

        String query = "INSERT INTO students (name, email, phone, usn, semester, branch_id) VALUES (?, ?, ?, ?, ?, (SELECT branch_id FROM branch WHERE branch_name = ?))";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, usn);
            stmt.setInt(5, semester);
            stmt.setString(6, branch);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added successfully!");

            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            usnField.setText("");

            loadStudentList();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding student.");
        }
    }

    private void loadStudentList() {
        loadingBar.setVisible(true);
        loadingBar.setValue(0);

        String query = "SELECT s.student_id, s.name, s.email, s.phone, s.usn, s.semester, b.branch_name FROM students s JOIN branch b ON s.branch_id = b.branch_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                int id = rs.getInt("student_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String usn = rs.getString("usn");
                int semester = rs.getInt("semester");
                String branch = rs.getString("branch_name");

                tableModel.addRow(new Object[]{id, name, email, phone, usn, semester, branch});
            }

            loadingBar.setVisible(false);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student list.");
        }
    }
}
