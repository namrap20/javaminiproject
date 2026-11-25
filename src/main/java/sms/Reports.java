package sms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import sms.model.AttendanceRecord;
import sms.model.Course;
import sms.model.ExamResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reports: Institutional Reporting and Data Export Module.
 * 
 * Generates:
 * 1. All Students Master Register
 * 2. Course-wise Enrollment
 * 3. Semester-wise Register
 * 4. Division-wise Class Lists
 * 5. Attendance Summary Logs
 * 6. Examination Performance Analysis
 * 
 * Supports: Search, Filtering, CSV Export, and Direct Printing.
 */
public class Reports extends VBox {

    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicDAO academicDAO = new AcademicDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();

    private ComboBox<String> reportTypeCombo;
    private ComboBox<String> courseCombo;
    private ComboBox<String> semCombo;
    private ComboBox<String> divCombo;
    private TableView<ObservableList<String>> reportTable;
    private Label reportStatsLabel;

    private final List<String> currentHeaders = new ArrayList<>();
    private final ObservableList<ObservableList<String>> currentData = FXCollections.observableArrayList();

    public Reports() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F8FAFC;");
        buildUI();
        generateReport();
    }

    private void buildUI() {
        // Heading
        Label heading = new Label("Institutional Reports");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#10265C"));

        Label desc = new Label("Generate official audit reports, analyze academic performance metrics, and export records to CSV.");
        desc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        desc.setTextFill(Color.web("#64748B"));

        // Control Filter Card
        HBox filterCard = new HBox(12);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(14, 16, 14, 16));
        filterCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        reportTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "1. All Students Register",
            "2. Course-wise Students",
            "3. Semester-wise Students",
            "4. Division-wise Students",
            "5. Attendance Summary Log",
            "6. Examination Results Performance"
        ));
        reportTypeCombo.setValue("1. All Students Register");
        reportTypeCombo.setPrefWidth(220);
        reportTypeCombo.setStyle(inputStyle());
        reportTypeCombo.setOnAction(e -> generateReport());

        courseCombo = new ComboBox<>();
        courseCombo.getItems().add("All Courses");
        for (Course c : academicDAO.getAllCourses()) courseCombo.getItems().add(c.getCode());
        courseCombo.setValue("All Courses");
        courseCombo.setStyle(inputStyle());
        courseCombo.setOnAction(e -> generateReport());

        semCombo = new ComboBox<>(FXCollections.observableArrayList(
            "All Semesters", "Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6", "Sem 7", "Sem 8"
        ));
        semCombo.setValue("All Semesters");
        semCombo.setStyle(inputStyle());
        semCombo.setOnAction(e -> generateReport());

        divCombo = new ComboBox<>(FXCollections.observableArrayList("All Divisions", "A", "B", "C", "D"));
        divCombo.setValue("All Divisions");
        divCombo.setStyle(inputStyle());
        divCombo.setOnAction(e -> generateReport());

        Button genBtn = new Button("↻ Run Report");
        genBtn.setStyle("-fx-background-color: #4054F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");
        genBtn.setOnAction(e -> generateReport());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📥 Export CSV");
        exportBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> exportToCsv());

        Button printBtn = new Button("🖨 Print");
        printBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px; -fx-font-weight: bold;");
        printBtn.setOnAction(e -> printTable());

        filterCard.getChildren().addAll(
            new Label("Report:"), reportTypeCombo,
            courseCombo, semCombo, divCombo,
            genBtn, spacer, exportBtn, printBtn
        );

        // Stats Banner
        HBox statsBanner = new HBox(12);
        statsBanner.setAlignment(Pos.CENTER_LEFT);
        statsBanner.setPadding(new Insets(8, 14, 8, 14));
        statsBanner.setStyle("-fx-background-color: #EEF3FF; -fx-background-radius: 8px;");

        reportStatsLabel = new Label("Generating report...");
        reportStatsLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        reportStatsLabel.setTextFill(Color.web("#10265C"));
        statsBanner.getChildren().add(reportStatsLabel);

        // Dynamic TableView
        reportTable = new TableView<>(currentData);
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        reportTable.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        getChildren().addAll(new VBox(4, heading, desc), filterCard, statsBanner, reportTable);
    }

    public void generateReport() {
        String type = reportTypeCombo.getValue();
        if (type == null) return;

        currentHeaders.clear();
        currentData.clear();
        reportTable.getColumns().clear();

        String selCourse = courseCombo.getValue();
        String selSem = semCombo.getValue();
        String selDiv = divCombo.getValue();

        if (type.startsWith("1.") || type.startsWith("2.") || type.startsWith("3.") || type.startsWith("4.")) {
            // Student-based reports
            currentHeaders.addAll(List.of("Student ID", "GR No", "Roll No", "Student Name", "Course", "Semester", "Division", "DOB", "Gender", "Adm Year"));
            setupColumns();

            List<sms.model.Student> all = studentDAO.getAllStudents();
            int count = 0;
            for (sms.model.Student s : all) {
                boolean matchCourse = (selCourse == null || "All Courses".equalsIgnoreCase(selCourse) || selCourse.equalsIgnoreCase(s.getCourse()));
                boolean matchSem = true;
                if (selSem != null && !"All Semesters".equalsIgnoreCase(selSem)) {
                    int targetSem = Integer.parseInt(selSem.replace("Sem", "").trim());
                    matchSem = (s.getSemester() == targetSem);
                }
                boolean matchDiv = (selDiv == null || "All Divisions".equalsIgnoreCase(selDiv) || selDiv.equalsIgnoreCase(s.getDivision()));

                if (matchCourse && matchSem && matchDiv) {
                    ObservableList<String> row = FXCollections.observableArrayList(
                        s.getId(), s.getGrNo(), s.getRollNo(), s.getName(),
                        s.getCourse(), "Sem " + s.getSemester(), s.getDivision(),
                        s.getDob(), s.getGender(), String.valueOf(s.getAdmissionYear())
                    );
                    currentData.add(row);
                    count++;
                }
            }
            reportStatsLabel.setText(String.format("Report Type: %s | Total Matched Records: %d", type, count));

        } else if (type.startsWith("5.")) {
            // Attendance Report
            currentHeaders.addAll(List.of("Log ID", "Student ID", "Student Name", "Date", "Status"));
            setupColumns();

            List<AttendanceRecord> list = attendanceDAO.getAllAttendance();
            int count = 0;
            long present = 0;
            for (AttendanceRecord a : list) {
                ObservableList<String> row = FXCollections.observableArrayList(
                    String.valueOf(a.getId()), a.getStudentId(), a.getStudentName(), a.getDate(), a.getStatus()
                );
                currentData.add(row);
                count++;
                if ("PRESENT".equalsIgnoreCase(a.getStatus())) present++;
            }
            double pct = count > 0 ? (present * 100.0) / count : 0.0;
            reportStatsLabel.setText(String.format("Attendance Logs: %d records | Present: %d | Absent: %d | Overall Attendance Rate: %.1f%%",
                    count, present, count - present, pct));

        } else if (type.startsWith("6.")) {
            // Exam Results Report
            currentHeaders.addAll(List.of("Result ID", "Student ID", "Student Name", "Subject", "Marks", "Max Marks", "Percentage", "Grade"));
            setupColumns();

            List<ExamResult> list = examDAO.getAllExamResults();
            int count = 0;
            double totalPct = 0;
            for (ExamResult e : list) {
                double pct = e.getTotal() > 0 ? (e.getMarks() / e.getTotal()) * 100.0 : 0.0;
                totalPct += pct;
                ObservableList<String> row = FXCollections.observableArrayList(
                    String.valueOf(e.getId()), e.getStudentId(), e.getStudentName(),
                    e.getSubjectCode(), String.valueOf(e.getMarks()), String.valueOf(e.getTotal()),
                    String.format("%.1f%%", pct), e.getGrade()
                );
                currentData.add(row);
                count++;
            }
            double avg = count > 0 ? totalPct / count : 0.0;
            reportStatsLabel.setText(String.format("Exam Records: %d evaluations | Overall Average Score: %.1f%%", count, avg));
        }
    }

    private void setupColumns() {
        for (int i = 0; i < currentHeaders.size(); i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(currentHeaders.get(i));
            col.setCellValueFactory(cellData -> {
                ObservableList<String> row = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(colIndex < row.size() ? row.get(colIndex) : "");
            });
            reportTable.getColumns().add(col);
        }
    }

    private void exportToCsv() {
        if (currentData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No report data available to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("SMS_Report_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write Header
                for (int i = 0; i < currentHeaders.size(); i++) {
                    writer.write("\"" + currentHeaders.get(i) + "\"");
                    if (i < currentHeaders.size() - 1) writer.write(",");
                }
                writer.write("\n");

                // Write Rows
                for (ObservableList<String> row : currentData) {
                    for (int i = 0; i < row.size(); i++) {
                        writer.write("\"" + row.get(i).replace("\"", "\"\"") + "\"");
                        if (i < row.size() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }

                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Report exported successfully to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to write CSV file: " + e.getMessage());
            }
        }
    }

    private void printTable() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(getScene().getWindow())) {
            boolean success = job.printPage(reportTable);
            if (success) {
                job.endJob();
            }
        }
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
