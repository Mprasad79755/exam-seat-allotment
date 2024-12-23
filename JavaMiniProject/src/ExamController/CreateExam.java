package ExamController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import com.toedter.calendar.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class CreateExam extends JPanel {
    private JTextField examNameField, durationField;
    private JComboBox<String> subjectComboBox;
    private JDateChooser examDatePicker;
    private JComboBox<String> examTypeComboBox;
    private JButton saveButton, refreshButton, deleteButton;
    private JTable examTable;
    private DefaultTableModel tableModel;

    public CreateExam() {
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Create New Exam", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        add(headerLabel, BorderLayout.NORTH);

        // Main Panel (Split into two parts)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setDividerSize(10);

        // Left side: Exam Form
        JPanel formPanel = createExamForm();
        splitPane.setLeftComponent(formPanel);

        // Right side: Available Exams (Table)
        JPanel tablePanel = createExamTable();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        // Load subjects into the combo box
        loadSubjects();

        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExam();
            }
        });

        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadExams(); // Reload the exams into the table
            }
        });
//        loadExams();
    }

    // Method to create the exam form (left side)
    private JPanel createExamForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add space around components

        // Exam Name
        JLabel examNameLabel = new JLabel("Exam Name:");
        examNameField = new JTextField(20);
        examNameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        examNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        examNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(examNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(examNameField, gbc);

        // Exam Date
        JLabel examDateLabel = new JLabel("Exam Date:");
        examDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        examDatePicker = new JDateChooser();
        examDatePicker.setFont(new Font("Arial", Font.PLAIN, 14));
        examDatePicker.setDateFormatString("yyyy-MM-dd");
        examDatePicker.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(examDateLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(examDatePicker, gbc);

        // Duration (in hours)
        JLabel durationLabel = new JLabel("Duration (hours):");
        durationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        durationField = new JTextField(20);
        durationField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        durationField.setFont(new Font("Arial", Font.PLAIN, 14));

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(durationLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(durationField, gbc);

        // Exam Type
        JLabel examTypeLabel = new JLabel("Exam Type:");
        examTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        examTypeComboBox = new JComboBox<>(new String[] {"Written", "Practical"});
        examTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        examTypeComboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(examTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(examTypeComboBox, gbc);

        // Subjects (dropdown)
        JLabel subjectLabel = new JLabel("Select Subjects:");
        subjectLabel.setFont(new Font("Arial", Font.BOLD, 14));
        subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        subjectComboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(subjectLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(subjectComboBox, gbc);

        // Buttons (Save, Refresh, Delete) - Styled and aligned to the right
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Align to the right
        buttonPanel.setBackground(new Color(255, 255, 255));

        saveButton = new JButton("Save Exam");
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.setFocusPainted(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(255, 69, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 16));
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        deleteButton = new JButton("Delete Exam");
        deleteButton.setBackground(new Color(255, 69, 0));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteButton.setPreferredSize(new Dimension(120, 40));
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        buttonPanel.add(saveButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 1;
        gbc.gridy = 5;
        formPanel.add(buttonPanel, gbc);

        // Button actions
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteExam();
            }
        });

        return formPanel;
    }

    private void deleteExam() {
        // Get the selected exam from the table
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exam to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the exam name from the selected row
        String examName = (String) tableModel.getValueAt(selectedRow, 1);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String deleteQuery = "DELETE FROM exam WHERE exam_name = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteQuery)) {
                ps.setString(1, examName);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Exam deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadExams(); // Refresh the exam list after deletion
                } else {
                    JOptionPane.showMessageDialog(this, "No such exam found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to create the exam table (right side)
    private JPanel createExamTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        // Table to display exams
        String[] columns = {"Exam ID", "Exam Name", "Exam Date", "Duration", "Type", "Subject"};
        tableModel = new DefaultTableModel(columns, 0);
        examTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(examTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    // Method to load all exams from the database into the table
    private void loadExams() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            // Updated query to exclude teacher and room_id information, only fetching exam details
            String selectQuery = "SELECT e.exam_id, e.exam_name, e.exam_date, e.duration, e.exam_type, e.subject_name "
                                + "FROM exam e";
            try (PreparedStatement ps = con.prepareStatement(selectQuery); ResultSet rs = ps.executeQuery()) {
                tableModel.setRowCount(0); // Clear existing data
                while (rs.next()) {
                    int examId = rs.getInt("exam_id");
                    String examName = rs.getString("exam_name");
                    Date examDate = rs.getDate("exam_date");
                    String duration = rs.getString("duration");
                    String examType = rs.getString("exam_type");
                    String subject = rs.getString("subject_name");

                    // Add the row to the table model
                    Object[] row = {examId, examName, examDate, duration, examType, subject};
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    // Method to load subject names into the combo box
    private void loadSubjects() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String selectQuery = "SELECT subject_name FROM subjects";
            try (PreparedStatement ps = con.prepareStatement(selectQuery); ResultSet rs = ps.executeQuery()) {
                subjectComboBox.removeAllItems(); // Clear existing items
                while (rs.next()) {
                    subjectComboBox.addItem(rs.getString("subject_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to save the exam into the database
    private void saveExam() {
        String examName = examNameField.getText();
        String examDate = new SimpleDateFormat("yyyy-MM-dd").format(examDatePicker.getDate());
        String duration = durationField.getText();
        String examType = (String) examTypeComboBox.getSelectedItem();
        String subject = (String) subjectComboBox.getSelectedItem();

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            // Disable auto-commit for transaction control
            con.setAutoCommit(false);

            try {
                // Insert exam details
                String insertExamQuery = "INSERT INTO exam (exam_name, exam_date, duration, exam_type, subject_name) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertExamQuery, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, examName);
                    ps.setString(2, examDate);
                    ps.setString(3, duration);
                    ps.setString(4, examType);
                    ps.setString(5, subject);
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        // Get generated exam_id for the new exam
                        ResultSet generatedKeys = ps.getGeneratedKeys();
                        int examId = -1;
                        if (generatedKeys.next()) {
                            examId = generatedKeys.getInt(1);
                        }

                        // Select a random teacher for invigilator role
                        int teacherId = getRandomTeacherId(con);

                        // Check if data already exists in exam_room_availability before inserting
                        if (examId != -1 && teacherId != -1) {
                            String checkAvailabilityQuery = "SELECT COUNT(*) FROM exam_room_availability WHERE exam_id = ?";
                            try (PreparedStatement psCheck = con.prepareStatement(checkAvailabilityQuery)) {
                                psCheck.setInt(1, examId);
                                ResultSet rsCheck = psCheck.executeQuery();
                                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                                    // Insert room availability with the selected teacher for the newly created exam
                                    String insertAvailabilityQuery = "INSERT INTO exam_room_availability (exam_id, room_id, available_seats, teacher_id) "
                                            + "SELECT ?, room_id, capacity, ? FROM room";
                                    try (PreparedStatement psAvailability = con.prepareStatement(insertAvailabilityQuery)) {
                                        psAvailability.setInt(1, examId);
                                        psAvailability.setInt(2, teacherId);
                                        psAvailability.executeUpdate();
                                    }

                                    // Commit the transaction after all operations
                                    con.commit();
                                    JOptionPane.showMessageDialog(this, "Exam created successfully with room availability and teacher assignment", "Success", JOptionPane.INFORMATION_MESSAGE);
                                    loadExams(); // Refresh the exam list after adding a new exam
                                } else {
                                    // Rollback if data already exists
                                    con.rollback();
                                    JOptionPane.showMessageDialog(this, "Room availability for the exam already exists", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {
                            throw new SQLException("Failed to assign teacher or room availability.");
                        }
                    }
                }
            } catch (SQLException e) {
                // If any exception occurs, roll back the transaction
                con.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving exam: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Reset auto-commit to true after the transaction
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving exam connection: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    // Method to fetch a random teacher ID
    private int getRandomTeacherId(Connection con) {
        int teacherId = -1;
        String query = "SELECT teacher_id FROM teachers ORDER BY RAND() LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                teacherId = rs.getInt("teacher_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teacherId;
    }
}

