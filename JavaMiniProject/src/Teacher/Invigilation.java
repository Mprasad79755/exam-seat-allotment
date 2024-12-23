package Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Invigilation extends JPanel {

    private JTable allocationTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> teacherComboBox;

    public Invigilation() {
        setLayout(new BorderLayout());

        // Panel to filter exams by teacher
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Teacher selection combo box
        teacherComboBox = new JComboBox<>();
        teacherComboBox.addActionListener(e -> loadExamAllocations()); // Load exams when a teacher is selected
        filterPanel.add(new JLabel("Select Teacher:"));
        filterPanel.add(teacherComboBox);

        add(filterPanel, BorderLayout.NORTH);

        // Initialize the table model with column names
        String[] columnNames = {"Exam Name", "Teacher Name", "Exam Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        allocationTable = new JTable(tableModel);
        allocationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(allocationTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load teachers and exam allocations
        loadTeachers();
        loadExamAllocations();
    }

    private void loadTeachers() {
        // Fetch teachers from the database and populate the JComboBox
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT teacher_id, name FROM teachers";
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    teacherComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching teachers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadExamAllocations() {
        // Clear existing rows
        tableModel.setRowCount(0);

        String selectedTeacher = (String) teacherComboBox.getSelectedItem();

        if (selectedTeacher == null || selectedTeacher.isEmpty()) {
            return;
        }

        // Fetch exam allocations for the selected teacher from exam_room_availability
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT e.exam_name, e.exam_date, t.name AS teacher_name " +
                    "FROM exam_room_availability er " +
                    "JOIN exam e ON er.exam_id = e.exam_id " +
                    "JOIN teachers t ON er.teacher_id = t.teacher_id " +
                    "WHERE t.name = ?";

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, selectedTeacher);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Object[] row = {
                        rs.getString("exam_name"),
                        rs.getString("teacher_name"),
                        rs.getDate("exam_date")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exam allocations", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
}
