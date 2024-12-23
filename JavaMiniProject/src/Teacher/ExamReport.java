package Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class ExamReport extends JPanel {
    private JTable examReportTable;
    private JButton downloadButton;
    private JButton filterButton;
    private JComboBox<String> examFilterComboBox;
    private JScrollPane scrollPane;
    private JLabel footerLabel;

    public ExamReport() {
        setLayout(new BorderLayout());

        // Create the exam report table
        examReportTable = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String[] {"Student Name", "Exam Name", "Subject", "Exam Date", "Room Number", "Room Name", "Capacity"}, 0);
        examReportTable.setModel(model);

        // Add table to scroll pane
        scrollPane = new JScrollPane(examReportTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create download button
        downloadButton = new JButton("Download Report");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadExamReport();
            }
        });

        // Create filter button
        filterButton = new JButton("Filter by Exam");
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filterExamReport();
            }
        });

        // Create the combo box for selecting an exam to filter by
        examFilterComboBox = new JComboBox<>();
        loadExamsForFilter();

        // Create panel for buttons and filter
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Select Exam:"));
        controlPanel.add(examFilterComboBox);
        controlPanel.add(filterButton);
        controlPanel.add(downloadButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Create footer to display total number of students
        footerLabel = new JLabel("Total Students: 0");
        add(footerLabel, BorderLayout.NORTH);

        // Load the full exam report data initially
        loadExamReportData();
    }

    // Method to load exams into the filter combo box
    private void loadExamsForFilter() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT exam_name FROM exam";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    examFilterComboBox.addItem(rs.getString("exam_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exams for filter.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to load the exam report data into the table
    private void loadExamReportData() {
    	String query = "SELECT students.name AS student_name, exam.exam_name, exam.exam_date, exam.subject_name, " +
                "exam_room_availability.room_id, room.room_name, room.capacity " +
                "FROM seat_allocation " +
                "JOIN students ON seat_allocation.student_id = students.student_id " +
                "JOIN exam ON seat_allocation.exam_id = exam.exam_id " +
                "JOIN exam_room_availability ON exam.exam_id = exam_room_availability.exam_id " +
                "JOIN room ON exam_room_availability.room_id = room.room_id";

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            DefaultTableModel model = (DefaultTableModel) examReportTable.getModel();
            model.setRowCount(0);  // Clear existing rows
            int studentCount = 0;

            while (rs.next()) {
                String studentName = rs.getString("student_name");
                String examName = rs.getString("exam_name");
                Date examDate = rs.getDate("exam_date");
                String subjectName = rs.getString("subject_name");
                int roomId = rs.getInt("room_id");
                String roomName = rs.getString("room_name");
                int capacity = rs.getInt("capacity");

                model.addRow(new Object[] { studentName, examName, subjectName, examDate, roomId, roomName, capacity });
                studentCount++;
            }

            footerLabel.setText("Total Students: " + studentCount);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exam report data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to filter exam report based on the selected exam
    private void filterExamReport() {
        String selectedExam = (String) examFilterComboBox.getSelectedItem();
        if (selectedExam == null || selectedExam.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an exam to filter.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "SELECT student.name AS student_name, exam.exam_name, exam.exam_date, subject.subject_name, " +
                       "exam_room_availability.room_id, room.room_name, room.capacity " +
                       "FROM exam " +
                       "JOIN exam_room_availability ON exam.exam_id = exam_room_availability.exam_id " +
                       "JOIN room ON exam_room_availability.room_id = room.room_id " +
                       "JOIN student ON student.exam_id = exam.exam_id " + 
                       "JOIN subject ON subject.exam_id = exam.exam_id " +
                       "WHERE exam.exam_name = ?";

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, selectedExam);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = (DefaultTableModel) examReportTable.getModel();
            model.setRowCount(0);  // Clear existing rows
            int studentCount = 0;

            while (rs.next()) {
                String studentName = rs.getString("student_name");
                String examName = rs.getString("exam_name");
                Date examDate = rs.getDate("exam_date");
                String subjectName = rs.getString("subject_name");
                int roomId = rs.getInt("room_id");
                String roomName = rs.getString("room_name");
                int capacity = rs.getInt("capacity");

                model.addRow(new Object[] { studentName, examName, subjectName, examDate, roomId, roomName, capacity });
                studentCount++;
            }

            footerLabel.setText("Total Students: " + studentCount);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error filtering exam report data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to download the exam report
    private void downloadExamReport() {
        try {
            String filePath = "exam_report.csv"; // You can choose other formats like .pdf as well
            FileWriter writer = new FileWriter(filePath);
            DefaultTableModel model = (DefaultTableModel) examReportTable.getModel();

            // Writing table header to file
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.write(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? "," : "\n"));
            }

            // Writing table data to file
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    writer.write(model.getValueAt(i, j).toString() + (j < model.getColumnCount() - 1 ? "," : "\n"));
                }
            }

            writer.close();
            JOptionPane.showMessageDialog(this, "Exam report downloaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error downloading the report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method for testing the panel
   
}
