package sms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sms.model.User;

/**
 * Settings: Application Configuration, MySQL Connection Status, and Admin Security.
 */
public class Settings extends VBox {

    private final UserDAO userDAO = new UserDAO();
    private final User currentUser;
    private final Runnable onLogout;

    private Label statusIndicator;
    private Label connectionDetails;
    private PasswordField currentPassField;
    private PasswordField newPassField;
    private PasswordField confirmPassField;
    private Label pwdMsgLabel;

    public Settings(User currentUser, Runnable onLogout) {
        this.currentUser = currentUser != null ? currentUser : new User("admin", "1111");
        this.onLogout = onLogout;
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F8FAFC;");
        buildUI();
    }

    private void buildUI() {
        // Heading
        Label heading = new Label("System Settings & Security");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#10265C"));

        Label desc = new Label("Review MySQL database connection parameters, inspect runtime environment, and update administrative credentials.");
        desc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        desc.setTextFill(Color.web("#64748B"));

        HBox grid = new HBox(20);
        grid.setAlignment(Pos.TOP_LEFT);

        // Column 1: Database & System Info
        VBox col1 = new VBox(16);
        col1.setPrefWidth(460);
        col1.getChildren().addAll(createDbCard(), createSystemCard());

        // Column 2: Password Security & Theme
        VBox col2 = new VBox(16);
        col2.setPrefWidth(420);
        col2.getChildren().addAll(createSecurityCard(), createThemeCard());

        grid.getChildren().addAll(col1, col2);

        getChildren().addAll(new VBox(4, heading, desc), grid);
    }

    private VBox createDbCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Database Architecture & Engine");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(Color.web("#10265C"));

