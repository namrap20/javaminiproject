package sms;

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
import sms.model.ClassModel;
import sms.model.Course;
import sms.model.Subject;
import sms.model.Timetable;

import java.util.List;

/**
 * Academic: Academic Management Module View & Controller.
 * 
 * Manages:
 * 1. Courses (Add, View, Edit, Delete)
 * 2. Subjects (Add, View, Edit, Delete)
 * 3. Classes (Add, View, Edit, Delete)
 * 4. Timetable (Add, View, Edit, Delete)
 */
public class Academic extends VBox {

    private final AcademicDAO dao = new AcademicDAO();

    // Observable Lists
    private final ObservableList<Course> courseList = FXCollections.observableArrayList();
    private final ObservableList<Subject> subjectList = FXCollections.observableArrayList();
    private final ObservableList<ClassModel> classList = FXCollections.observableArrayList();
    private final ObservableList<Timetable> timetableList = FXCollections.observableArrayList();

    public Academic() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F8FAFC;");
        buildUI();
        loadAllData();
    }

    private void buildUI() {
        // Heading
        Label heading = new Label("Academic Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#10265C"));

        Label desc = new Label("Configure and maintain institution courses, curriculum subjects, class sections, and timetable schedules.");
        desc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        desc.setTextFill(Color.web("#64748B"));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabPane.getTabs().addAll(
            createCoursesTab(),
            createSubjectsTab(),
            createClassesTab(),
            createTimetableTab()
        );

        getChildren().addAll(new VBox(4, heading, desc), tabPane);
    }

    // ========================================================
    // TAB 1: COURSES
    // ========================================================
    private Tab createCoursesTab() {
        Tab tab = new Tab("Courses");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 0, 0, 0));

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Available Academic Courses");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Course");
        addBtn.setStyle(primaryBtnStyle());
        addBtn.setOnAction(e -> showCourseDialog(null));

        topBar.getChildren().addAll(title, sp, addBtn);

        TableView<Course> table = new TableView<>(courseList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Course, Integer> durCol = new TableColumn<>("Duration (Years)");
        durCol.setCellValueFactory(new PropertyValueFactory<>("durationYears"));

        TableColumn<Course, Integer> semCol = new TableColumn<>("Total Semesters");
        semCol.setCellValueFactory(new PropertyValueFactory<>("totalSemesters"));

        TableColumn<Course, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            private final HBox box = new HBox(6, edit, del);
            {
                box.setAlignment(Pos.CENTER);
                edit.setStyle(editBtnStyle());
                del.setStyle(delBtnStyle());
                edit.setOnAction(e -> showCourseDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    Course c = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Course", c.getName())) {
                        dao.deleteCourse(c.getCode());
                        loadAllData();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(codeCol, nameCol, durCol, semCol, actCol);
        content.getChildren().addAll(topBar, table);
        tab.setContent(content);
        return tab;
    }

    private void showCourseDialog(Course existing) {
        boolean isEdit = (existing != null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Course" : "Add Course");
        dialog.setHeaderText(isEdit ? "Update course details" : "Register a new academic course");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(16));

        TextField codeF = new TextField(isEdit ? existing.getCode() : "");
        codeF.setDisable(isEdit);
        codeF.setPromptText("e.g. 0obc6678");
        codeF.setStyle(inputStyle());

        TextField nameF = new TextField(isEdit ? existing.getName() : "");
        nameF.setPromptText("e.g. Computer Applications (BCA)");
        nameF.setStyle(inputStyle());

        Spinner<Integer> durSp = new Spinner<>(1, 6, isEdit ? existing.getDurationYears() : 3);
        Spinner<Integer> semSp = new Spinner<>(1, 12, isEdit ? existing.getTotalSemesters() : 6);

        g.add(new Label("Course Code *"), 0, 0); g.add(codeF, 1, 0);
        g.add(new Label("Course Name *"), 0, 1); g.add(nameF, 1, 1);
        g.add(new Label("Duration (Years)"), 0, 2); g.add(durSp, 1, 2);
        g.add(new Label("Total Semesters"), 0, 3); g.add(semSp, 1, 3);

        pane.setContent(g);

        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String c = codeF.getText().trim();
            String n = nameF.getText().trim();
            if (c.isEmpty() || n.isEmpty()) {
                showAlert("Course Code and Course Name are required.");
                ev.consume();
                return;
            }
            Course course = new Course(c, n, durSp.getValue(), semSp.getValue());
            boolean success = isEdit ? dao.updateCourse(course) : dao.addCourse(course);
            if (!success) {
                showAlert("Failed to save course. Course code may already exist.");
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) loadAllData();
        });
    }

    // ========================================================
    // TAB 2: SUBJECTS
    // ========================================================
    private Tab createSubjectsTab() {
        Tab tab = new Tab("Subjects");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 0, 0, 0));

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Curriculum Subjects");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Subject");
        addBtn.setStyle(primaryBtnStyle());
        addBtn.setOnAction(e -> showSubjectDialog(null));

        topBar.getChildren().addAll(title, sp, addBtn);

        TableView<Subject> table = new TableView<>(subjectList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Subject, String> codeCol = new TableColumn<>("Subject Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Subject, String> nameCol = new TableColumn<>("Subject Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Subject, String> courseCol = new TableColumn<>("Course Code");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));

        TableColumn<Subject, Integer> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(new PropertyValueFactory<>("semester"));

        TableColumn<Subject, Integer> credCol = new TableColumn<>("Credits");
        credCol.setCellValueFactory(new PropertyValueFactory<>("credits"));

        TableColumn<Subject, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            private final HBox box = new HBox(6, edit, del);
            {
                box.setAlignment(Pos.CENTER);
                edit.setStyle(editBtnStyle());
                del.setStyle(delBtnStyle());
                edit.setOnAction(e -> showSubjectDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    Subject s = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Subject", s.getName())) {
                        dao.deleteSubject(s.getCode());
                        loadAllData();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(codeCol, nameCol, courseCol, semCol, credCol, actCol);
        content.getChildren().addAll(topBar, table);
        tab.setContent(content);
        return tab;
    }

    private void showSubjectDialog(Subject existing) {
        boolean isEdit = (existing != null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Subject" : "Add Subject");
        dialog.setHeaderText(isEdit ? "Update subject details" : "Register a new subject");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(16));

        TextField codeF = new TextField(isEdit ? existing.getCode() : "");
        codeF.setDisable(isEdit);
        codeF.setPromptText("e.g. CS305");
        codeF.setStyle(inputStyle());

        TextField nameF = new TextField(isEdit ? existing.getName() : "");
        nameF.setPromptText("e.g. Operating Systems");
        nameF.setStyle(inputStyle());

        ComboBox<String> courseCombo = new ComboBox<>();
        for (Course c : courseList) courseCombo.getItems().add(c.getCode());
        if (isEdit) courseCombo.setValue(existing.getCourseCode());
        else if (!courseCombo.getItems().isEmpty()) courseCombo.getSelectionModel().selectFirst();
        courseCombo.setStyle(inputStyle());

        Spinner<Integer> semSp = new Spinner<>(1, 10, isEdit ? existing.getSemester() : 3);
        Spinner<Integer> credSp = new Spinner<>(1, 8, isEdit ? existing.getCredits() : 4);

        g.add(new Label("Subject Code *"), 0, 0); g.add(codeF, 1, 0);
        g.add(new Label("Subject Name *"), 0, 1); g.add(nameF, 1, 1);
        g.add(new Label("Course"), 0, 2); g.add(courseCombo, 1, 2);
        g.add(new Label("Semester"), 0, 3); g.add(semSp, 1, 3);
        g.add(new Label("Credits"), 0, 4); g.add(credSp, 1, 4);

        pane.setContent(g);

        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String c = codeF.getText().trim();
            String n = nameF.getText().trim();
            if (c.isEmpty() || n.isEmpty()) {
                showAlert("Subject Code and Subject Name are required.");
                ev.consume();
                return;
            }
            Subject sub = new Subject(c, n, courseCombo.getValue(), semSp.getValue(), credSp.getValue());
            boolean success = isEdit ? dao.updateSubject(sub) : dao.addSubject(sub);
            if (!success) {
                showAlert("Failed to save subject. Subject code may already exist.");
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) loadAllData();
        });
    }

    // ========================================================
    // TAB 3: CLASSES
    // ========================================================
    private Tab createClassesTab() {
        Tab tab = new Tab("Classes");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 0, 0, 0));

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Class Sections & Rooms");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Class");
        addBtn.setStyle(primaryBtnStyle());
        addBtn.setOnAction(e -> showClassDialog(null));

        topBar.getChildren().addAll(title, sp, addBtn);

        TableView<ClassModel> table = new TableView<>(classList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ClassModel, String> idCol = new TableColumn<>("Class ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("classId"));

        TableColumn<ClassModel, String> courseCol = new TableColumn<>("Course Code");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));

        TableColumn<ClassModel, Integer> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(new PropertyValueFactory<>("semester"));

        TableColumn<ClassModel, String> divCol = new TableColumn<>("Division");
        divCol.setCellValueFactory(new PropertyValueFactory<>("division"));

        TableColumn<ClassModel, String> roomCol = new TableColumn<>("Room No");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNo"));

        TableColumn<ClassModel, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            private final HBox box = new HBox(6, edit, del);
            {
                box.setAlignment(Pos.CENTER);
                edit.setStyle(editBtnStyle());
                del.setStyle(delBtnStyle());
                edit.setOnAction(e -> showClassDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    ClassModel cm = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Class", cm.getClassId())) {
                        dao.deleteClass(cm.getClassId());
                        loadAllData();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, courseCol, semCol, divCol, roomCol, actCol);
        content.getChildren().addAll(topBar, table);
        tab.setContent(content);
        return tab;
    }

    private void showClassDialog(ClassModel existing) {
        boolean isEdit = (existing != null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Class" : "Add Class");
        dialog.setHeaderText(isEdit ? "Update class details" : "Register a new class section");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(16));

        TextField idF = new TextField(isEdit ? existing.getClassId() : "");
        idF.setDisable(isEdit);
        idF.setPromptText("e.g. BCA-3A");
        idF.setStyle(inputStyle());

        ComboBox<String> courseCombo = new ComboBox<>();
        for (Course c : courseList) courseCombo.getItems().add(c.getCode());
        if (isEdit) courseCombo.setValue(existing.getCourseCode());
        else if (!courseCombo.getItems().isEmpty()) courseCombo.getSelectionModel().selectFirst();
        courseCombo.setStyle(inputStyle());

        Spinner<Integer> semSp = new Spinner<>(1, 10, isEdit ? existing.getSemester() : 3);
        ComboBox<String> divCombo = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        divCombo.setValue(isEdit ? existing.getDivision() : "A");
        divCombo.setStyle(inputStyle());

        TextField roomF = new TextField(isEdit ? existing.getRoomNo() : "Room 101");
        roomF.setStyle(inputStyle());

        g.add(new Label("Class ID *"), 0, 0); g.add(idF, 1, 0);
        g.add(new Label("Course"), 0, 1); g.add(courseCombo, 1, 1);
        g.add(new Label("Semester"), 0, 2); g.add(semSp, 1, 2);
        g.add(new Label("Division"), 0, 3); g.add(divCombo, 1, 3);
        g.add(new Label("Room No"), 0, 4); g.add(roomF, 1, 4);

        pane.setContent(g);

        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String cid = idF.getText().trim();
            if (cid.isEmpty()) {
                showAlert("Class ID is required (e.g. BCA-3A).");
                ev.consume();
                return;
            }
            ClassModel cm = new ClassModel(cid, courseCombo.getValue(), semSp.getValue(), divCombo.getValue(), roomF.getText().trim());
            boolean success = isEdit ? dao.updateClass(cm) : dao.addClass(cm);
            if (!success) {
                showAlert("Failed to save class. Class ID may already exist.");
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) loadAllData();
        });
    }

    // ========================================================
    // TAB 4: TIMETABLE
    // ========================================================
    private Tab createTimetableTab() {
        Tab tab = new Tab("Timetable");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 0, 0, 0));

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Class Weekly Schedules");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Timetable Slot");
        addBtn.setStyle(primaryBtnStyle());
        addBtn.setOnAction(e -> showTimetableDialog(null));

        topBar.getChildren().addAll(title, sp, addBtn);

        TableView<Timetable> table = new TableView<>(timetableList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Timetable, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Timetable, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(new PropertyValueFactory<>("classId"));

        TableColumn<Timetable, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));

        TableColumn<Timetable, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));

        TableColumn<Timetable, String> subCol = new TableColumn<>("Subject Code");
        subCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));

        TableColumn<Timetable, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            private final HBox box = new HBox(6, edit, del);
            {
                box.setAlignment(Pos.CENTER);
                edit.setStyle(editBtnStyle());
                del.setStyle(delBtnStyle());
                edit.setOnAction(e -> showTimetableDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    Timetable t = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Timetable Slot", t.getClassId() + " " + t.getDay())) {
                        dao.deleteTimetable(t.getId());
                        loadAllData();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, classCol, dayCol, timeCol, subCol, actCol);
        content.getChildren().addAll(topBar, table);
        tab.setContent(content);
        return tab;
    }

    private void showTimetableDialog(Timetable existing) {
        boolean isEdit = (existing != null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Timetable Slot" : "Add Timetable Slot");
        dialog.setHeaderText(isEdit ? "Update slot schedule" : "Assign subject slot to class");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(16));

        ComboBox<String> classCombo = new ComboBox<>();
        for (ClassModel cm : classList) classCombo.getItems().add(cm.getClassId());
        if (isEdit) classCombo.setValue(existing.getClassId());
        else if (!classCombo.getItems().isEmpty()) classCombo.getSelectionModel().selectFirst();
        classCombo.setStyle(inputStyle());

        ComboBox<String> dayCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        ));
        dayCombo.setValue(isEdit ? existing.getDay() : "Monday");
        dayCombo.setStyle(inputStyle());

        TextField timeF = new TextField(isEdit ? existing.getTimeSlot() : "09:00 - 10:00 AM");
        timeF.setStyle(inputStyle());

        ComboBox<String> subCombo = new ComboBox<>();
        for (Subject s : subjectList) subCombo.getItems().add(s.getCode());
        if (isEdit) subCombo.setValue(existing.getSubjectCode());
        else if (!subCombo.getItems().isEmpty()) subCombo.getSelectionModel().selectFirst();
        subCombo.setStyle(inputStyle());

        g.add(new Label("Class *"), 0, 0); g.add(classCombo, 1, 0);
        g.add(new Label("Day *"), 0, 1); g.add(dayCombo, 1, 1);
        g.add(new Label("Time Slot *"), 0, 2); g.add(timeF, 1, 2);
        g.add(new Label("Subject *"), 0, 3); g.add(subCombo, 1, 3);

        pane.setContent(g);

        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String cl = classCombo.getValue();
            String dy = dayCombo.getValue();
            String ts = timeF.getText().trim();
            String sb = subCombo.getValue();
            if (cl == null || dy == null || ts.isEmpty() || sb == null) {
                showAlert("All fields are required.");
                ev.consume();
                return;
            }
            Timetable t = isEdit ? existing : new Timetable();
            t.setClassId(cl);
            t.setDay(dy);
            t.setTimeSlot(ts);
            t.setSubjectCode(sb);
            boolean success = isEdit ? dao.updateTimetable(t) : dao.addTimetable(t);
            if (!success) {
                showAlert("Failed to save timetable slot.");
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) loadAllData();
        });
    }

    public void loadAllData() {
        courseList.setAll(dao.getAllCourses());
        subjectList.setAll(dao.getAllSubjects());
        classList.setAll(dao.getAllClasses());
        timetableList.setAll(dao.getAllTimetables());
    }

    private boolean confirmDelete(String type, String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete " + type + ": " + name);
        alert.setContentText("Are you sure you want to delete this record? This action cannot be undone.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Validation Notice");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String primaryBtnStyle() {
        return "-fx-background-color: #4054F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 6px 14px;";
    }

    private String editBtnStyle() {
        return "-fx-background-color: #F0FDF4; -fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3px 8px;";
    }

    private String delBtnStyle() {
        return "-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 3px 8px;";
    }

    private String inputStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 10px; -fx-font-size: 12px; -fx-text-fill: #172554;";
    }
}
