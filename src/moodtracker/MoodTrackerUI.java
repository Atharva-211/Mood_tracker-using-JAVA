package moodtracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import javax.swing.border.Border;


public class MoodTrackerUI extends JFrame implements ActionListener {
    private JComboBox<String> moodComboBox;
    private JButton submitButton;
    private JList<String> moodList;
    private JLabel statisticsLabel;
    private JPanel centerPanel; // Declare centerPanel as a class member
    private Map<String, Integer> moodCountMap;
    private JFreeChart pieChart; // Declare pieChart at the class level
    
    

    public MoodTrackerUI() {
        setTitle("Mood Tracker");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        getContentPane().add(panel);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setPreferredSize(new Dimension(800, 100)); // Set the preferred size
        topPanel.setBackground(Color.PINK);
     // Create an inner border with bold lines
        Border innerBorder = BorderFactory.createLineBorder(Color.BLACK, 8); // 2 pixels width for the bold lines

        // Create an outer border with padding
        int topPadding = 10;
        int leftPadding = 10;
        int bottomPadding = 10;
        int rightPadding = 10;
        Border outerBorder = BorderFactory.createEmptyBorder(topPadding, leftPadding, bottomPadding, rightPadding);

        // Create a compound border with the inner and outer borders
        Border compoundBorder = BorderFactory.createCompoundBorder(outerBorder, innerBorder);

        // Set the compound border to the topPanel
        topPanel.setBorder(compoundBorder);

        JLabel moodLabel = new JLabel("Select Mood:");
        moodLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Set the font size to 20
        topPanel.add(moodLabel);

        String[] moods = {"Happy", "Angry", "Sad", "Lazy"};
        moodComboBox = new JComboBox<>(moods);
        moodComboBox.setPreferredSize(new Dimension(150, moodComboBox.getPreferredSize().height));
        moodComboBox.setFont(new Font("Arial", Font.PLAIN, 16)); // Set the font size to 16
        topPanel.add(moodComboBox);

        submitButton = new JButton("Submit");
        submitButton.addActionListener(this);
        submitButton.setFont(new Font("Arial", Font.PLAIN, 16)); // Set the font size to 16
        topPanel.add(submitButton);

        panel.add(topPanel, BorderLayout.NORTH);


     // Initialize centerPanel
        centerPanel = new JPanel(new BorderLayout());
        panel.add(centerPanel, BorderLayout.CENTER); // Add centerPanel to the content pane

        pieChart = ChartFactory.createPieChart("Mood Distribution", null, true, true, false); // Initialize pieChart

        moodList = new JList<>();
        moodList.setCellRenderer(new MoodListRenderer()); // Set custom cell renderer
        JScrollPane scrollPane = new JScrollPane(moodList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Statistics panel
        JPanel statisticsPanel = new JPanel(new BorderLayout());
        centerPanel.add(statisticsPanel, BorderLayout.SOUTH);
        statisticsLabel = new JLabel();
        statisticsPanel.add(statisticsLabel, BorderLayout.CENTER);
        statisticsPanel.setPreferredSize(new Dimension(1150, 400));

        // Initialize mood count map
        moodCountMap = new HashMap<>();
        for (String mood : moods) {
            moodCountMap.put(mood, 0);
        }
        
        // Create pie chart
        pieChart = createPieChart(new DefaultPieDataset());

        // Load data from database when UI is created
        loadDataFromDatabase();
    }

 // Add pie chart to the chartPanel
    private void addPieChartToChartPanel(JPanel chartPanel) {
        // Create the dataset for the pie chart
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : moodCountMap.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        // Create the pie chart
        JFreeChart chart = ChartFactory.createPieChart(
                "Mood Distribution",  // chart title
                dataset,             // dataset
                true,                // include legend
                true,
                false
        );

        // Create a chart panel to hold the chart
        ChartPanel chartPanelComponent = new ChartPanel(chart);
        chartPanelComponent.setPreferredSize(new Dimension(300, 300));

        // Add the chart panel to the chartPanel
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
    }

    
    private void loadDataFromDatabase() {
        // Clear existing mood count map
        moodCountMap.clear();
        
        List<String> moodData = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT mood, mood_date FROM mood";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String mood = resultSet.getString("mood");
                String datetime = resultSet.getString("mood_date");
                moodData.add(mood + " - " + datetime);
                // Update mood count map
                moodCountMap.put(mood, moodCountMap.getOrDefault(mood, 0) + 1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load data from database.");
        }

        // Set custom font for the moodList
        Font font = moodList.getFont().deriveFont(Font.PLAIN, 16); // Change 16 to the desired font size
        moodList.setFont(font);
        moodList.setListData(moodData.toArray(new String[0]));

        // Update statistics label
        updateStatisticsLabel();
    }


 // Update the statistics label and add the pie chart
    private void updateStatisticsLabel() {
        // Clear existing components from statisticsLabel
        statisticsLabel.removeAll();
        statisticsLabel.setLayout(new BorderLayout());

        // Calculate total count of moods
        int totalMoods = 0;
        for (int count : moodCountMap.values()) {
            totalMoods += count;
        }

        // Calculate highest mood
        String highestMood = "";
        int highestCount = 0;
        for (Map.Entry<String, Integer> entry : moodCountMap.entrySet()) {
            String mood = entry.getKey();
            int count = entry.getValue();
            if (count > highestCount) {
                highestCount = count;
                highestMood = mood;
            }
        }

        // Load and display image associated with the highest count mood (if it exists)
//     // Load and display GIF associated with the highest count mood (if it exists)
//        if (!highestMood.isEmpty()) {
//            ImageIcon gifIcon = new ImageIcon(getClass().getResource("/com/moodtracker/emotions/" + highestMood.toLowerCase() + ".gif"));
//            JLabel gifLabel = new JLabel(gifIcon);
//            statisticsLabel.add(gifLabel, BorderLayout.CENTER);
//        }

        

        // Load and display image associated with the highest count mood (if it exists)
        if (!highestMood.isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/emotions/" + highestMood.toLowerCase() + ".jpg"));
            Image originalImage = originalIcon.getImage();
            Image scaledImage = originalImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Adjust the width and height as needed
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(scaledIcon);
            statisticsLabel.add(imageLabel, BorderLayout.CENTER);
        }


        // Update statistics text and pie chart dataset
        DefaultPieDataset dataset = new DefaultPieDataset();
        StringBuilder statisticsText = new StringBuilder("<html><b>Statistics:</b><br/>");
        for (Map.Entry<String, Integer> entry : moodCountMap.entrySet()) {
            String mood = entry.getKey();
            int count = entry.getValue();
            double percentage = totalMoods == 0 ? 0 : ((double) count / totalMoods) * 100;
            dataset.setValue(mood, percentage); // Update pie chart dataset
            statisticsText.append("(").append(mood).append(": __").append(count).append(" __").append(String.format("%.2f", percentage)).append("%)<br/>");
        }
        statisticsText.append("</html>");
        statisticsLabel.setText(statisticsText.toString());

        // Increase font size of statisticsLabel
        Font font = statisticsLabel.getFont().deriveFont(Font.PLAIN, 20);
        statisticsLabel.setFont(font);

        // Add pie chart to the statistics panel
        if (pieChart == null) {
            pieChart = createPieChart(dataset);
        } else {
            // Update the dataset of the existing pie chart
            pieChart = ChartFactory.createPieChart("Mood Distribution", dataset, true, true, false);
        }
        addPieChartToStatisticsPanel();

        // Repaint the statisticsLabel
        statisticsLabel.revalidate();
        statisticsLabel.repaint();
    }

    private void addPieChartToStatisticsPanel() {
        // Create a chart panel to hold the pie chart
        ChartPanel chartPanel = new ChartPanel(pieChart);
        statisticsLabel.add(chartPanel, BorderLayout.EAST);
    }

    private DefaultPieDataset createPieChartDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        // Populate the dataset with mood counts
        for (Map.Entry<String, Integer> entry : moodCountMap.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }


    private JFreeChart createPieChart(DefaultPieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
                "Mood Distribution", // Chart title
                dataset, // Dataset
                true, // Include legend
                true, // Include tooltips
                false // Include URLs
        );
        return chart;
    }






    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String selectedMood = (String) moodComboBox.getSelectedItem();
            saveMoodToDatabase(selectedMood);
            JOptionPane.showMessageDialog(this, "Your mood has been recorded successfully!");
            // Reload data from database after saving a new mood
            loadDataFromDatabase();
        }
    }

    private void saveMoodToDatabase(String mood) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO mood (mood) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, mood);
            statement.executeUpdate();
            
            // After saving the mood to the database, update the UI to reflect the changes
            updateStatisticsLabel();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save mood to database.");
        }
    }


    // Custom cell renderer class
    class MoodListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String moodText = value.toString();
            if (moodText.contains("Happy")) {
                renderer.setForeground(Color.ORANGE);
            } else if (moodText.contains("Angry")) {
                renderer.setForeground(Color.RED);
            } else if (moodText.contains("Sad")) {
                renderer.setForeground(Color.BLUE);
            } else if (moodText.contains("Lazy")) {
                renderer.setForeground(Color.GREEN);
            }
            return renderer;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Set look and feel to system default
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MoodTrackerUI ui = new MoodTrackerUI();
            ui.setVisible(true);
        });
    }
}