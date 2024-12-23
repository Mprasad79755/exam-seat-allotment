package Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import Teacher.ExamListPanel;

public class Login extends JFrame {

    private JTextField usnField;
    private JTextArea examDetailsArea;
    private JButton viewRoomButton;
    private JButton viewTimetableButton;
    private JPanel roomPanel;
    private JPanel mainPanel;  // To hold the current panel (either login or timetable panel)

    public Login() {
        setTitle("Upcoming Exam Allotment");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main panel to switch between views
        mainPanel = new JPanel();
        mainPanel.setLayout(new CardLayout());

        // Panel for USN entry
        JPanel usnPanel = new JPanel();
        usnPanel.setLayout(new FlowLayout());
        JLabel usnLabel = new JLabel("Enter USN:");
        usnField = new JTextField(15);
        JButton fetchExamButton = new JButton("Fetch Exam");

        usnPanel.add(usnLabel);
        usnPanel.add(usnField);
        usnPanel.add(fetchExamButton);

        // Area for displaying exam details
        examDetailsArea = new JTextArea();
        examDetailsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(examDetailsArea);

        // Panel for view room button and room layout
        JPanel roomPanelContainer = new JPanel();
        roomPanelContainer.setLayout(new BorderLayout());
        viewRoomButton = new JButton("View Room Layout");
        roomPanelContainer.add(viewRoomButton, BorderLayout.NORTH);

        roomPanel = new JPanel();
        roomPanel.setLayout(new GridLayout(5, 5));  // Example room with 5x5 grid of seats
        roomPanel.setPreferredSize(new Dimension(300, 300));

        roomPanelContainer.add(roomPanel, BorderLayout.CENTER);

        // View Timetable Button
        viewTimetableButton = new JButton("View Timetable");
        viewTimetableButton.addActionListener(e -> showTimetablePanel());

        // Add buttons and areas to the main panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());
        loginPanel.add(usnPanel, BorderLayout.NORTH);
        loginPanel.add(scrollPane, BorderLayout.CENTER);
        loginPanel.add(roomPanelContainer, BorderLayout.SOUTH);
        loginPanel.add(viewTimetableButton, BorderLayout.EAST);

        mainPanel.add(loginPanel, "Login");

        // Add main panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Event Listener for fetching exam details
        fetchExamButton.addActionListener(e -> fetchExamDetails());

        // Event Listener for viewing the room layout
        viewRoomButton.addActionListener(e -> viewRoomLayout());
    }

    private void showTimetablePanel() {
        // Create and show the TimetablePanel
        ExamListPanel timetablePanel = new ExamListPanel();
        mainPanel.add(timetablePanel, "Timetable");
        
        // Switch to the Timetable panel
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        cardLayout.show(mainPanel, "Timetable");
    }

    private void fetchExamDetails() {
        String usn = usnField.getText().trim();
        if (usn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your USN", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Fetch exam details from the database based on the USN
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT e.exam_name, e.exam_date, r.room_id, sa.seat_number " +
                    "FROM seat_allocation sa " +
                    "JOIN exam e ON sa.exam_id = e.exam_id " +
                    "JOIN room r ON sa.room_id = r.room_id " +
                    "JOIN students s ON sa.student_id = s.student_id " +
                    "WHERE s.usn = ?  " +
                    "ORDER BY e.exam_date ASC LIMIT 1"; // Fetch upcoming exam

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, usn);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String examName = rs.getString("exam_name");
                    String examDate = rs.getString("exam_date");
                    String roomId = rs.getString("room_id");
                    String seatNumber = rs.getString("seat_number");

                    examDetailsArea.setText("Exam: " + examName + "\nDate: " + examDate + 
                                            "\nRoom: " + roomId + "\nSeat Number: " + seatNumber);
                } else {
                    JOptionPane.showMessageDialog(this, "No upcoming exam found for this USN.", "No Exam", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching exam details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewRoomLayout() {
        String usn = usnField.getText().trim();
        if (usn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your USN first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Fetch room details from the database for the given USN
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String query = "SELECT r.room_id, sa.seat_number " +
                    "FROM seat_allocation sa " +
                    "JOIN room r ON sa.room_id = r.room_id " +
                    "JOIN students s ON sa.student_id = s.student_id " +
                    "WHERE s.usn = ? " ; // Fetch upcoming exam details

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, usn);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String roomId = rs.getString("room_id");
                    String seatNumber = rs.getString("seat_number");

                    // Reset the room layout
                    roomPanel.removeAll();

                    // Set layout manager to GridLayout with 5 rows and 5 columns
                    roomPanel.setLayout(new GridLayout(5, 5, 10, 10));

                    // Example layout with 10 seats in each row, two columns (to mimic your design)
                    for (int i = 1; i <= 25; i++) {
                        JPanel seatContainer = new JPanel();
                        seatContainer.setLayout(new BorderLayout());

                        JButton seatButton = new JButton(String.valueOf(i));
                        seatButton.setEnabled(false);

                        // Check if this is the seat assigned to the student
                        if (String.valueOf(i).equals(seatNumber)) {
                            seatButton.setBackground(Color.YELLOW); // Highlight the student's seat
                        } else {
                            seatButton.setBackground(Color.LIGHT_GRAY); // Other seats in light gray
                        }

                        seatContainer.add(seatButton, BorderLayout.CENTER);
                        roomPanel.add(seatContainer);  // Add seat container to grid
                    }

                    roomPanel.revalidate();
                    roomPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "No room layout found for this USN.", "No Room", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching room layout", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}
