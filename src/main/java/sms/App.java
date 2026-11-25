package sms;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sms.model.User;

import java.io.InputStream;

/**
 * App: Main JavaFX Desktop Application Entry Point for SMS.
 * 
 * Application: SMS — Student Management System
 * Final Executable: SMS.exe
 */
public class App extends Application {

    private static final String APP_TITLE = "SMS — Student Management System";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 680;

    private Stage primaryStage;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        System.out.println("==================================================");
        System.out.println("      SMS — STUDENT MANAGEMENT SYSTEM             ");
        System.out.println("==================================================");
        System.out.println("[1/2] Connecting to MySQL Database...");

        // Initialize MySQL Database Tables (idempotent)
        DB.init();

        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);

        // Set Application Desktop Icon
        try {
            stage.getIcons().add(generateAppIcon());
        } catch (Exception ignored) {}

        // Start at Login Screen
        showLoginScreen();

        stage.show();
        System.out.println("[2/2] SMS Desktop Application Window successfully displayed.");
    }

    public void showLoginScreen() {
        Login loginView = new Login(this::handleLoginSuccess);
        if (scene == null) {
            scene = new Scene(loginView, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(loginView);
        }
    }

    private void handleLoginSuccess(User user) {
        System.out.println("[AUTH] Administrator '" + user.getUsername() + "' logged in successfully.");
        Dashboard dashboardView = new Dashboard(user, this::showLoginScreen);
        scene.setRoot(dashboardView);
    }

    private Image generateAppIcon() {
        // High-res crisp application icon with royal blue rounded badge
        int size = 64;
        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();
        Color bg = Color.web("#4054F4");
        Color inner = Color.WHITE;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // Rounded corner check (radius 16)
                int r = 16;
                boolean inCorner = false;
                if (x < r && y < r && Math.hypot(x - r, y - r) > r) inCorner = true;
                if (x >= size - r && y < r && Math.hypot(x - (size - r), y - r) > r) inCorner = true;
                if (x < r && y >= size - r && Math.hypot(x - r, y - (size - r)) > r) inCorner = true;
                if (x >= size - r && y >= size - r && Math.hypot(x - (size - r), y - (size - r)) > r) inCorner = true;

                if (!inCorner) {
                    // Simple cross / academic cap silhouette
                    if (y >= 18 && y <= 24 && x >= 14 && x <= 50) {
                        pw.setColor(x, y, inner);
                    } else if (x >= 28 && x <= 36 && y >= 14 && y <= 48) {
                        pw.setColor(x, y, inner);
                    } else {
                        pw.setColor(x, y, bg);
                    }
                }
            }
        }
        return img;
    }

    @Override
    public void stop() {
        System.out.println("Closing SMS Desktop Application... Goodbye!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
