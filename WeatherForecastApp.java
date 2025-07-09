import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;

public class WeatherForecastApp extends JFrame {
    // Constants
    private static final String APP_TITLE = "Weather Forecast";
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font VALUE_FONT = new Font("Arial", Font.BOLD, 16);
    
    // UI Components
    private JTextField locationField;
    private JLabel locationLabel, lastUpdatedLabel;
    private JLabel[] weatherInfoLabels;
    private JPanel forecastPanel;
    private JLabel statusLabel;
    
    // Services
    private WeatherService weatherService;
    private LocationService locationService;
    
    // Weather icons
    private static final Map<String, String> WEATHER_ICONS = createWeatherIcons();
    
    public WeatherForecastApp() {
        initializeServices();
        initializeUI();
        setLocationByIp();
    }
    
    private void initializeServices() {
        weatherService = new WeatherService();
        locationService = new LocationService();
    }
    
    private static Map<String, String> createWeatherIcons() {
        Map<String, String> icons = new HashMap<>();
        icons.put("clear", "☀️");
        icons.put("sunny", "☀️");
        icons.put("cloudy", "☁️");
        icons.put("partly cloudy", "⛅");
        icons.put("rain", "🌧️");
        icons.put("thunderstorm", "⛈️");
        icons.put("snow", "❄️");
        icons.put("fog", "🌫️");
        return icons;
    }
    
