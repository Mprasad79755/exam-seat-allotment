package ExamController;

import Admin.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Dashboard extends JFrame {

    private JTabbedPane tabbedPane;

    public Dashboard() {
        // Set title and full-screen window
        setTitle("Exam Controller Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
//        setUndecorated(true); // Remove window borders and controls (optional)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the main panel with a layout manager
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(34, 34, 34)); // Dark background color
        setContentPane(contentPanel);

        // Create a tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // Allow scrolling for tabs if needed
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));

        // Add tabs to the tabbed pane
        tabbedPane.addTab("Add Teacher", new AddTeacher());
        tabbedPane.addTab("Add Student", new AddStudent());
        tabbedPane.addTab("Exams", new CreateExam());
        tabbedPane.addTab("Allotment", new AllotmentSystem());
        tabbedPane.addTab("View Allotments", new ViewRoom());
     // Create a Logout tab (if it doesn't exist already)
        tabbedPane.addTab("Logout", null); // Add the "Logout" tab if it's not already present

        // Set the logout label as the tab component for the "Logout" tab
        JLabel logoutLabel = new JLabel("Logout");
        logoutLabel.setForeground(Color.RED);

        // Set the tab component for the "Logout" tab
        tabbedPane.setTabComponentAt(tabbedPane.indexOfTab("Logout"), logoutLabel);

        // Add ChangeListener to detect "Logout" tab selection
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Check if the "Logout" tab is selected
                if (tabbedPane.getSelectedIndex() == tabbedPane.indexOfTab("Logout")) {
                    dispose(); // Dispose the current window
                    JOptionPane.showMessageDialog(null, "Logout successful!", "Logout", JOptionPane.INFORMATION_MESSAGE);
                    new Login(); // Open the login screen
                }
            }
        });


        // Add the tabbed pane to the content panel
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Create a panel for the header or navigation bar
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(28, 28, 28)); // Darker background for the header
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Add a logo or title in the header (You can replace this with an actual logo image)
        JLabel titleLabel = new JLabel("Exam Controller Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Add header panel to the top
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Set the window visibility
        setVisible(true);
    }

    // Create a dummy tab for "Add Teacher"
    private JPanel createTeacherTab() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 50));
        panel.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea("Add Teacher functionality will be here.");
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(60, 60, 60));
        textArea.setEditable(false);
        
        panel.add(textArea, BorderLayout.CENTER);
        
        return panel;
    }

    // Create a dummy tab for "Add Student"
    private JPanel createStudentTab() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 50));
        panel.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea("Add Student functionality will be here.");
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(60, 60, 60));
        textArea.setEditable(false);

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    // Create a dummy tab for "Add Examiner"
    private JPanel createExaminerTab() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 50));
        panel.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea("Add Examiner functionality will be here.");
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(60, 60, 60));
        textArea.setEditable(false);

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    // Create a dummy tab for "Reports"
    private JPanel createReportsTab() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 50));
        panel.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea("Reports functionality will be here.");
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(60, 60, 60));
        textArea.setEditable(false);

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    // Create a dummy tab for "Settings"
    private JPanel createSettingsTab() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(50, 50, 50));
        panel.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea("Settings functionality will be here.");
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(60, 60, 60));
        textArea.setEditable(false);

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    // Main method to launch the dashboard
    
}

