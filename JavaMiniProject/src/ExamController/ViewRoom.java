package ExamController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ViewRoom extends JPanel {
    private JComboBox<String> examComboBox;
    private JComboBox<String> roomComboBox;
    private JButton showLayoutButton;
    private JButton completeExamButton; // New button to complete the exam
    private JPanel seatLayoutPanel;
    private Map<Integer, SeatDetails> seatStatusMap = new HashMap<>();
    private int selectedExamId = -1;

    public ViewRoom() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // Panel for selecting exam and room
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JLabel examLabel = new JLabel("Select Exam:");
        examComboBox = new JComboBox<>();
        loadExams();  // Load exam data from the database
        examComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedExam = (String) examComboBox.getSelectedItem();
                if (selectedExam != null) {
                    selectedExamId = getExamIdByName(selectedExam);
                    loadRoomsForExam(selectedExamId);  // Load rooms allocated for the selected exam
                }
            }
        });
        controlPanel.add(examLabel);
        controlPanel.add(examComboBox);

        JLabel roomLabel = new JLabel("Select Room:");
        roomComboBox = new JComboBox<>();
        controlPanel.add(roomLabel);
        controlPanel.add(roomComboBox);

        showLayoutButton = new JButton("Show Room Layout");
        showLayoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoomLayout();
            }
        });
        controlPanel.add(showLayoutButton);

        // Adding the "Complete Exam" button
        completeExamButton = new JButton("Complete Exam");
        completeExamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completeExam();
            }
        });
        controlPanel.add(completeExamButton);

        add(controlPanel, BorderLayout.NORTH);

        // Panel for displaying the room layout
        seatLayoutPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSeatLayout(g);
            }
        };
        add(seatLayoutPanel, BorderLayout.CENTER);
    }

    // Load exams into combo box
    private void loadExams() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT exam_id, exam_name FROM exam";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    examComboBox.addItem(rs.getString("exam_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get exam ID by exam name
    private int getExamIdByName(String examName) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT exam_id FROM exam WHERE exam_name = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, examName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("exam_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Load rooms allocated for the selected exam
    private void loadRoomsForExam(int examId) {
        roomComboBox.removeAllItems();
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT room_id, room_name FROM room WHERE room_id IN (SELECT room_id FROM seat_allocation WHERE exam_id = ?)";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, examId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    roomComboBox.addItem(rs.getString("room_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show room layout with free and occupied seats
    private void showRoomLayout() {
        seatStatusMap.clear();
        String selectedRoom = (String) roomComboBox.getSelectedItem();
        if (selectedRoom == null || selectedRoom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int roomId = getRoomIdByName(selectedRoom);
        if (roomId == -1) {
            JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get seat allocation data for the selected room
        loadSeatAllocationData(roomId);

        // Repaint the panel to show the layout
        seatLayoutPanel.repaint();
    }

    // Load seat allocation data for the selected room
    private void loadSeatAllocationData(int roomId) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT seat_number, student_id FROM seat_allocation WHERE room_id = ? AND exam_id = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, roomId);
                ps.setInt(2, selectedExamId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int seatNumber = rs.getInt("seat_number");
                    int studentId = rs.getInt("student_id");
                    String studentName = getStudentNameById(studentId);
                    seatStatusMap.put(seatNumber, new SeatDetails(true, studentName));  // Mark seat as occupied and assign student
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get student name by student ID
    private String getStudentNameById(int studentId) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT name FROM students WHERE student_id = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // Draw the seat layout on the panel
    private void drawSeatLayout(Graphics g) {
        if (seatStatusMap.isEmpty()) {
            return;
        }

        int numRows = 5;
        int numCols = 5;
        int seatWidth = 50;
        int seatHeight = 50;
        int padding = 10;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int seatNumber = row * numCols + col + 1;
                int x = col * (seatWidth + padding);
                int y = row * (seatHeight + padding);

                if (seatStatusMap.containsKey(seatNumber) && seatStatusMap.get(seatNumber).isOccupied()) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.GREEN);
                }
                g.fillRect(x, y, seatWidth, seatHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, seatWidth, seatHeight);

                g.setColor(Color.WHITE);
                g.drawString(String.valueOf(seatNumber), x + seatWidth / 2 - 10, y + seatHeight / 2 + 5);

                if (seatStatusMap.containsKey(seatNumber) && seatStatusMap.get(seatNumber).isOccupied()) {
                    String studentName = seatStatusMap.get(seatNumber).getStudentName();
                    g.setColor(Color.BLACK);
                    g.drawString(studentName, x + seatWidth / 2 - 20, y + seatHeight / 2 - 10);
                }
            }
        }
    }

    // Get room ID by room name
    private int getRoomIdByName(String roomName) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT room_id FROM room WHERE room_name = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, roomName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("room_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Complete the exam and clear room allocation
 // Complete the exam and remove records from the related tables
    private void completeExam() {
        String selectedRoom = (String) roomComboBox.getSelectedItem();
        if (selectedRoom == null || selectedRoom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int roomId = getRoomIdByName(selectedRoom);
        if (roomId == -1) {
            JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            // Start a transaction to ensure data integrity
            con.setAutoCommit(false);
            String deleteRoomAvailabilityQuery = "DELETE FROM exam_room_availability WHERE room_id = ? AND exam_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteRoomAvailabilityQuery)) {
                ps.setInt(1, roomId);
                ps.setInt(2, selectedExamId);
                ps.executeUpdate();
            }
            try {
                // Remove records from seat_allocation for the specific room and exam
                String deleteSeatAllocationQuery = "DELETE FROM seat_allocation WHERE room_id = ? AND exam_id = ?";
                try (PreparedStatement ps = con.prepareStatement(deleteSeatAllocationQuery)) {
                    ps.setInt(1, roomId);
                    ps.setInt(2, selectedExamId);
                    ps.executeUpdate();
                }

                // Remove the records from exam_room_availability for the specific room and exam
                

                // Remove the exam record (if needed)
                String deleteExamQuery = "DELETE FROM exam WHERE exam_id = ?";
                try (PreparedStatement ps = con.prepareStatement(deleteExamQuery)) {
                    ps.setInt(1, selectedExamId);
                    ps.executeUpdate();
                }

                // Commit the transaction
                con.commit();
                JOptionPane.showMessageDialog(this, "Exam completed and records removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException e) {
                // Rollback in case of error
                con.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while completing the exam. Transaction rolled back.", "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Reset the auto-commit mode
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper class to store seat status and student name
    private static class SeatDetails {
        private boolean occupied;
        private String studentName;

        public SeatDetails(boolean occupied, String studentName) {
            this.occupied = occupied;
            this.studentName = studentName;
        }

        public boolean isOccupied() {
            return occupied;
        }

        public String getStudentName() {
            return studentName;
        }
    }
}
