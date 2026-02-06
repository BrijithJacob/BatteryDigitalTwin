# Battery Digital Twin - Java Application

A Java implementation of a battery monitoring and prediction system using neural networks.

## Overview

This application monitors battery health and uses machine learning to predict future battery states. It features:

- Real-time battery monitoring
- Neural network-based predictions
- Visual battery state display
- Power management alerts
- Cross-platform support (with OS-specific implementations)

## Architecture

### Interfaces

- **IBatteryPredictionModel**: Interface for battery prediction models
- **INeuralNetwork**: Interface for neural network implementations
- **IBatteryInfoReader**: Interface for OS-specific battery information reading

### Core Classes

- **BatteryState**: Represents current battery state
- **PredictionResult**: Encapsulates prediction outputs with uncertainty
- **BatteryDataRecord**: Database record for historical battery data
- **ValidationError**: Stores prediction error metrics

### Model Classes

- **MLBatteryModel**: Main ML prediction model implementing IBatteryPredictionModel
- **SimpleNeuralNetwork**: Neural network implementation
- **StandardScaler**: Feature normalization

### Data Management

- **BatteryDatabase**: SQLite database handler for battery history

### System Integration

- **PowerManagement**: OS-specific power saving controls
- **AlertSystem**: System notifications and alerts
- **MacOSBatteryReader**: macOS-specific battery reading
- **MockBatteryReader**: Mock implementation for testing

### UI Components

- **BatteryVisualizer**: Main GUI application
- **BatteryPanel**: Custom battery visualization panel

### Utility Classes

- **TrainingHistory**: Stores neural network training metrics

## Dependencies

### Required

- Java 11 or higher
- SQLite JDBC driver (`org.xerial:sqlite-jdbc:3.41.2.1`)

### Optional (for different OS platforms)

- macOS: No additional dependencies (uses `ioreg`)
- Windows: Requires PowerShell
- Linux: Requires `cpufreq-utils`

## Compilation

### Using javac (with SQLite JDBC)

```bash
# Download SQLite JDBC driver
wget https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.41.2.1/sqlite-jdbc-3.41.2.1.jar

# Compile all Java files
javac -cp ".:sqlite-jdbc-3.41.2.1.jar" *.java

# Run the application
java -cp ".:sqlite-jdbc-3.41.2.1.jar" BatteryVisualizer
```

### Using Maven

Create a `pom.xml` file:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.battery</groupId>
    <artifactId>battery-twin</artifactId>
    <version>1.0</version>
    
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.41.2.1</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
            </plugin>
        </plugins>
    </build>
</project>
```

Then compile and run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="BatteryVisualizer"
```

### Using Gradle

Create a `build.gradle` file:

```gradle
plugins {
    id 'java'
    id 'application'
}

group = 'com.battery'
version = '1.0'

sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.xerial:sqlite-jdbc:3.41.2.1'
}

application {
    mainClass = 'BatteryVisualizer'
}
```

Then compile and run:

```bash
gradle build
gradle run
```

## Usage

### Starting the Application

Run the main class:

```bash
java -cp ".:sqlite-jdbc-3.41.2.1.jar" BatteryVisualizer
```

### UI Controls

- **Left Panel**: Shows current real-time battery state
- **Right Panel**: Shows neural network predicted future state
- **Slider**: Adjust prediction time horizon (0-10 hours)
- **Status Bar**: Shows training status and prediction confidence

### Features

1. **Real-time Monitoring**: Updates every 2 seconds
2. **ML Predictions**: Neural network trained on historical data
3. **Uncertainty Visualization**: Shows prediction confidence intervals
4. **Power Alerts**: Notifies when battery is predicted to run low
5. **Automatic Power Saving**: Enables power saving mode when needed

## Database

The application uses SQLite to store battery history:

- **File**: `battery_health.db`
- **Table**: `battery_health`
- **Location**: Same directory as application

### Schema

```sql
CREATE TABLE battery_health (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp TEXT NOT NULL,
    charge_percentage REAL NOT NULL,
    temperature REAL,
    voltage REAL,
    is_charging TEXT,
    daily_max_soc REAL,
    current REAL
);
```

## Extending the Application

### Adding a New OS Battery Reader

1. Implement `IBatteryInfoReader`
2. Override `readBatteryInfo()`, `getBatteryHealth()`, and `isBatteryAvailable()`
3. Update `BatteryVisualizer` to instantiate your reader based on OS detection

### Using a Different ML Framework

1. Implement `INeuralNetwork` interface
2. Override `train()` and `predict()` methods
3. Update `MLBatteryModel` to use your implementation

### Customizing Alerts

Modify `AlertSystem` class to:
- Change notification style
- Add custom alert conditions
- Integrate with external notification services

## Limitations

1. **Neural Network**: Simple implementation for demonstration
2. **Training Data**: Requires sufficient historical data
3. **OS Support**: Full features only on macOS; other OS use simulation
4. **Predictions**: Accuracy depends on usage patterns

## Performance

- Memory: ~100-200 MB
- CPU: Minimal (background updates)
- Training: 30-60 seconds on 10K records

## Troubleshooting

### "Database locked" error
- Close other instances of the application
- Check file permissions on `battery_health.db`

### No battery information on Windows/Linux
- Application will use mock data for demonstration
- Implement Windows/Linux specific readers for real data

### Prediction accuracy issues
- Collect more historical data (run for several days)
- Adjust neural network architecture in `SimpleNeuralNetwork`
- Tune hyperparameters in `MLBatteryModel`

## License

Educational/Research purposes

## Future Enhancements

- [ ] Integration with TensorFlow Java API
- [ ] More sophisticated neural network architectures
- [ ] Cloud-based model training
- [ ] Multi-device battery health comparison
- [ ] Battery health degradation prediction
- [ ] Web dashboard interface
