package sms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sms.model.User;

import java.util.function.Consumer;

/**
 * Login: Professional Desktop Authentication Screen.
 * 
 * Features:
 * - Clean Card UI with Light Lavender / Blue Theme
 * - Show/Hide Password Toggle
 * - Pre-login validation and MySQL database connectivity checks
 * - Meaningful error messages for authentication or database issues
 */
public class Login extends StackPane {

    private final UserDAO userDAO = new UserDAO();
    private final Consumer<User> onLoginSuccess;

    private TextField usernameField;
    private PasswordField passwordHiddenField;
    private TextField passwordVisibleField;
    private Button togglePasswordBtn;
    private boolean isPasswordVisible = false;
    private Label errorLabel;
    private Label dbStatusLabel;

    public Login(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        buildUI();
    }

    private void buildUI() {
        // Outer Container with soft lavender background
        setStyle("-fx-background-color: linear-gradient(to bottom right, #EEF3FF, #F0F0FF);");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));

        // Center Login Card
        VBox card = new VBox(18);
        card.setMaxWidth(420);
        card.setPadding(new Insets(36, 32, 36, 32));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: #FFFFFF; " +
            "-fx-background-radius: 18px; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 38, 92, 0.12), 24, 0, 0, 8);"
        );

        // Application Logo / Monogram
        StackPane logoBadge = new StackPane();
        logoBadge.setPrefSize(60, 60);
        logoBadge.setMaxSize(60, 60);
        logoBadge.setStyle(
            "-fx-background-color: #4054F4; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(64, 84, 244, 0.35), 12, 0, 0, 4);"
        );
        Label logoText = new Label("SMS");
        logoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logoText.setTextFill(Color.WHITE);
        logoBadge.getChildren().add(logoText);

        // Header Titles
        Label title = new Label("SMS");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#10265C"));

        Label subtitle = new Label("Student Management System");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        subtitle.setTextFill(Color.web("#4054F4"));

        Label desc = new Label("Sign in to your administrative desktop portal");
        desc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        desc.setTextFill(Color.web("#64748B"));

        VBox headerBox = new VBox(6, logoBadge, title, subtitle, desc);
        headerBox.setAlignment(Pos.CENTER);

        // Username Input
        VBox userBox = new VBox(6);
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        userLabel.setTextFill(Color.web("#172554"));

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username (e.g. admin)");
        usernameField.setPrefHeight(42);
        usernameField.setStyle(inputStyle());
        userBox.getChildren().addAll(userLabel, usernameField);

        // Password Input with Show/Hide Toggle
        VBox passBox = new VBox(6);
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        passLabel.setTextFill(Color.web("#172554"));

        passwordHiddenField = new PasswordField();
        passwordHiddenField.setPromptText("Enter your password");
        passwordHiddenField.setPrefHeight(42);
        passwordHiddenField.setStyle(inputStyle());

        passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Enter your password");
        passwordVisibleField.setPrefHeight(42);
        passwordVisibleField.setStyle(inputStyle());
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);

        // Synchronize fields
        passwordHiddenField.textProperty().addListener((obs, old, val) -> {
            if (!isPasswordVisible) passwordVisibleField.setText(val);
        });
        passwordVisibleField.textProperty().addListener((obs, old, val) -> {
            if (isPasswordVisible) passwordHiddenField.setText(val);
        });

        togglePasswordBtn = new Button("👁");
        togglePasswordBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-cursor: hand; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #64748B;"
        );
        togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());

        StackPane passInputStack = new StackPane(passwordHiddenField, passwordVisibleField, togglePasswordBtn);
        StackPane.setAlignment(togglePasswordBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordBtn, new Insets(0, 8, 0, 0));

        passBox.getChildren().addAll(passLabel, passInputStack);

        // Error message label
        errorLabel = new Label();
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        errorLabel.setTextFill(Color.web("#EF4444"));
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Submit Button
        Button loginBtn = new Button("Sign In to SMS");
        loginBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        loginBtn.setTextFill(Color.WHITE);
        loginBtn.setPrefHeight(44);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(
            "-fx-background-color: #4054F4; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(64, 84, 244, 0.35), 8, 0, 0, 3);"
        );
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: #3244D4; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(50, 68, 212, 0.45), 10, 0, 0, 4);"
        ));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color: #4054F4; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(64, 84, 244, 0.35), 8, 0, 0, 3);"
        ));
        loginBtn.setOnAction(e -> handleLogin());

        // Enter key shortcut
        usernameField.setOnAction(e -> handleLogin());
        passwordHiddenField.setOnAction(e -> handleLogin());
        passwordVisibleField.setOnAction(e -> handleLogin());

        // Database status footer
        dbStatusLabel = new Label();
        dbStatusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        dbStatusLabel.setTextFill(Color.web("#64748B"));
        updateDbStatus();

        // Database help link
        Hyperlink dbHelpLink = new Hyperlink("MySQL Database Settings & Help");
        dbHelpLink.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        dbHelpLink.setTextFill(Color.web("#4054F4"));
        dbHelpLink.setOnAction(e -> showDbHelpDialog());

        VBox footerBox = new VBox(4, dbStatusLabel, dbHelpLink);
        footerBox.setAlignment(Pos.CENTER);

        // Assemble Card
        card.getChildren().addAll(
            headerBox,
            userBox,
            passBox,
            errorLabel,
            loginBtn,
            new Separator(),
            footerBox
        );

        getChildren().add(card);
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordVisibleField.setText(passwordHiddenField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordHiddenField.setVisible(false);
            passwordHiddenField.setManaged(false);
            togglePasswordBtn.setText("🔒");
        } else {
            passwordHiddenField.setText(passwordVisibleField.getText());
            passwordHiddenField.setVisible(true);
            passwordHiddenField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            togglePasswordBtn.setText("👁");
        }
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = isPasswordVisible ? passwordVisibleField.getText() : passwordHiddenField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Test database connectivity
        if (!DB.checkConnection()) {
            showError("Database Connection Error:\n" + DB.getLastErrorMessage());
            updateDbStatus();
            return;
        }

        // Validate user against database
        boolean valid = userDAO.validateUser(user, pass);
        if (valid) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            User u = userDAO.getUser(user);
            if (u == null) u = new User(user, pass);
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(u);
            }
        } else {
            showError("Invalid username or password. Please verify credentials.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void updateDbStatus() {
        boolean ok = DB.checkConnection();
        if (ok) {
            if (DB.isUsingFallback()) {
                dbStatusLabel.setText("● Database Ready: Standalone Mode (Zero-Config)");
                dbStatusLabel.setTextFill(Color.web("#16A34A"));
            } else {
                dbStatusLabel.setText("● MySQL 8 Database Connected: " + DB.getDbName());
                dbStatusLabel.setTextFill(Color.web("#16A34A"));
            }
        } else {
            dbStatusLabel.setText("○ Database Offline: " + DB.getLastErrorMessage());
            dbStatusLabel.setTextFill(Color.web("#EF4444"));
        }
    }

    private void showDbHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Database Configuration");
        alert.setHeaderText("SMS Database Setup & Status");
        alert.setContentText(
            "Active Engine: " + DB.getActiveModeDescription() + "\n\n" +
            "Current Configuration:\n" +
            "• URL: " + DB.getUrl() + "\n" +
            "• Database: " + DB.getDbName() + "\n" +
            "• User: " + DB.getUser() + "\n\n" +
            "Features:\n" +
            "• Zero-Config Standalone Mode runs immediately without requiring MySQL server.\n" +
            "• If MySQL 8 Server is running on port 3306, SMS automatically connects to MySQL.\n" +
            "• You can configure or test live MySQL server connections anytime in Settings."
        );
        alert.showAndWait();
        updateDbStatus();
    }

    private String inputStyle() {
        return "-fx-background-color: #FFFFFF; " +
               "-fx-border-color: #CBD5E1; " +
               "-fx-border-radius: 8px; " +
               "-fx-background-radius: 8px; " +
               "-fx-padding: 8px 12px; " +
               "-fx-font-size: 13px; " +
               "-fx-text-fill: #172554;";
    }
}