    private void initializeUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(700, 550));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Title
        JLabel title = new JLabel(APP_TITLE, SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(PRIMARY_COLOR);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        
        locationField = new JTextField(20);
        locationField.setFont(LABEL_FONT);
        locationField.addActionListener(e -> fetchWeather());
        
        JButton searchBtn = createButton("Search", new Color(34, 139, 34), e -> fetchWeather());
        JButton locationBtn = createButton("📍 My Location", new Color(255, 140, 0), e -> setLocationByIp());
        JButton refreshBtn = createButton("🔄 Refresh", new Color(30, 144, 255), e -> refreshWeather());
        
        searchPanel.add(new JLabel("Location:"));
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(locationField);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchBtn);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(locationBtn);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(refreshBtn);
        
        header.add(searchPanel, BorderLayout.SOUTH);
        return header;
    }
    
    private JButton createButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        button.setMargin(new Insets(5, 10, 5, 10)); // Added padding for better button appearance
        return button;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(SECONDARY_COLOR);
        
        mainPanel.add(createCurrentWeatherPanel(), BorderLayout.NORTH);
        mainPanel.add(createForecastScrollPane(), BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createCurrentWeatherPanel() {
        JPanel currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBorder(BorderFactory.createTitledBorder("Current Weather"));
        currentPanel.setBackground(Color.WHITE);
        
        // Header with location and update time
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        
        locationLabel = new JLabel("No location selected");
        locationLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        lastUpdatedLabel = new JLabel("Last updated: Never");
        lastUpdatedLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        lastUpdatedLabel.setForeground(Color.GRAY);
        
        header.add(locationLabel, BorderLayout.WEST);
        header.add(lastUpdatedLabel, BorderLayout.EAST);
        
        // Weather info grid
        JPanel infoGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        infoGrid.setBackground(Color.WHITE);
        infoGrid.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] labels = {"Condition", "Temperature", "Humidity", "Wind", "Pressure", "Visibility"};
        weatherInfoLabels = new JLabel[labels.length];
        
        for (int i = 0; i < labels.length; i++) {
            JPanel cell = new JPanel(new BorderLayout());
            JLabel label = new JLabel(labels[i] + ":", SwingConstants.CENTER);
            label.setFont(LABEL_FONT);
            cell.add(label, BorderLayout.NORTH);
            
            weatherInfoLabels[i] = new JLabel("--", SwingConstants.CENTER);
            weatherInfoLabels[i].setFont(VALUE_FONT);
            if (i == 1) weatherInfoLabels[i].setForeground(new Color(220, 20, 60));
            
            cell.add(weatherInfoLabels[i], BorderLayout.CENTER);
            infoGrid.add(cell);
        }
        
        currentPanel.add(header, BorderLayout.NORTH);
        currentPanel.add(infoGrid, BorderLayout.CENTER);
        return currentPanel;
    }
    
    private JScrollPane createForecastScrollPane() {
        forecastPanel = new JPanel();
        forecastPanel.setLayout(new BoxLayout(forecastPanel, BoxLayout.Y_AXIS));
        forecastPanel.setBackground(SECONDARY_COLOR);
        forecastPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Added padding
        
        JScrollPane scrollPane = new JScrollPane(forecastPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("5-Day Forecast"));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(230, 230, 250));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(LABEL_FONT);
        
        statusBar.add(new JLabel("Status:"));
        statusBar.add(statusLabel);
        return statusBar;
    }
    
    private void setLocationByIp() {
        updateStatus("Detecting location...");
        
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return locationService.getCurrentLocation();
            }
            
            @Override
            protected void done() {
                try {
                    String location = get();
                    if (location != null && !location.trim().isEmpty()) {
                        locationField.setText(location);
                        fetchWeather();
                    } else {
                        showWarning("Could not detect location. Please enter manually.");
                    }
                } catch (Exception e) {
                    showError("Location detection failed: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void fetchWeather() {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            showWarning("Please enter a location");
            return;
        }
        
        updateStatus("Loading weather data for " + location + "...");
        
        new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                return weatherService.getWeatherData(location);
            }
            
            @Override
            protected void done() {
                try {
                    WeatherData data = get();
                    if (data != null) {
                        updateUI(data);
                        updateStatus("Weather data loaded successfully");
                    } else {
                        showError("Could not retrieve weather data for " + location);
                    }
                } catch (Exception e) {
                    showError("Error loading weather data: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void refreshWeather() {
        if (locationField.getText().trim().isEmpty()) {
            showWarning("No location to refresh");
            return;
        }
        fetchWeather();
    }
    
    private void updateUI(WeatherData data) {
        // Update current weather
        SwingUtilities.invokeLater(() -> {
            locationLabel.setText(data.getLocationName());
            
            String condition = data.getCurrentCondition();
            weatherInfoLabels[0].setText(getWeatherIcon(condition) + " " + condition);
            weatherInfoLabels[1].setText(data.getCurrentTemp() + "°C");
            weatherInfoLabels[2].setText(data.getHumidity() + "%");
            weatherInfoLabels[3].setText(data.getWindSpeed() + " km/h " + data.getWindDirection());
            weatherInfoLabels[4].setText(data.getPressure() + " hPa");
            weatherInfoLabels[5].setText(data.getVisibility() + " km");
            
            // Update timestamp
            lastUpdatedLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            
            // Update forecast
            updateForecastUI(data.getForecast());
        });
    }
    
    private void updateForecastUI(List<ForecastDay> forecast) {
        SwingUtilities.invokeLater(() -> {
            forecastPanel.removeAll();
            
            if (forecast != null && !forecast.isEmpty()) {
                for (ForecastDay day : forecast) {
                    forecastPanel.add(createForecastDayPanel(day));
                    forecastPanel.add(Box.createVerticalStrut(5));
                }
            } else {
                JLabel noDataLabel = new JLabel("No forecast data available", SwingConstants.CENTER);
                noDataLabel.setFont(LABEL_FONT);
                forecastPanel.add(noDataLabel);
            }
            
            forecastPanel.revalidate();
            forecastPanel.repaint();
        });
    }
    
    private JPanel createForecastDayPanel(ForecastDay day) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Date
        JLabel dateLabel = new JLabel(day.getDate());
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Weather info
        JPanel infoPanel = new JPanel(new GridLayout(1, 4));
        infoPanel.setBackground(Color.WHITE);
        
        String condition = getWeatherIcon(day.getCondition()) + " " + day.getCondition();
        infoPanel.add(createForecastLabel(condition));
        infoPanel.add(createForecastLabel("H: " + day.getMaxTemp() + "° L: " + day.getMinTemp() + "°"));
        infoPanel.add(createForecastLabel("💧 " + day.getHumidity() + "%"));
        infoPanel.add(createForecastLabel("💨 " + day.getWindSpeed() + " km/h"));
        
        panel.add(dateLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JLabel createForecastLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(LABEL_FONT);
        return label;
    }
    
    private String getWeatherIcon(String condition) {
        if (condition == null) return "🌤️";
        
        String lowerCondition = condition.toLowerCase();
        return WEATHER_ICONS.entrySet().stream()
                .filter(entry -> lowerCondition.contains(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse("🌤️");
    }
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
    
    private void showWarning(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
            updateStatus("Warning: " + message);
        });
    }
    
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            updateStatus("Error: " + message);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new WeatherForecastApp().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error setting system look and feel: " + e.getMessage(), 
                    "Initialization Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

// Dummy service classes to make the code compile
class WeatherService {
    public WeatherData getWeatherData(String location) {
        // Mock implementation
        WeatherData data = new WeatherData();
        data.setLocationName(location);
        data.setCurrentCondition("Sunny");
        data.setCurrentTemp(25);
        data.setHumidity(65);
        data.setWindSpeed(12);
        data.setWindDirection("NW");
        data.setPressure(1013);
        data.setVisibility(10);
        
        List<ForecastDay> forecast = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ForecastDay day = new ForecastDay();
            day.setDate("Day " + i);
            day.setCondition(i % 2 == 0 ? "Sunny" : "Cloudy");
            day.setMaxTemp(20 + i);
            day.setMinTemp(10 + i);
            day.setHumidity(60 + i);
            day.setWindSpeed(5 + i);
            forecast.add(day);
        }
        data.setForecast(forecast);
        
        return data;
    }
}

class LocationService {
    public String getCurrentLocation() {
        // Mock implementation
        return "New York, US";
    }
}

class WeatherData {
    private String locationName;
    private String currentCondition;
    private int currentTemp;
    private int humidity;
    private int windSpeed;
    private String windDirection;
    private int pressure;
    private int visibility;
    private List<ForecastDay> forecast;
    
    // Getters and setters
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }
    public int getCurrentTemp() { return currentTemp; }
    public void setCurrentTemp(int currentTemp) { this.currentTemp = currentTemp; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public int getWindSpeed() { return windSpeed; }
    public void setWindSpeed(int windSpeed) { this.windSpeed = windSpeed; }
    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }
    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }
    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }
    public List<ForecastDay> getForecast() { return forecast; }
    public void setForecast(List<ForecastDay> forecast) { this.forecast = forecast; }
}

class ForecastDay {
    private String date;
    private String condition;
    private int maxTemp;
    private int minTemp;
    private int humidity;
    private int windSpeed;
    
    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public int getMaxTemp() { return maxTemp; }
    public void setMaxTemp(int maxTemp) { this.maxTemp = maxTemp; }
    public int getMinTemp() { return minTemp; }
    public void setMinTemp(int minTemp) { this.minTemp = minTemp; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public int getWindSpeed() { return windSpeed; }
    public void setWindSpeed(int windSpeed) { this.windSpeed = windSpeed; }
}