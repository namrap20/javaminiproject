package sms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sms.model.Course;
import sms.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard: Main Administrative Shell for SMS Desktop Application.
 * 
 * Includes:
 * - Persistent Navigation Sidebar with active state highlights
 * - Global Header Bar with Search, Admin Profile, and Settings
 * - Dynamic Main Content Area supporting all core modules
 * - Real MySQL Statistics Cards and 4 Real-Time JavaFX Charts
 */
public class Dashboard extends BorderPane {

    private final User currentUser;
    private final Runnable onLogout;

    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicDAO academicDAO = new AcademicDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();

    // Module Views (Lazy or Cached)
    private Student studentView;
    private Academic academicView;
    private Attendance attendanceView;
    private Exam examView;
    private Reports reportsView;
    private Settings settingsView;
    private ScrollPane overviewView;

    private StackPane contentStack;
    private Label pageTitleLabel;
    private TextField topSearchField;

    // Sidebar buttons for active highlight
    private final Map<String, Button> sidebarButtons = new LinkedHashMap<>();

    public Dashboard(User currentUser, Runnable onLogout) {
        this.currentUser = currentUser != null ? currentUser : new User("admin", "1111");
        this.onLogout = onLogout;

        setStyle("-fx-background-color: #F8FAFC;");

        setLeft(createSidebar());
        setTop(createTopbar());

        contentStack = new StackPane();
        contentStack.setStyle("-fx-background-color: #F8FAFC;");
        setCenter(contentStack);

        // Load default overview
        showSection("Dashboard");
    }

