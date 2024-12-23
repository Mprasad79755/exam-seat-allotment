package ExamController;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel backgroundPanel;
    
    public Login() {
        // Set the title of the window
        setTitle("Exam Controller Login");

        // Set the layout to null to control the component positioning
        setLayout(null);
        
        // Set the size and location of the frame
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Background panel
        backgroundPanel = new JPanel();
        backgroundPanel.setBounds(0, 0, 600, 400);
        backgroundPanel.setBackground(new Color(34, 34, 34)); // Dark theme background
        backgroundPanel.setLayout(null);
        
        // Create a title label
        JLabel titleLabel = new JLabel("EXAM CONTROLLER LOGIN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(150, 50, 300, 40);
        backgroundPanel.add(titleLabel);
        
        // Create username label and text field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setBounds(100, 130, 100, 30);
        backgroundPanel.add(usernameLabel);
        
        usernameField = new JTextField();
        usernameField.setBounds(200, 130, 250, 30);
        usernameField.setBackground(new Color(50, 50, 50));
        usernameField.setForeground(Color.WHITE);
        usernameField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        backgroundPanel.add(usernameField);
        
        // Create password label and password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setBounds(100, 180, 100, 30);
        backgroundPanel.add(passwordLabel);
        
        passwordField = new JPasswordField();
        passwordField.setBounds(200, 180, 250, 30);
        passwordField.setBackground(new Color(50, 50, 50));
        passwordField.setForeground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        backgroundPanel.add(passwordField);
        
        // Create the login button
        loginButton = new JButton("Login");
        loginButton.setBounds(200, 230, 250, 40);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(34, 139, 34)); // Green button
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        backgroundPanel.add(loginButton);
        
        // Add ActionListener for login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                // Validate credentials from the database
                if (validateUser(username, password)) {
                    // Show success message and navigate to the dashboard
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    // Redirect to the dashboard
                    openDashboard();
                } else {
                    // Show error message
                    JOptionPane.showMessageDialog(null, "Invalid credentials. Please try again.");
                }
            }
        });
        
        // Add the background panel to the frame
        add(backgroundPanel);
    }

    // Method to validate user credentials from the database
    private boolean validateUser(String username, String password) {
        boolean isValid = false;

        // Database connection details
        String dbUrl = "jdbc:mysql://localhost:3306/ExamSeat"; // Your DB URL
        String dbUser = "root";
        String dbPassword = "mprasad";

        // SQL query to check if the user exists in the database
        String query = "SELECT * FROM exam_controllers WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters for the prepared statement
            stmt.setString(1, username);
            stmt.setString(2, password); // For simplicity, no encryption here, consider using hashed password

            ResultSet rs = stmt.executeQuery();

            // Check if user exists
            if (rs.next()) {
                isValid = true; // Valid credentials
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database.");
        }
        return isValid;
    }

    // Method to open the dashboard after successful login
    private void openDashboard() {
        // For now, just open a simple dialog or another window (this could be replaced with your actual dashboard window)
        Dashboard adminDashboard = new Dashboard();
        adminDashboard.setVisible(true);
    }

    // Main method to launch the login window
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}
