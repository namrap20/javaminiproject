package sms;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sms.model.AttendanceRecord;
import sms.model.Course;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Attendance: Complete Student Attendance Management Module.
 * 
 * Features:
 * - Filter by Course, Semester, Division, and Date
 * - Load Students into interactive TableView
 * - Toggle Present / Absent with instant color feedback
 * - Mark All Present / Mark All Absent one-click shortcuts
 * - Batch saves to MySQL with Transactions (no duplicate records)
 * - Real-time statistics: Total, Present, Absent, Attendance Percentage %
 * - Attendance History viewer with date filtering
 */
public class Attendance extends VBox {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicDAO academicDAO = new AcademicDAO();

    // Attendance row item wrapper
    public static class AttendanceItem {
        private final sms.model.Student student;
        private final SimpleStringProperty status = new SimpleStringProperty("PRESENT");

        public AttendanceItem(sms.model.Student s, String st) {
            this.student = s;
            this.status.set(st != null ? st : "PRESENT");
        }

        public sms.model.Student getStudent() { return student; }
        public String getId() { return student.getId(); }
        public String getRollNo() { return student.getRollNo(); }
        public String getName() { return student.getName(); }
        public String getStatus() { return status.get(); }
        public void setStatus(String s) { this.status.set(s); }
        public SimpleStringProperty statusProperty() { return status; }
    }

    private final ObservableList<AttendanceItem> currentList = FXCollections.observableArrayList();
    private final ObservableList<AttendanceRecord> historyList = FXCollections.observableArrayList();

    private ComboBox<String> courseCombo;
    private ComboBox<Integer> semCombo;
    private ComboBox<String> divCombo;
    private DatePicker datePicker;

    private Label totalLabel;
    private Label presentLabel;
    private Label absentLabel;
    private Label pctLabel;