    // ========================================================
    // SIDEBAR
    // ========================================================
    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);
        sidebar.setStyle("-fx-background-color: #10265C;");
        sidebar.setPadding(new Insets(20, 12, 20, 12));

        // Brand Banner
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(6, 10, 20, 10));

        StackPane brandIcon = new StackPane();
        brandIcon.setPrefSize(38, 38);
        brandIcon.setStyle("-fx-background-color: #4054F4; -fx-background-radius: 10px;");
        Label iconLbl = new Label("SMS");
        iconLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        iconLbl.setTextFill(Color.WHITE);
        brandIcon.getChildren().add(iconLbl);

        VBox brandText = new VBox(2);
        Label titleLbl = new Label("SMS PORTAL");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLbl.setTextFill(Color.WHITE);

        Label subLbl = new Label("Student Management");
        subLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        subLbl.setTextFill(Color.web("#94A3B8"));
        brandText.getChildren().addAll(titleLbl, subLbl);

        brand.getChildren().addAll(brandIcon, brandText);

        // Navigation Items
        VBox navBox = new VBox(6);
        navBox.getChildren().addAll(
            createNavButton("📊  Dashboard", "Dashboard"),
            createNavButton("👨‍🎓  Students", "Students"),
            createNavButton("🏛  Academic Management", "Academic Management"),
            createNavButton("📅  Attendance", "Attendance"),
            createNavButton("📝  Examinations & Results", "Examinations & Results"),
            createNavButton("📈  Reports", "Reports"),
            createNavButton("⚙  Settings", "Settings")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout Button at bottom
        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setPadding(new Insets(10, 16, 10, 16));
        logoutBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        logoutBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-background-radius: 8px;"
        );
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-background-radius: 8px;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-background-radius: 8px;"));
        logoutBtn.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });

        sidebar.getChildren().addAll(brand, navBox, spacer, new Separator(), logoutBtn);
        return sidebar;
    }

    private Button createNavButton(String text, String sectionKey) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-cursor: hand; -fx-background-radius: 8px;");

        btn.setOnAction(e -> showSection(sectionKey));
        sidebarButtons.put(sectionKey, btn);
        return btn;
    }

    private void updateActiveNav(String activeKey) {
        for (Map.Entry<String, Button> entry : sidebarButtons.entrySet()) {
            Button b = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(activeKey)) {
                b.setStyle("-fx-background-color: #4054F4; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8px;");
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-cursor: hand; -fx-background-radius: 8px;");
                b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-text-fill: #FFFFFF; -fx-cursor: hand; -fx-background-radius: 8px;"));
                b.setOnMouseExited(e -> {
                    if (!entry.getKey().equalsIgnoreCase(activeKey)) {
                        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-cursor: hand; -fx-background-radius: 8px;");
                    }
                });
            }
        }
    }

    // ========================================================
    // TOPBAR
    // ========================================================
    private HBox createTopbar() {
        HBox topbar = new HBox(16);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPrefHeight(64);
        topbar.setPadding(new Insets(0, 24, 0, 24));
        topbar.setStyle(
            "-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 2);"
        );

        pageTitleLabel = new Label("Dashboard Overview");
        pageTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        pageTitleLabel.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Global Search
        topSearchField = new TextField();
        topSearchField.setPromptText("Global Student Search (ID, Roll, Name)...");
        topSearchField.setPrefWidth(320);
        topSearchField.setPrefHeight(36);
        topSearchField.setStyle(
            "-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6px 16px; -fx-font-size: 12px;"
        );
        topSearchField.setOnAction(e -> {
            String q = topSearchField.getText().trim();
            if (!q.isEmpty()) {
                showSection("Students");
                if (studentView != null) studentView.filterBySearchQuery(q);
            }
        });

        // Administrator Profile Badge
        HBox profileBadge = new HBox(10);
        profileBadge.setAlignment(Pos.CENTER_LEFT);
        profileBadge.setPadding(new Insets(4, 12, 4, 8));
        profileBadge.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 20px;");

        Circle avatar = new Circle(14, Color.web("#4054F4"));
        Label avText = new Label("A");
        avText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        avText.setTextFill(Color.WHITE);
        StackPane avStack = new StackPane(avatar, avText);

        VBox userMeta = new VBox(1);
        Label uName = new Label(currentUser.getUsername());
        uName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        uName.setTextFill(Color.web("#172554"));

        Label uRole = new Label("Administrator");
        uRole.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        uRole.setTextFill(Color.web("#64748B"));
        userMeta.getChildren().addAll(uName, uRole);

        profileBadge.getChildren().addAll(avStack, userMeta);

        topbar.getChildren().addAll(pageTitleLabel, sp, topSearchField, profileBadge);
        return topbar;
    }

    // ========================================================
    // SECTION SWITCHER
    // ========================================================
    public void showSection(String section) {
        updateActiveNav(section);
        pageTitleLabel.setText(section);
        contentStack.getChildren().clear();

        switch (section) {
            case "Dashboard" -> {
                overviewView = createOverviewView();
                contentStack.getChildren().add(overviewView);
            }
            case "Students" -> {
                if (studentView == null) studentView = new Student();
                else studentView.loadData();
                contentStack.getChildren().add(studentView);
            }
            case "Academic Management" -> {
                if (academicView == null) academicView = new Academic();
                else academicView.loadAllData();
                contentStack.getChildren().add(academicView);
            }
            case "Attendance" -> {
                if (attendanceView == null) attendanceView = new Attendance();
                contentStack.getChildren().add(attendanceView);
            }
            case "Examinations & Results" -> {
                if (examView == null) examView = new Exam();
                contentStack.getChildren().add(examView);
            }
            case "Reports" -> {
                if (reportsView == null) reportsView = new Reports();
                else reportsView.generateReport();
                contentStack.getChildren().add(reportsView);
            }
            case "Settings" -> {
                if (settingsView == null) settingsView = new Settings(currentUser, onLogout);
                contentStack.getChildren().add(settingsView);
            }
        }
    }

    // ========================================================
    // DASHBOARD OVERVIEW VIEW (6 REAL STATS + 4 REAL CHARTS)
    // ========================================================
    private ScrollPane createOverviewView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: #F8FAFC;");

        // Fetch Real Database Values
        int totalStudents = studentDAO.count();
        int totalCourses = academicDAO.countCourses();
        int totalSubjects = academicDAO.countSubjects();
        int totalClasses = academicDAO.countClasses();

        Map<String, Object> attStats = attendanceDAO.getOverallStats();
        double attPct = (double) attStats.getOrDefault("percentage", 0.0);

        Map<String, Object> exStats = examDAO.getExamStats();
        double avgScore = (double) exStats.getOrDefault("average_score", 0.0);

        // ROW 1: 6 Statistics Cards
        HBox statsGrid = new HBox(14);
        statsGrid.getChildren().addAll(
            statCard("TOTAL STUDENTS", String.valueOf(totalStudents), "👨‍🎓", "#4054F4", "#EEF3FF"),
            statCard("ACTIVE COURSES", String.valueOf(totalCourses), "📚", "#8B5CF6", "#F5F3FF"),
            statCard("SUBJECTS", String.valueOf(totalSubjects), "📝", "#06B6D4", "#ECFEFF"),
            statCard("CLASS SECTIONS", String.valueOf(totalClasses), "🏛", "#F59E0B", "#FFFBEB"),
            statCard("ATTENDANCE", String.format("%.1f%%", attPct), "📅", "#16A34A", "#F0FDF4"),
            statCard("AVERAGE SCORE", String.format("%.1f%%", avgScore), "⭐", "#3B82F6", "#EFF6FF")
        );

        // ROW 2: Charts Grid (2 columns x 2 rows)
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(16);
        chartsGrid.setVgap(16);

        // Chart 1: Course Enrollment Bar Chart
        chartsGrid.add(createCourseChartCard(), 0, 0);

        // Chart 2: Semester Distribution Bar Chart
        chartsGrid.add(createSemesterChartCard(), 1, 0);

        // Chart 3: Attendance Present vs Absent Pie Chart
        chartsGrid.add(createAttendancePieCard(attStats), 0, 1);

        // Chart 4: Grade Distribution Chart
        chartsGrid.add(createGradeChartCard(exStats), 1, 1);

        container.getChildren().addAll(statsGrid, chartsGrid);

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #F8FAFC; -fx-background-color: transparent;");
        return sp;
    }

    private VBox statCard(String title, String val, String icon, String colorHex, String bgHex) {
        VBox card = new VBox(6);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 6, 0, 0, 2);"
        );

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32);
        iconBox.setStyle("-fx-background-color: " + bgHex + "; -fx-background-radius: 8px;");
        Label ic = new Label(icon);
        ic.setFont(Font.font(14));
        iconBox.getChildren().add(ic);

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        t.setTextFill(Color.web("#64748B"));

        top.getChildren().addAll(iconBox, t);

        Label v = new Label(val);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        v.setTextFill(Color.web(colorHex));

        card.getChildren().addAll(top, v);
        return card;
    }

    // ========================================================
    // CHARTS (ALL FROM REAL MYSQL DATA)
    // ========================================================
    private VBox createCourseChartCard() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Course-wise Student Distribution");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Students");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<Course> courses = academicDAO.getAllCourses();
        for (Course c : courses) {
            int count = 0;
            String sql = "SELECT COUNT(*) FROM students WHERE course = ?";
            try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getCode());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) count = rs.getInt(1);
                }
            } catch (SQLException ignored) {}
            series.getData().add(new XYChart.Data<>(c.getCode(), count));
        }

        chart.getData().add(series);
        box.getChildren().addAll(title, chart);
        return box;
    }

    private VBox createSemesterChartCard() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Semester-wise Enrollment Distribution");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Students");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String sql = "SELECT semester, COUNT(*) as cnt FROM students GROUP BY semester ORDER BY semester ASC";
        try (Connection conn = DB.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>("Sem " + rs.getInt("semester"), rs.getInt("cnt")));
            }
        } catch (SQLException ignored) {}

        chart.getData().add(series);
        box.getChildren().addAll(title, chart);
        return box;
    }

    private VBox createAttendancePieCard(Map<String, Object> attStats) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Attendance Statistics (Present vs Absent)");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        int present = (int) attStats.getOrDefault("present", 0);
        int absent = (int) attStats.getOrDefault("absent", 0);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Present (" + present + ")", present),
            new PieChart.Data("Absent (" + absent + ")", absent)
        );

        PieChart chart = new PieChart(pieData);
        chart.setPrefHeight(250);

        box.getChildren().addAll(title, chart);
        return box;
    }

    @SuppressWarnings("unchecked")
    private VBox createGradeChartCard(Map<String, Object> exStats) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label title = new Label("Examination Performance (Grade Distribution)");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> grades = (Map<String, Integer>) exStats.getOrDefault("grade_distribution", Map.of());
        for (String g : List.of("A+", "A", "B", "C", "D", "F")) {
            series.getData().add(new XYChart.Data<>(g, grades.getOrDefault(g, 0)));
        }

        chart.getData().add(series);
        box.getChildren().addAll(title, chart);
        return box;
    }
}
