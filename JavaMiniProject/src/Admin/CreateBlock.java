package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class CreateBlock extends JPanel {
    private JTextField blockNameField;
    private JButton saveButton, refreshButton, deleteButton;
    private JTable blockTable;
    private DefaultTableModel tableModel;

    public CreateBlock() {
    	
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Manage Blocks", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        add(headerLabel, BorderLayout.NORTH);

        // Main Panel (Split into two parts)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setDividerSize(10);

        // Left side: Block Form
        JPanel formPanel = createBlockForm();
        splitPane.setLeftComponent(formPanel);

        // Right side: Block List (Table)
        JPanel tablePanel = createBlockTable();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBlocks(); // Reload the blocks into the table
            }
        });

        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBlock();
            }
        });

        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBlock();
            }
        });
        loadBlocks();
    }

    // Method to create the block form (left side)
    private JPanel createBlockForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add space around components

        // Block Name
        JLabel blockNameLabel = new JLabel("Block Name:");
        blockNameField = new JTextField(20);
        blockNameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        blockNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        blockNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(blockNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(blockNameField, gbc);

        // Buttons (Save, Refresh, Delete)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(255, 255, 255));

        saveButton = new JButton("Save Block");
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setPreferredSize(new Dimension(120, 40));

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(255, 69, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 16));
        refreshButton.setPreferredSize(new Dimension(120, 40));

        deleteButton = new JButton("Delete Block");
        deleteButton.setBackground(new Color(255, 0, 0));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteButton.setPreferredSize(new Dimension(120, 40));

        buttonPanel.add(saveButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    // Method to create the block table (right side)
    private JPanel createBlockTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        // Table to display blocks
        String[] columns = {"Block ID", "Block Name"};
        tableModel = new DefaultTableModel(columns, 0);
        blockTable = new JTable(tableModel);

        // Change the background color of the table and header
        blockTable.setBackground(new Color(240, 240, 240));  // Light gray background for the table
        blockTable.setForeground(new Color(0, 0, 0));  // Black text color
        blockTable.setFont(new Font("Arial", Font.PLAIN, 14));  // Font style and size

        // Change the header color
        blockTable.getTableHeader().setBackground(new Color(34, 139, 34));  // Green header background
        blockTable.getTableHeader().setForeground(Color.WHITE);  // White text for header
        blockTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));  // Bold font for header

        // Customizing the row selection color
        blockTable.setSelectionBackground(new Color(0, 123, 255));  // Blue selection background
        blockTable.setSelectionForeground(Color.WHITE);  // White text when selected

        JScrollPane scrollPane = new JScrollPane(blockTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }


    // Method to load all blocks from the database into the table
    private void loadBlocks() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String selectQuery = "SELECT * FROM block";
            try (PreparedStatement ps = con.prepareStatement(selectQuery); ResultSet rs = ps.executeQuery()) {
                tableModel.setRowCount(0); // Clear existing data
                while (rs.next()) {
                    int blockId = rs.getInt("block_id");
                    String blockName = rs.getString("block_name");

                    Object[] row = {blockId, blockName};
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to save the block into the database
    private void saveBlock() {
        String blockName = blockNameField.getText();

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String insertQuery = "INSERT INTO block (block_name) VALUES (?)";  // Block ID is auto-incremented
            try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                ps.setString(1, blockName);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Block saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBlocks(); // Refresh the block list after adding a new block
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving block", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to delete the selected block from the database
    private void deleteBlock() {
        int selectedRow = blockTable.getSelectedRow();

        if (selectedRow != -1) {
            int blockId = (int) tableModel.getValueAt(selectedRow, 0); // Get selected block ID

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete block " + blockId + "?", "Delete Block", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
                    String deleteQuery = "DELETE FROM block WHERE block_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(deleteQuery)) {
                        ps.setInt(1, blockId);
                        int rowsAffected = ps.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(this, "Block deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadBlocks(); // Refresh the block list after deletion
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting block", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a block to delete", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}