    public Attendance() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F8FAFC;");
        buildUI();
    }

    private void buildUI() {
        // Heading
        Label heading = new Label("Attendance Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#10265C"));

        Label desc = new Label("Record daily class attendance, batch-update logs, and view historical participation records.");
        desc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        desc.setTextFill(Color.web("#64748B"));

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        tabs.getTabs().addAll(
            createMarkAttendanceTab(),
            createHistoryTab()
        );

        getChildren().addAll(new VBox(4, heading, desc), tabs);
    }

    // ========================================================
    // TAB 1: MARK ATTENDANCE
    // ========================================================
    private Tab createMarkAttendanceTab() {
        Tab tab = new Tab("Mark Attendance");
        tab.setClosable(false);

        VBox content = new VBox(14);
        content.setPadding(new Insets(16, 0, 0, 0));

        // Filter / Controls Card
        HBox filterCard = new HBox(12);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(14, 16, 14, 16));
        filterCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Course");
        courseCombo.setStyle(inputStyle());
        for (Course c : academicDAO.getAllCourses()) {
            courseCombo.getItems().add(c.getCode());
        }
        if (!courseCombo.getItems().isEmpty()) courseCombo.getSelectionModel().selectFirst();

        semCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8));
        semCombo.setValue(3);
        semCombo.setStyle(inputStyle());

        divCombo = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        divCombo.setValue("A");
        divCombo.setStyle(inputStyle());

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle(inputStyle());

        Button loadBtn = new Button("Load Students");
        loadBtn.setStyle("-fx-background-color: #4054F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        loadBtn.setOnAction(e -> loadClassStudents());

        filterCard.getChildren().addAll(
            new Label("Class:"), courseCombo,
            new Label("Sem:"), semCombo,
            new Label("Div:"), divCombo,
            new Label("Date:"), datePicker,
            loadBtn
        );

        // Stats Summary Cards
        HBox statsRow = new HBox(12);
        totalLabel = createStatNum("0", "#10265C");
        presentLabel = createStatNum("0", "#16A34A");
        absentLabel = createStatNum("0", "#EF4444");
        pctLabel = createStatNum("0.0%", "#4054F4");

        statsRow.getChildren().addAll(
            statCard("Total Students", totalLabel),
            statCard("Marked Present", presentLabel),
            statCard("Marked Absent", absentLabel),
            statCard("Attendance Rate", pctLabel)
        );

        // Action Buttons Row
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button allPresentBtn = new Button("✔ Mark All Present");
        allPresentBtn.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        allPresentBtn.setOnAction(e -> {
            for (AttendanceItem item : currentList) item.setStatus("PRESENT");
            updateStats();
        });

        Button allAbsentBtn = new Button("✖ Mark All Absent");
        allAbsentBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        allAbsentBtn.setOnAction(e -> {
            for (AttendanceItem item : currentList) item.setStatus("ABSENT");
            updateStats();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = new Button("💾 Save Attendance to MySQL");
        saveBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        saveBtn.setStyle("-fx-background-color: #4054F4; -fx-text-fill: white; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 8px 18px;");
        saveBtn.setOnAction(e -> saveAttendance());

        actionRow.getChildren().addAll(allPresentBtn, allAbsentBtn, spacer, saveBtn);

        // TableView
        TableView<AttendanceItem> table = new TableView<>(currentList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        TableColumn<AttendanceItem, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(100);

        TableColumn<AttendanceItem, String> rollCol = new TableColumn<>("Roll No");
        rollCol.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        rollCol.setPrefWidth(120);

        TableColumn<AttendanceItem, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(220);

        TableColumn<AttendanceItem, String> statusCol = new TableColumn<>("Attendance Status");
        statusCol.setPrefWidth(200);
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final ToggleButton presBtn = new ToggleButton("PRESENT");
            private final ToggleButton absBtn = new ToggleButton("ABSENT");
            private final ToggleGroup grp = new ToggleGroup();
            private final HBox box = new HBox(6, presBtn, absBtn);
            {
                box.setAlignment(Pos.CENTER);
                presBtn.setToggleGroup(grp);
                absBtn.setToggleGroup(grp);

                presBtn.setOnAction(e -> {
                    AttendanceItem it = getTableView().getItems().get(getIndex());
                    it.setStatus("PRESENT");
                    updateToggleStyles();
                    updateStats();
                });

                absBtn.setOnAction(e -> {
                    AttendanceItem it = getTableView().getItems().get(getIndex());
                    it.setStatus("ABSENT");
                    updateToggleStyles();
                    updateStats();
                });
            }

            private void updateToggleStyles() {
                if (presBtn.isSelected()) {
                    presBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand;");
                    absBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand;");
                } else {
                    presBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand;");
                    absBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand;");
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    AttendanceItem it = getTableView().getItems().get(getIndex());
                    if ("PRESENT".equalsIgnoreCase(it.getStatus())) {
                        presBtn.setSelected(true);
                    } else {
                        absBtn.setSelected(true);
                    }
                    updateToggleStyles();
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, rollCol, nameCol, statusCol);

        content.getChildren().addAll(filterCard, statsRow, actionRow, table);
        tab.setContent(content);

        // Preload first class
        loadClassStudents();

        return tab;
    }

    private void loadClassStudents() {
        String course = courseCombo.getValue();
        Integer sem = semCombo.getValue();
        String div = divCombo.getValue();
        LocalDate date = datePicker.getValue();

        if (course == null || sem == null || div == null || date == null) return;

        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, String> existingMap = attendanceDAO.getAttendanceMapForDate(dateStr);

        List<sms.model.Student> students = studentDAO.getStudentsByClass(course, sem, div);
        currentList.clear();

        for (sms.model.Student s : students) {
            String status = existingMap.getOrDefault(s.getId(), "PRESENT");
            currentList.add(new AttendanceItem(s, status));
        }

        updateStats();
    }

    private void updateStats() {
        int total = currentList.size();
        long present = currentList.stream().filter(i -> "PRESENT".equalsIgnoreCase(i.getStatus())).count();
        long absent = total - present;
        double pct = total > 0 ? (present * 100.0) / total : 0.0;

        totalLabel.setText(String.valueOf(total));
        presentLabel.setText(String.valueOf(present));
        absentLabel.setText(String.valueOf(absent));
        pctLabel.setText(String.format("%.1f%%", pct));
    }

    private void saveAttendance() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showAlert(Alert.AlertType.ERROR, "Date Required", "Please select an attendance date.");
            return;
        }
        if (currentList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Students", "No students loaded to mark attendance.");
            return;
        }

        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<AttendanceRecord> records = new ArrayList<>();
        for (AttendanceItem item : currentList) {
            records.add(new AttendanceRecord(0, item.getId(), item.getName(), dateStr, item.getStatus()));
        }

        boolean ok = attendanceDAO.saveBatchAttendance(records);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Attendance Saved",
                    "Attendance successfully persisted to MySQL for " + records.size() + " students on " + dateStr + ".");
            loadHistory();
        } else {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Failed to persist attendance records to MySQL.");
        }
    }

    // ========================================================
    // TAB 2: ATTENDANCE HISTORY
    // ========================================================
    private Tab createHistoryTab() {
        Tab tab = new Tab("Attendance History");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 0, 0, 0));

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Logged Attendance History");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button refreshBtn = new Button("↻ Refresh History");
        refreshBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadHistory());

        topBar.getChildren().addAll(title, sp, refreshBtn);

        TableView<AttendanceRecord> historyTable = new TableView<>(historyList);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        TableColumn<AttendanceRecord, Integer> idCol = new TableColumn<>("Log ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);

        TableColumn<AttendanceRecord, String> stuIdCol = new TableColumn<>("Student ID");
        stuIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        stuIdCol.setPrefWidth(100);

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        nameCol.setPrefWidth(220);

        TableColumn<AttendanceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);

        TableColumn<AttendanceRecord, String> statCol = new TableColumn<>("Status");
        statCol.setPrefWidth(110);
        statCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label l = new Label(item);
                    l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    if ("PRESENT".equalsIgnoreCase(item)) {
                        l.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-padding: 3px 8px; -fx-background-radius: 4px;");
                    } else {
                        l.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-padding: 3px 8px; -fx-background-radius: 4px;");
                    }
                    setGraphic(l);
                }
            }
        });
        statCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<AttendanceRecord, Void> actCol = new TableColumn<>("Action");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("Delete");
            {
                delBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4px; -fx-padding: 3px 8px;");
                delBtn.setOnAction(e -> {
                    AttendanceRecord ar = getTableView().getItems().get(getIndex());
                    if (attendanceDAO.deleteAttendance(ar.getId())) {
                        loadHistory();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : delBtn);
            }
        });

        historyTable.getColumns().addAll(idCol, stuIdCol, nameCol, dateCol, statCol, actCol);

        content.getChildren().addAll(topBar, historyTable);
        tab.setContent(content);

        loadHistory();
        return tab;
    }

    private void loadHistory() {
        historyList.setAll(attendanceDAO.getAllAttendance());
    }

    private Label createStatNum(String init, String colorHex) {
        Label l = new Label(init);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        l.setTextFill(Color.web(colorHex));
        return l;
    }

    private VBox statCard(String title, Label valLbl) {
        VBox b = new VBox(4);
        b.setPadding(new Insets(10, 16, 10, 16));
        b.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");
        HBox.setHgrow(b, Priority.ALWAYS);
        b.setAlignment(Pos.CENTER);

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        t.setTextFill(Color.web("#64748B"));

        b.getChildren().addAll(valLbl, t);
        return b;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String inputStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 10px; -fx-font-size: 12px; -fx-text-fill: #172554;";
    }
}
