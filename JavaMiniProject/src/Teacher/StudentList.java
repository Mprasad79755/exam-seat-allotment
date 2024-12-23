package Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentList extends JPanel {

    private JComboBox<String> branchComboBox;
    private JTable studentsTable;
    private DefaultTableModel tableModel;

    public StudentList() {
        // Set layout for the panel
        setLayout(new BorderLayout());

        // Panel for branch selection
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());

        JLabel branchLabel = new JLabel("Select Branch:");
        branchComboBox = new JComboBox<>();
        
        // Add branches to the JComboBox (fetch from the database)
        fetchBranches();
        
        // Add action listener to filter students based on selected branch
        branchComboBox.addActionListener(e -> filterStudentsByBranch());

        filterPanel.add(branchLabel);
        filterPanel.add(branchComboBox);

        add(filterPanel, BorderLayout.NORTH);

        // Table for displaying students
        tableModel = new DefaultTableModel(new String[]{"Student ID", "Name", "USN", "Branch"}, 0);
        studentsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load all students initially
        loadStudents("");
    }

    private void fetchBranches() {
        // Fetch branches from the database and add them to the JComboBox
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT branch_name FROM branch"; // Assuming the column in branch table is branch_name
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    branchComboBox.addItem(rs.getString("branch_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching branches", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterStudentsByBranch() {
        String selectedBranch = (String) branchComboBox.getSelectedItem();
        loadStudents(selectedBranch); // Load students for the selected branch
    }

    private void loadStudents(String branch) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Fetch students from the database
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT s.student_id, s.name, s.usn, b.branch_name " +
                           "FROM students s " +
                           "JOIN branch b ON s.branch_id = b.branch_id"; // Join with branch table
            if (!branch.isEmpty()) {
                query += " WHERE b.branch_name = ?"; // Filter by branch_name
            }

            try (PreparedStatement ps = con.prepareStatement(query)) {
                if (!branch.isEmpty()) {
                    ps.setString(1, branch); // Set the selected branch in the query
                }
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    // Add each student as a row in the table
                    Object[] row = {
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("usn"),
                        rs.getString("branch_name") // Use branch_name from the branch table
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}