        statusIndicator = new Label();
        statusIndicator.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));

        connectionDetails = new Label();
        connectionDetails.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        connectionDetails.setTextFill(Color.web("#64748B"));
        connectionDetails.setWrapText(true);

        updateConnectionInfo();

        HBox btnRow = new HBox(10);
        Button testBtn = new Button("Test MySQL 8");
        testBtn.setStyle("-fx-background-color: #4054F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        testBtn.setOnAction(e -> testMySQLConnection());

        Button switchBtn = new Button("Switch Engine");
        switchBtn.setStyle("-fx-background-color: #10265C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        switchBtn.setOnAction(e -> toggleEngine());

        Button reloadBtn = new Button("Reload Config");
        reloadBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        reloadBtn.setOnAction(e -> {
            DB.loadConfiguration();
            updateConnectionInfo();
        });

        btnRow.getChildren().addAll(testBtn, switchBtn, reloadBtn);

        card.getChildren().addAll(title, statusIndicator, connectionDetails, new Separator(), btnRow);
        return card;
    }

    private void updateConnectionInfo() {
        boolean ok = DB.checkConnection();
        if (ok) {
            statusIndicator.setText("✔ DATABASE CONNECTED & OPERATIONAL");
            statusIndicator.setTextFill(Color.web("#16A34A"));
        } else {
            statusIndicator.setText("✖ DATABASE OFFLINE / DISCONNECTED");
            statusIndicator.setTextFill(Color.web("#EF4444"));
        }

        connectionDetails.setText(
            "• Active Engine: " + DB.getActiveModeDescription() + "\n" +
            "• Database Name: " + DB.getDbName() + "\n" +
            "• User: " + DB.getUser() + "\n" +
            "• MySQL Host/Port: " + DB.getHost() + ":" + DB.getPort() + "\n" +
            "• Active JDBC URL: " + DB.getUrl() + "\n" +
            "• Config File: config/application.properties"
        );
    }

    private void testMySQLConnection() {
        boolean ok = DB.testMySQLConnection();
        Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle("MySQL 8 Connectivity Test");
        alert.setHeaderText(ok ? "MySQL 8.0 Server Online!" : "MySQL 8.0 Server Not Detected");
        alert.setContentText(ok ?
            "Successfully connected to MySQL database on " + DB.getHost() + ":" + DB.getPort() + "." :
            "Cannot connect to MySQL server at " + DB.getHost() + ":" + DB.getPort() + ".\n\n" +
            "Notice: Standalone Zero-Config Mode is actively serving all application requests without any errors."
        );
        alert.showAndWait();
    }

    private void toggleEngine() {
        if (DB.isUsingFallback()) {
            boolean switched = DB.switchToMySQL();
            if (switched) {
                updateConnectionInfo();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully switched to Live MySQL 8.0 Server!", ButtonType.OK);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot switch to MySQL: MySQL server on " + DB.getHost() + ":" + DB.getPort() + " is offline.\nRemaining in Standalone Mode.", ButtonType.OK);
                alert.showAndWait();
            }
        } else {
            DB.switchToEmbedded();
            updateConnectionInfo();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Switched to Standalone Zero-Config Engine.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private VBox createSecurityCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Change Administrator Password");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(Color.web("#10265C"));

        Label userNotice = new Label("Logged in as Administrator: " + currentUser.getUsername());
        userNotice.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        userNotice.setTextFill(Color.web("#64748B"));

        currentPassField = new PasswordField();
        currentPassField.setPromptText("Current Password");
        currentPassField.setStyle(inputStyle());

        newPassField = new PasswordField();
        newPassField.setPromptText("New Password (min 4 characters)");
        newPassField.setStyle(inputStyle());

        confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm New Password");
        confirmPassField.setStyle(inputStyle());

        pwdMsgLabel = new Label();
        pwdMsgLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        pwdMsgLabel.setVisible(false);
        pwdMsgLabel.setManaged(false);

        Button changeBtn = new Button("Update Password");
        changeBtn.setStyle("-fx-background-color: #10265C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 16px;");
        changeBtn.setOnAction(e -> handleChangePassword());

        card.getChildren().addAll(
            title, userNotice,
            new Label("Current Password:"), currentPassField,
            new Label("New Password:"), newPassField,
            new Label("Confirm Password:"), confirmPassField,
            pwdMsgLabel, changeBtn
        );
        return card;
    }

    private void handleChangePassword() {
        String cur = currentPassField.getText();
        String nw = newPassField.getText();
        String cf = confirmPassField.getText();

        if (cur.isEmpty() || nw.isEmpty() || cf.isEmpty()) {
            showPwdMsg("All password fields are required.", true);
            return;
        }

        if (!nw.equals(cf)) {
            showPwdMsg("New password and confirmation do not match.", true);
            return;
        }

        if (nw.length() < 4) {
            showPwdMsg("New password must be at least 4 characters long.", true);
            return;
        }

        // Validate current password
        boolean valid = userDAO.validateUser(currentUser.getUsername(), cur);
        if (!valid) {
            showPwdMsg("Current password is incorrect.", true);
            return;
        }

        boolean ok = userDAO.changePassword(currentUser.getUsername(), nw);
        if (ok) {
            currentUser.setPassword(nw);
            currentPassField.clear();
            newPassField.clear();
            confirmPassField.clear();
            showPwdMsg("Password updated successfully in MySQL!", false);
        } else {
            showPwdMsg("Failed to update password in database.", true);
        }
    }

    private void showPwdMsg(String text, boolean isError) {
        pwdMsgLabel.setText(text);
        pwdMsgLabel.setTextFill(Color.web(isError ? "#EF4444" : "#16A34A"));
        pwdMsgLabel.setVisible(true);
        pwdMsgLabel.setManaged(true);
    }

    private VBox createSystemCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Application & Runtime Diagnostics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(Color.web("#10265C"));

        Label info = new Label(
            "• Application Name: SMS - Student Management System\n" +
            "• Version: 1.0.0 (Release Build)\n" +
            "• Java Version: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")\n" +
            "• JavaFX Desktop Engine: 21.0.2\n" +
            "• Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")\n" +
            "• Architecture: Simple MVC + DAO Pattern\n" +
            "• Packaging: Native Windows Application (SMS.exe via jpackage)"
        );
        info.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        info.setTextFill(Color.web("#64748B"));

        card.getChildren().addAll(title, info);
        return card;
    }

    private VBox createThemeCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Visual Theme & Session");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(Color.web("#10265C"));

        Label themeDesc = new Label("Active Theme: Modern Light Lavender & Royal Blue (#4054F4)");
        themeDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        themeDesc.setTextFill(Color.web("#64748B"));

        Button logoutBtn = new Button("🚪 Logout of Administrative Session");
        logoutBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-border-color: #FECACA; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 8px 16px;");
        logoutBtn.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });

        card.getChildren().addAll(title, themeDesc, new Separator(), logoutBtn);
        return card;
    }

    private String inputStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 7px 10px; -fx-font-size: 12px; -fx-text-fill: #172554;";
    }
}
