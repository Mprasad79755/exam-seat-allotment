package ExamController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllotmentSystem extends JPanel {
    private JTable studentTable;
    private JComboBox<String> examComboBox;
    private JButton allocateButton, selectAllButton;
    private DefaultTableModel studentTableModel;
    private int selectedExamId;

    public AllotmentSystem() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        JPanel studentPanel = createStudentTablePanel();
        add(studentPanel, BorderLayout.NORTH);

        JPanel examPanel = createExamSelectionPanel();
        add(examPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        allocateButton = new JButton("Allocate Seats");
        selectAllButton = new JButton("Select All");
        
        // Add listener to Select All button
        selectAllButton.addActionListener(e -> selectAllStudents());
        
        bottomPanel.add(selectAllButton);
        bottomPanel.add(allocateButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        allocateButton.addActionListener(e -> allocateSeats());
    }

    private JPanel createStudentTablePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        String[] columns = {"Select", "Student ID", "Name", "Email"};
        studentTableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(studentTableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }
        };
        studentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExamSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel examLabel = new JLabel("Select Exam:");
        examComboBox = new JComboBox<>();
        loadExams();

        examComboBox.addActionListener(e -> {
            String selectedExamName = (String) examComboBox.getSelectedItem();
            selectedExamId = getExamIdByName(selectedExamName);
            loadUnallocatedStudents(selectedExamId);
        });

        panel.add(examLabel);
        panel.add(examComboBox);

        return panel;
    }

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

    private void loadUnallocatedStudents(int examId) {
        studentTableModel.setRowCount(0);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT student_id, name, email FROM students " +
                    "WHERE student_id NOT IN (SELECT student_id FROM seat_allocation WHERE exam_id = ?)";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, examId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Object[] row = {false, rs.getInt("student_id"), rs.getString("name"), rs.getString("email")};
                    studentTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void allocateSeats() {
        List<Integer> selectedStudentIds = new ArrayList<>();
        for (int row = 0; row < studentTableModel.getRowCount(); row++) {
            Boolean isSelected = (Boolean) studentTableModel.getValueAt(row, 0);
            if (isSelected != null && isSelected) {
                selectedStudentIds.add((Integer) studentTableModel.getValueAt(row, 1));
            }
        }

        if (selectedStudentIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one student.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedExamName = (String) examComboBox.getSelectedItem();
        selectedExamId = getExamIdByName(selectedExamName);

        if (selectedExamId == -1) {
            JOptionPane.showMessageDialog(this, "Invalid exam selection.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Room> availableRooms = getAvailableRooms(selectedExamId);

        int totalCapacity = availableRooms.stream().mapToInt(Room::getAvailableSeats).sum();
        if (selectedStudentIds.size() > totalCapacity) {
            JOptionPane.showMessageDialog(this, "Not enough total capacity across all available rooms. " + totalCapacity + " " + selectedStudentIds.size(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        allocateSeatsToStudents(selectedStudentIds, availableRooms);

        loadUnallocatedStudents(selectedExamId);
    }

    private void allocateSeatsToStudents(List<Integer> selectedStudentIds, List<Room> availableRooms) {
        int seatNumber = 1;
        int roomIndex = 0;

        for (int studentId : selectedStudentIds) {
            Room room = availableRooms.get(roomIndex);

            if (seatNumber > room.getAvailableSeats()) {
                roomIndex++;
                if (roomIndex >= availableRooms.size()) {
                    break;
                }
                seatNumber = 1;
                room = availableRooms.get(roomIndex);
            }

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
                String insertQuery = "INSERT INTO seat_allocation (student_id, exam_id, room_id, seat_number) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                    ps.setInt(1, studentId);
                    ps.setInt(2, selectedExamId);
                    ps.setInt(3, room.getRoomId());
                    ps.setInt(4, seatNumber++);
                    ps.executeUpdate();
                }
                room.decrementAvailableSeats();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(this, "Seats allocated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

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

    private List<Room> getAvailableRooms(int examId) {
        List<Room> rooms = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT r.room_id, r.room_name, a.available_seats " +
                    "FROM room r JOIN exam_room_availability a ON r.room_id = a.room_id " +
                    "WHERE a.exam_id = ? AND a.available_seats > 0";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, examId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    rooms.add(new Room(rs.getInt("room_id"), rs.getString("room_name"), rs.getInt("available_seats")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private void selectAllStudents() {
        for (int row = 0; row < studentTableModel.getRowCount(); row++) {
            studentTableModel.setValueAt(true, row, 0); // Select all students
        }
    }

    private class Room {
        private int roomId;
        private String roomName;
        private int availableSeats;

        public Room(int roomId, String roomName, int availableSeats) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.availableSeats = availableSeats;
        }

        public int getRoomId() { return roomId; }
        public int getAvailableSeats() { return availableSeats; }
        public void decrementAvailableSeats() {
            // Get the current available seats from the database
            int currentAvailableSeats = getCurrentAvailableSeatsFromDatabase();

            if (currentAvailableSeats > 0) {
                // Decrease the available seats
                currentAvailableSeats--;

                // Update the available seats in the database
                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
                    String updateQuery = "UPDATE exam_room_availability SET available_seats = ? WHERE room_id = ? AND exam_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(updateQuery)) {
                        ps.setInt(1, currentAvailableSeats);
                        ps.setInt(2, roomId);
                        ps.setInt(3, selectedExamId); // Make sure selectedExamId is accessible in this context
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No available seats left in the room.");
            }
        }

        // Method to fetch current available seats from the database
        private int getCurrentAvailableSeatsFromDatabase() {
            int availableSeats = 0;
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
                String query = "SELECT available_seats FROM exam_room_availability WHERE room_id = ? AND exam_id = ?";
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setInt(1, roomId);
                    ps.setInt(2, selectedExamId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        availableSeats = rs.getInt("available_seats");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return availableSeats;
        }
    }
}
