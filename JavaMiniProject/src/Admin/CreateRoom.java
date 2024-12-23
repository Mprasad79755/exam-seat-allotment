package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class CreateRoom extends JPanel {
    private JTextField roomNameField, capacityField;
    private JButton saveButton, refreshButton, deleteButton;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> blockComboBox;  // ComboBox for selecting block

    public CreateRoom() {
    	
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Manage Rooms", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        add(headerLabel, BorderLayout.NORTH);

        // Main Panel (Split into two parts)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setDividerSize(10);

        // Left side: Room Form
        JPanel formPanel = createRoomForm();
        splitPane.setLeftComponent(formPanel);

        // Right side: Room List (Table)
        JPanel tablePanel = createRoomTable();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRooms(); // Reload the rooms into the table
            }
        });

        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRoom();
            }
        });

        // Delete button action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRoom();
            }
        });
        loadRooms();
    }

    // Method to create the room form (left side)
    private JPanel createRoomForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add space around components

        // Room Name
        JLabel roomNameLabel = new JLabel("Room Name:");
        roomNameField = new JTextField(20);
        roomNameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        roomNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        roomNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(roomNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(roomNameField, gbc);

        // Room Capacity
        JLabel capacityLabel = new JLabel("Room Capacity:");
        capacityField = new JTextField(20);
        capacityField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        capacityField.setFont(new Font("Arial", Font.PLAIN, 14));
        capacityLabel.setFont(new Font("Arial", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(capacityLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(capacityField, gbc);

        // Block Selection (ComboBox)
        JLabel blockLabel = new JLabel("Select Block:");
        blockComboBox = new JComboBox<>();
        blockComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        loadBlocks();

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(blockLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(blockComboBox, gbc);

        // Buttons (Save, Refresh, Delete)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(255, 255, 255));

        saveButton = new JButton("Save Room");
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setPreferredSize(new Dimension(120, 40));

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(255, 69, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 16));
        refreshButton.setPreferredSize(new Dimension(120, 40));

        deleteButton = new JButton("Delete Room");
        deleteButton.setBackground(new Color(255, 0, 0));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteButton.setPreferredSize(new Dimension(120, 40));

        buttonPanel.add(saveButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    // Method to create the room table (right side)
    private JPanel createRoomTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        // Table to display rooms
        String[] columns = {"Room ID", "Room Name", "Capacity", "Block"};
        tableModel = new DefaultTableModel(columns, 0);
        roomTable = new JTable(tableModel);

        // Change the background color of the table and header
        roomTable.setBackground(new Color(240, 240, 240));  // Light gray background for the table
        roomTable.setForeground(new Color(0, 0, 0));  // Black text color
        roomTable.setFont(new Font("Arial", Font.PLAIN, 14));  // Font style and size

        // Change the header color
        roomTable.getTableHeader().setBackground(new Color(34, 139, 34));  // Green header background
        roomTable.getTableHeader().setForeground(Color.WHITE);  // White text for header
        roomTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));  // Bold font for header

        // Customizing the row selection color
        roomTable.setSelectionBackground(new Color(0, 123, 255));  // Blue selection background
        roomTable.setSelectionForeground(Color.WHITE);  // White text when selected

        JScrollPane scrollPane = new JScrollPane(roomTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }


    // Method to load all rooms from the database into the table
    private void loadRooms() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String selectQuery = "SELECT r.room_id, r.room_name, r.capacity, b.block_name FROM room r LEFT JOIN block b ON r.block_id = b.block_id";
            try (PreparedStatement ps = con.prepareStatement(selectQuery); ResultSet rs = ps.executeQuery()) {
                tableModel.setRowCount(0); // Clear existing data
                while (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    String roomName = rs.getString("room_name");
                    int capacity = rs.getInt("capacity");
                    String blockName = rs.getString("block_name");

                    Object[] row = {roomId, roomName, capacity, blockName};
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to load blocks into the combo box
    private void loadBlocks() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            String selectQuery = "SELECT block_id, block_name FROM block";
            try (PreparedStatement ps = con.prepareStatement(selectQuery); ResultSet rs = ps.executeQuery()) {
                blockComboBox.removeAllItems();
                while (rs.next()) {
                    int blockId = rs.getInt("block_id");
                    String blockName = rs.getString("block_name");
                    blockComboBox.addItem(blockName + " (ID: " + blockId + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to save the room into the database
    private void saveRoom() {
        String roomName = roomNameField.getText();
        String capacity = capacityField.getText();
        String selectedBlock = (String) blockComboBox.getSelectedItem();
        int blockId = Integer.parseInt(selectedBlock.split(":")[1].replace(")", "").trim());

        // Validate Capacity
        if (capacity.isEmpty() || !capacity.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid capacity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
            // Check if the block already has a room
            String checkBlockQuery = "SELECT COUNT(*) FROM room WHERE block_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkBlockQuery)) {
                checkStmt.setInt(1, blockId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int roomCount = rs.getInt(1);
                

                String insertQuery = "INSERT INTO room (room_name, capacity, block_id) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                    ps.setString(1, roomName);
                    ps.setInt(2, Integer.parseInt(capacity));
                    ps.setInt(3, blockId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Room added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadRooms();  // Refresh the room list
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to delete a selected room from the database
    private void deleteRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow != -1) {
            int roomId = (int) roomTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this room?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ExamSeat", "root", "mprasad")) {
                    String deleteQuery = "DELETE FROM room WHERE room_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(deleteQuery)) {
                        ps.setInt(1, roomId);
                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Room deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadRooms();  // Refresh the room list
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
