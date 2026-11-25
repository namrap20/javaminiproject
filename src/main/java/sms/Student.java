package sms;

import javafx.beans.property.SimpleIntegerProperty;
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
import sms.model.ExamResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Student: Complete Student Management Module View & Controller.
 * 
 * Features:
 * - TableView with academic fields (no email, phone, or address)
 * - Search, filter by Course, Semester, Division
 * - Add Student modal with validations (duplicate ID/GR, required fields)
 * - Edit Student modal
 * - Delete Student with confirmation
 * - View Student Profile with Attendance Summary & Exam Performance from MySQL
 */
public class Student extends VBox {

    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicDAO academicDAO = new AcademicDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();

    private TableView<sms.model.Student> table;
    private final ObservableList<sms.model.Student> studentList = FXCollections.observableArrayList();
    private TextField searchField;
    private ComboBox<String> courseFilter;
    private ComboBox<String> semFilter;
    private ComboBox<String> divFilter;
    private Label countLabel;

    public Student() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F8FAFC;");
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Top Action Bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label heading = new Label("Student Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#10265C"));

        countLabel = new Label("Total Registered Students: 0");
        countLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        countLabel.setTextFill(Color.web("#64748B"));
        titleBox.getChildren().addAll(heading, countLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add New Student");
        addBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        addBtn.setTextFill(Color.WHITE);
        addBtn.setPrefHeight(38);
        addBtn.setStyle(
            "-fx-background-color: #4054F4; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-cursor: hand;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: #3244D4; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: #4054F4; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-cursor: hand;"));
        addBtn.setOnAction(e -> showAddEditDialog(null));

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        refreshBtn.setStyle(
            "-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 7px 14px; -fx-cursor: hand; -fx-text-fill: #172554;"
        );
        refreshBtn.setOnAction(e -> loadData());

        topBar.getChildren().addAll(titleBox, spacer, refreshBtn, addBtn);

        // Filter / Search Bar Card
        HBox filterCard = new HBox(12);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(12, 16, 12, 16));
        filterCard.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;"
        );

        searchField = new TextField();
        searchField.setPromptText("🔍 Search student by ID, GR, Roll, Name...");
        searchField.setPrefWidth(280);
        searchField.setPrefHeight(36);
        searchField.setStyle(inputStyle());
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());

        courseFilter = new ComboBox<>();
        courseFilter.setPromptText("Course");
        courseFilter.setPrefHeight(36);
        courseFilter.setStyle(inputStyle());
        courseFilter.setOnAction(e -> applyFilters());

        semFilter = new ComboBox<>();
        semFilter.getItems().addAll("All Semesters", "Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6", "Sem 7", "Sem 8");
        semFilter.setValue("All Semesters");
        semFilter.setPrefHeight(36);
        semFilter.setStyle(inputStyle());
        semFilter.setOnAction(e -> applyFilters());

        divFilter = new ComboBox<>();
        divFilter.getItems().addAll("All Divisions", "A", "B", "C", "D");
        divFilter.setValue("All Divisions");
        divFilter.setPrefHeight(36);
        divFilter.setStyle(inputStyle());
        divFilter.setOnAction(e -> applyFilters());

        Button clearFilterBtn = new Button("Clear");
        clearFilterBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 12px;");
        clearFilterBtn.setOnAction(e -> {
            searchField.clear();
            courseFilter.setValue("All Courses");
            semFilter.setValue("All Semesters");
            divFilter.setValue("All Divisions");
            loadData();
        });

        filterCard.getChildren().addAll(
            new Label("Filters:"),
            searchField,
            courseFilter,
            semFilter,
            divFilter,
            clearFilterBtn
        );

        // Student TableView
        table = new TableView<>();
        table.setItems(studentList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        TableColumn<sms.model.Student, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(90);

        TableColumn<sms.model.Student, String> grCol = new TableColumn<>("GR No");
        grCol.setCellValueFactory(new PropertyValueFactory<>("grNo"));
        grCol.setPrefWidth(80);

        TableColumn<sms.model.Student, String> rollCol = new TableColumn<>("Roll No");
        rollCol.setCellValueFactory(new PropertyValueFactory<>("rollNo"));
        rollCol.setPrefWidth(100);

        TableColumn<sms.model.Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<sms.model.Student, String> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        dobCol.setPrefWidth(95);

        TableColumn<sms.model.Student, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(75);

        TableColumn<sms.model.Student, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        courseCol.setPrefWidth(100);

        TableColumn<sms.model.Student, Number> semCol = new TableColumn<>("Sem");
        semCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getSemester()));
        semCol.setPrefWidth(55);

        TableColumn<sms.model.Student, String> divCol = new TableColumn<>("Div");
        divCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        divCol.setPrefWidth(55);

        TableColumn<sms.model.Student, Number> yearCol = new TableColumn<>("Adm Year");
        yearCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getAdmissionYear()));
        yearCol.setPrefWidth(75);

        TableColumn<sms.model.Student, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(210);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button profileBtn = new Button("Profile");
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, profileBtn, editBtn, delBtn);

            {
                box.setAlignment(Pos.CENTER);
                profileBtn.setStyle("-fx-background-color: #EEF3FF; -fx-text-fill: #4054F4; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3px 8px;");
                editBtn.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3px 8px;");
                delBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3px 8px;");

                profileBtn.setOnAction(e -> {
                    sms.model.Student s = getTableView().getItems().get(getIndex());
                    showProfileDialog(s);
                });
                editBtn.setOnAction(e -> {
                    sms.model.Student s = getTableView().getItems().get(getIndex());
                    showAddEditDialog(s);
                });
                delBtn.setOnAction(e -> {
                    sms.model.Student s = getTableView().getItems().get(getIndex());
                    handleDeleteStudent(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, grCol, rollCol, nameCol, dobCol, genderCol, courseCol, semCol, divCol, yearCol, actionsCol);

        getChildren().addAll(topBar, filterCard, table);
    }

    public void loadData() {
        // Load courses for filter
        List<Course> courses = academicDAO.getAllCourses();
        courseFilter.getItems().clear();
        courseFilter.getItems().add("All Courses");
        for (Course c : courses) {
            courseFilter.getItems().add(c.getCode());
        }
        courseFilter.setValue("All Courses");

        List<sms.model.Student> list = studentDAO.getAllStudents();
        studentList.setAll(list);
        countLabel.setText("Total Registered Students: " + list.size());
    }

    public void filterBySearchQuery(String query) {
        if (searchField != null) {
            searchField.setText(query);
        }
    }

    private void applyFilters() {
        String q = searchField.getText() != null ? searchField.getText().trim() : "";
        String selCourse = courseFilter.getValue();
        String selSem = semFilter.getValue();
        String selDiv = divFilter.getValue();

        List<sms.model.Student> source = q.isEmpty() ? studentDAO.getAllStudents() : studentDAO.searchStudents(q);

        ObservableList<sms.model.Student> filtered = FXCollections.observableArrayList();
        for (sms.model.Student s : source) {
            boolean matchCourse = (selCourse == null || "All Courses".equalsIgnoreCase(selCourse) || selCourse.equalsIgnoreCase(s.getCourse()));
            boolean matchSem = true;
            if (selSem != null && !"All Semesters".equalsIgnoreCase(selSem)) {
                int targetSem = Integer.parseInt(selSem.replace("Sem", "").trim());
                matchSem = (s.getSemester() == targetSem);
            }
            boolean matchDiv = (selDiv == null || "All Divisions".equalsIgnoreCase(selDiv) || selDiv.equalsIgnoreCase(s.getDivision()));

            if (matchCourse && matchSem && matchDiv) {
                filtered.add(s);
            }
        }

        studentList.setAll(filtered);
        countLabel.setText(String.format("Showing %d of %d Students", filtered.size(), studentDAO.count()));
    }

    private void showAddEditDialog(sms.model.Student existing) {
        boolean isEdit = (existing != null);
        Dialog<sms.model.Student> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Student Details" : "Add New Student");
        dialog.setHeaderText(isEdit ? "Update Student Information in Database" : "Register New Student to MySQL Database");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #FFFFFF;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField idField = new TextField(isEdit ? existing.getId() : "");
        idField.setPromptText("e.g. STU121");
        idField.setDisable(isEdit);
        idField.setStyle(inputStyle());

        TextField grField = new TextField(isEdit ? existing.getGrNo() : "");
        grField.setPromptText("e.g. 136086");
        grField.setStyle(inputStyle());

        TextField rollField = new TextField(isEdit ? existing.getRollNo() : "");
        rollField.setPromptText("e.g. 92500527042");
        rollField.setStyle(inputStyle());

        TextField nameField = new TextField(isEdit ? existing.getName() : "");
        nameField.setPromptText("Full Student Name");
        nameField.setStyle(inputStyle());

        DatePicker dobPicker = new DatePicker();
        if (isEdit && existing.getDob() != null && !existing.getDob().isEmpty()) {
            try { dobPicker.setValue(LocalDate.parse(existing.getDob())); } catch (Exception ignored) {}
        }
        dobPicker.setStyle(inputStyle());

        ComboBox<String> genderCombo = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        genderCombo.setValue(isEdit ? existing.getGender() : "Male");
        genderCombo.setStyle(inputStyle());

        ComboBox<String> courseCombo = new ComboBox<>();
        for (Course c : academicDAO.getAllCourses()) {
            courseCombo.getItems().add(c.getCode());
        }
        if (isEdit) courseCombo.setValue(existing.getCourse());
        else if (!courseCombo.getItems().isEmpty()) courseCombo.getSelectionModel().selectFirst();
        courseCombo.setStyle(inputStyle());

        ComboBox<Integer> semCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8));
        semCombo.setValue(isEdit ? existing.getSemester() : 1);
        semCombo.setStyle(inputStyle());

        ComboBox<String> divCombo = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        divCombo.setValue(isEdit ? existing.getDivision() : "A");
        divCombo.setStyle(inputStyle());

        TextField yearField = new TextField(isEdit ? String.valueOf(existing.getAdmissionYear()) : String.valueOf(LocalDate.now().getYear()));
        yearField.setStyle(inputStyle());

        // Layout rows
        int r = 0;
        grid.add(new Label("Student ID *"), 0, r); grid.add(idField, 1, r++);
        grid.add(new Label("GR Number *"), 0, r); grid.add(grField, 1, r++);
        grid.add(new Label("Roll Number *"), 0, r); grid.add(rollField, 1, r++);
        grid.add(new Label("Student Name *"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("Date of Birth *"), 0, r); grid.add(dobPicker, 1, r++);
        grid.add(new Label("Gender *"), 0, r); grid.add(genderCombo, 1, r++);
        grid.add(new Label("Course *"), 0, r); grid.add(courseCombo, 1, r++);
        grid.add(new Label("Semester *"), 0, r); grid.add(semCombo, 1, r++);
        grid.add(new Label("Division *"), 0, r); grid.add(divCombo, 1, r++);
        grid.add(new Label("Admission Year *"), 0, r); grid.add(yearField, 1, r++);

        pane.setContent(grid);

        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String sid = idField.getText().trim();
            String gr = grField.getText().trim();
            String roll = rollField.getText().trim();
            String name = nameField.getText().trim();
            LocalDate dob = dobPicker.getValue();
            String gender = genderCombo.getValue();
            String course = courseCombo.getValue();
            Integer sem = semCombo.getValue();
            String div = divCombo.getValue();
            String yearStr = yearField.getText().trim();

            if (sid.isEmpty() || gr.isEmpty() || roll.isEmpty() || name.isEmpty() || dob == null || course == null || div == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required. Please fill in all fields.");
                event.consume();
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearStr);
                if (year < 1990 || year > 2040) throw new NumberFormatException();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Admission year must be a valid 4-digit year (e.g. 2024).");
                event.consume();
                return;
            }

            // Check Duplicate ID
            if (!isEdit && studentDAO.isIdExists(sid)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate ID", "Student ID '" + sid + "' is already registered in the system.");
                event.consume();
                return;
            }

            // Check Duplicate GR
            if (studentDAO.isGrExists(gr, isEdit ? existing.getId() : null)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate GR Number", "GR Number '" + gr + "' already exists for another student.");
                event.consume();
                return;
            }

            sms.model.Student s = isEdit ? existing : new sms.model.Student();
            s.setId(sid);
            s.setGrNo(gr);
            s.setRollNo(roll);
            s.setName(name);
            s.setDob(dob.format(DateTimeFormatter.ISO_LOCAL_DATE));
            s.setGender(gender);
            s.setCourse(course);
            s.setSemester(sem != null ? sem : 1);
            s.setDivision(div);
            s.setAdmissionYear(year);

            boolean success = isEdit ? studentDAO.updateStudent(s) : studentDAO.addStudent(s);
            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save student record to MySQL database.");
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                loadData();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleDeleteStudent(sms.model.Student s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Student: " + s.getName());
        confirm.setContentText("Are you sure you want to permanently delete student ID: " + s.getId() + "?\nAll associated attendance and exam records will be deleted.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = studentDAO.deleteStudent(s.getId());
                if (ok) {
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Delete Failed", "Failed to delete student from MySQL.");
                }
            }
        });
    }

    private void showProfileDialog(sms.model.Student s) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student Academic Profile - " + s.getName());
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setStyle("-fx-background-color: #FFFFFF;");

        VBox content = new VBox(18);
        content.setPrefWidth(540);
        content.setPadding(new Insets(10));

        // Header with Avatar Badge
        HBox profileHeader = new HBox(16);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        profileHeader.setPadding(new Insets(16));
        profileHeader.setStyle("-fx-background-color: #EEF3FF; -fx-background-radius: 12px;");

        StackPane avatar = new StackPane();
        avatar.setPrefSize(56, 56);
        avatar.setStyle("-fx-background-color: #4054F4; -fx-background-radius: 28px;");
        String initials = getInitials(s.getName());
        Label initLabel = new Label(initials);
        initLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        initLabel.setTextFill(Color.WHITE);
        avatar.getChildren().add(initLabel);

        VBox metaBox = new VBox(4);
        Label nameLbl = new Label(s.getName());
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        nameLbl.setTextFill(Color.web("#10265C"));

        Label subLbl = new Label("ID: " + s.getId() + "  |  GR: " + s.getGrNo() + "  |  Roll: " + s.getRollNo());
        subLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subLbl.setTextFill(Color.web("#64748B"));

        metaBox.getChildren().addAll(nameLbl, subLbl);
        profileHeader.getChildren().addAll(avatar, metaBox);

        // Section 1: Academic Enrollment
        VBox academicBox = new VBox(8);
        Label sec1 = new Label("Academic Enrollment Details");
        sec1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        sec1.setTextFill(Color.web("#10265C"));

        GridPane acadGrid = new GridPane();
        acadGrid.setHgap(16);
        acadGrid.setVgap(8);
        acadGrid.setPadding(new Insets(10));
        acadGrid.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px;");

        acadGrid.add(metaLabel("Course:"), 0, 0); acadGrid.add(valueLabel(s.getCourse()), 1, 0);
        acadGrid.add(metaLabel("Semester:"), 2, 0); acadGrid.add(valueLabel("Sem " + s.getSemester()), 3, 0);
        acadGrid.add(metaLabel("Division:"), 0, 1); acadGrid.add(valueLabel(s.getDivision()), 1, 1);
        acadGrid.add(metaLabel("Admission Year:"), 2, 1); acadGrid.add(valueLabel(String.valueOf(s.getAdmissionYear())), 3, 1);
        acadGrid.add(metaLabel("Date of Birth:"), 0, 2); acadGrid.add(valueLabel(s.getDob()), 1, 2);
        acadGrid.add(metaLabel("Gender:"), 2, 2); acadGrid.add(valueLabel(s.getGender()), 3, 2);

        academicBox.getChildren().addAll(sec1, acadGrid);

        // Section 2: Attendance Summary from MySQL
        VBox attBox = new VBox(8);
        Label sec2 = new Label("Attendance Record Summary");
        sec2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        sec2.setTextFill(Color.web("#10265C"));

        List<AttendanceRecord> attList = attendanceDAO.getAttendanceByStudent(s.getId());
        int totalDays = attList.size();
        long presentDays = attList.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        long absentDays = totalDays - presentDays;
        double attPct = totalDays > 0 ? (presentDays * 100.0) / totalDays : 0.0;

        HBox attStats = new HBox(12);
        attStats.getChildren().addAll(
            miniStat("Total Classes", String.valueOf(totalDays), "#64748B"),
            miniStat("Present", String.valueOf(presentDays), "#16A34A"),
            miniStat("Absent", String.valueOf(absentDays), "#EF4444"),
            miniStat("Attendance %", String.format("%.1f%%", attPct), attPct >= 75 ? "#16A34A" : "#F59E0B")
        );
        attBox.getChildren().addAll(sec2, attStats);

        // Section 3: Examination Performance from MySQL
        VBox examBox = new VBox(8);
        Label sec3 = new Label("Examination Performance");
        sec3.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        sec3.setTextFill(Color.web("#10265C"));

        List<ExamResult> examList = examDAO.getResultsByStudent(s.getId());
        VBox examRows = new VBox(4);
        if (examList.isEmpty()) {
            Label noExam = new Label("No examination records logged for this student.");
            noExam.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            noExam.setTextFill(Color.web("#94A3B8"));
            examRows.getChildren().add(noExam);
        } else {
            double totalMarks = 0;
            double maxMarks = 0;
            for (ExamResult er : examList) {
                totalMarks += er.getMarks();
                maxMarks += er.getTotal();
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 10, 6, 10));
                row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6px;");

                Label subCode = new Label(er.getSubjectCode());
                subCode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                subCode.setPrefWidth(90);

                Label marksLbl = new Label(String.format("%.1f / %.1f", er.getMarks(), er.getTotal()));
                marksLbl.setPrefWidth(120);

                Label pctLbl = new Label(String.format("%.1f%%", (er.getMarks() / er.getTotal()) * 100.0));
                pctLbl.setPrefWidth(80);

                Label gradeLbl = new Label(er.getGrade());
                gradeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                gradeLbl.setTextFill("F".equals(er.getGrade()) ? Color.web("#EF4444") : Color.web("#16A34A"));

                row.getChildren().addAll(subCode, marksLbl, pctLbl, gradeLbl);
                examRows.getChildren().add(row);
            }
            double overallPct = maxMarks > 0 ? (totalMarks * 100.0) / maxMarks : 0.0;
            Label overall = new Label(String.format("Overall Score: %.1f / %.1f (%.1f%%) - Grade: %s",
                    totalMarks, maxMarks, overallPct, ExamDAO.calculateGrade(totalMarks, maxMarks)));
            overall.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            overall.setTextFill(Color.web("#4054F4"));
            examRows.getChildren().add(overall);
        }
        examBox.getChildren().addAll(sec3, examRows);

        content.getChildren().addAll(profileHeader, academicBox, attBox, examBox);
        pane.setContent(content);

        dialog.showAndWait();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "ST";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private Label metaLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#64748B"));
        return l;
    }

    private Label valueLabel(String text) {
        Label l = new Label(text != null ? text : "-");
        l.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        l.setTextFill(Color.web("#172554"));
        return l;
    }

    private VBox miniStat(String label, String value, String colorHex) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(8, 12, 8, 12));
        box.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setAlignment(Pos.CENTER);

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        v.setTextFill(Color.web(colorHex));

        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        l.setTextFill(Color.web("#64748B"));

        box.getChildren().addAll(v, l);
        return box;
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
