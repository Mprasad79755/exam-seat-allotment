package Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ExamListPanel extends JPanel {

    private JTable examTable;
    private DefaultTableModel tableModel;

    public ExamListPanel() {
        setLayout(new BorderLayout());

        // Initialize the table model with column names
        String[] columnNames = {"Exam ID", "Exam Name", "Exam Date", "Duration (Minutes)", "Exam Type", "Subject Name"};
        tableModel = new DefaultTableModel(columnNames, 0);
        examTable = new JTable(tableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(examTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load exams from the database
        loadExams();
    }

    private void loadExams() {
        // Fetch exams from the database
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT exam_id, exam_name, exam_date, duration, exam_type, subject_name FROM exam";
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                // Clear existing rows before adding new data
                tableModel.setRowCount(0);

                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("exam_id"),
                        rs.getString("exam_name"),
                        rs.getDate("exam_date"),
                        rs.getInt("duration"),
                        rs.getString("exam_type"),
                        rs.getString("subject_name")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exams", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
}

