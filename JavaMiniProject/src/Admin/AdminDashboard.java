package Admin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminDashboard extends JFrame {
    private JTabbedPane tabbedPane;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Exam Seat Allotment System");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Dark theme colors
        Color backgroundColor = new Color(45, 45, 45);
        Color primaryColor = new Color(35, 155, 86);
        Color textColor = Color.WHITE;

        // Create header panel for logo and title
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(250, 80));
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        ImageIcon logoIcon = new ImageIcon("path_to_online_image/logo.png");  // Replace with logo path
        JLabel logoLabel = new JLabel(logoIcon);
        headerPanel.add(logoLabel);
        headerPanel.add(titleLabel);

        // Initialize Tabbed Pane with animations
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(backgroundColor);
        tabbedPane.setForeground(textColor);
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 16));

        // Add tabs with icons and dummy components
        tabbedPane.addTab("Teachers", new AddTeacher());
        
        tabbedPane.addTab("Students", new AddStudent());
        
        tabbedPane.addTab("Exam Controler", new AddExamController());
        
        tabbedPane.addTab("Add Block", new CreateBlock());
        
        tabbedPane.addTab("Add Rooms", new CreateRoom());
        
        tabbedPane.addTab("Logout", new JPanel());

        // Set the "Logout" tab button color to red
        JLabel logoutLabel = new JLabel("Logout");
        logoutLabel.setForeground(Color.RED);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfTab("Logout"), logoutLabel);

        // Add ChangeListener to detect "Logout" tab selection
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == tabbedPane.indexOfTab("Logout")) {
                    dispose(); // Dispose the current window
                    JOptionPane.showMessageDialog(null, "Logout successful!", "Logout", JOptionPane.INFORMATION_MESSAGE);
                    new Login(); // Open the login screen
                }
            }
        });
 
        // Animation for tab selection change
        tabbedPane.addChangeListener(e -> animateTabTransition());

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Set Dark Look-and-Feel
        setDarkLookAndFeel();
    }

    // Dummy panel for tab content with animations
    private JPanel createTabContentPanel(String text) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(60, 60, 60));
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 24));
        panel.add(label);
        return panel;
    }

    // Tab transition animation
    private void animateTabTransition() {
        JPanel selectedPanel = (JPanel) tabbedPane.getSelectedComponent();
        selectedPanel.setVisible(false);
        Timer timer = new Timer(10, new ActionListener() {
            int opacity = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 5;
                selectedPanel.setBackground(new Color(60, 60, 60, Math.min(opacity, 255)));
                selectedPanel.setVisible(true);
                if (opacity >= 255) ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    // Splash screen for loading
    public static void showLoadingScreen() {
        JWindow loadingScreen = new JWindow();
        loadingScreen.getContentPane().setBackground(new Color(45, 45, 45));
        loadingScreen.setSize(400, 300);
        loadingScreen.setLocationRelativeTo(null);
        
        JLabel loadingLabel = new JLabel("Loading Admin Dashboard...", JLabel.CENTER);
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 20));

        ImageIcon loadingIcon = new ImageIcon("path_to_online_image/loading.gif");  // Replace with a loading GIF
        JLabel iconLabel = new JLabel(loadingIcon, JLabel.CENTER);

        loadingScreen.setLayout(new BorderLayout());
        loadingScreen.add(iconLabel, BorderLayout.CENTER);
        loadingScreen.add(loadingLabel, BorderLayout.SOUTH);

        loadingScreen.setVisible(true);

        // Close loading screen after delay
        Timer timer = new Timer(15000, e -> loadingScreen.dispose()); // 3 seconds delay
        timer.setRepeats(false);
        timer.start();
    }

    // Apply dark theme look-and-feel
    private void setDarkLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", new Color(45, 45, 45));
            UIManager.put("info", new Color(60, 63, 65));
            UIManager.put("nimbusBase", new Color(18, 30, 49));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(45, 45, 45));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", Color.WHITE);
            UIManager.put("nimbusSelectionBackground", new Color(35, 155, 86));
            UIManager.put("text", Color.WHITE);
        } catch (Exception e) {
            System.out.println("Failed to set dark theme: " + e.getMessage());
        }
    }

    
}
