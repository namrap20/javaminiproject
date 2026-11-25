package sms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sms.model.ExamResult;
import sms.model.Subject;

import java.util.List;

/**
 * Exam: Examinations and Results Management Module.
 * 
 * Features:
 * - Enter marks and max marks
 * - Automatic real-time calculation of Percentage and Grade
 * - Full CRUD for Examination Results
 * - Official College Marksheet generator and printable view
 */
public class Exam extends VBox {

    private final ExamDAO examDAO = new ExamDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicDAO academicDAO = new AcademicDAO();

    private final ObservableList<ExamResult> resultList = FXCollections.observableArrayList();

    public Exam() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F8FAFC;");
        buildUI();
        loadResults();
    }

    private void buildUI() {
        // Heading
        Label heading = new Label("Examinations & Results");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#10265C"));

        Label desc = new Label("Record assessment evaluations, verify grading formulas, and generate official student marksheets.");
        desc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        desc.setTextFill(Color.web("#64748B"));

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        tabs.getTabs().addAll(
            createResultsTab(),
            createMarksheetTab()
        );

        getChildren().addAll(new VBox(4, heading, desc), tabs);
    }

    // ========================================================
    // TAB 1: ALL EXAM RESULTS
    // ========================================================
    private Tab createResultsTab() {
        Tab tab = new Tab("Assessment Results");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 0, 0, 0));

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Student Examination Records");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#10265C"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Record Marks");
        addBtn.setStyle("-fx-background-color: #4054F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 6px 14px;");
        addBtn.setOnAction(e -> showAddEditDialog(null));

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadResults());

        topBar.getChildren().addAll(title, sp, refreshBtn, addBtn);

        TableView<ExamResult> table = new TableView<>(resultList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ExamResult, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<ExamResult, String> stuIdCol = new TableColumn<>("Student ID");
        stuIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        stuIdCol.setPrefWidth(95);

        TableColumn<ExamResult, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        nameCol.setPrefWidth(180);

        TableColumn<ExamResult, String> subCol = new TableColumn<>("Subject Code");
        subCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subCol.setPrefWidth(100);

        TableColumn<ExamResult, Double> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        marksCol.setPrefWidth(70);

        TableColumn<ExamResult, Double> totalCol = new TableColumn<>("Max Marks");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(85);

        TableColumn<ExamResult, String> pctCol = new TableColumn<>("Percentage");
        pctCol.setPrefWidth(90);
        pctCol.setCellValueFactory(cell -> {
            ExamResult er = cell.getValue();
            double p = er.getTotal() > 0 ? (er.getMarks() / er.getTotal()) * 100.0 : 0.0;
            return new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", p));
        });

        TableColumn<ExamResult, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setPrefWidth(75);
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label l = new Label(item);
                    l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    if ("A+".equals(item) || "A".equals(item)) {
                        l.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
                    } else if ("B".equals(item) || "C".equals(item)) {
                        l.setStyle("-fx-background-color: #EEF3FF; -fx-text-fill: #4054F4; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
                    } else if ("D".equals(item)) {
                        l.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
                    } else {
                        l.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-padding: 2px 8px; -fx-background-radius: 4px;");
                    }
                    setGraphic(l);
                }
            }
        });

        TableColumn<ExamResult, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(140);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                box.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #16A34A; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 3px 8px; -fx-background-radius: 4px;");
                delBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 3px 8px; -fx-background-radius: 4px;");

                editBtn.setOnAction(e -> showAddEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    ExamResult er = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Result ID " + er.getId())) {
                        examDAO.deleteExamResult(er.getId());
                        loadResults();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, stuIdCol, nameCol, subCol, marksCol, totalCol, pctCol, gradeCol, actCol);

        content.getChildren().addAll(topBar, table);
        tab.setContent(content);
        return tab;
    }

    private void showAddEditDialog(ExamResult existing) {
        boolean isEdit = (existing != null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Examination Result" : "Record Assessment Marks");
        dialog.setHeaderText(isEdit ? "Update marks for result ID: " + existing.getId() : "Enter new examination evaluation");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(16));

        ComboBox<String> stuCombo = new ComboBox<>();
        stuCombo.setStyle(inputStyle());
        for (sms.model.Student s : studentDAO.getAllStudents()) {
            stuCombo.getItems().add(s.getId() + " - " + s.getName());
        }
        if (isEdit) {
            for (String item : stuCombo.getItems()) {
                if (item.startsWith(existing.getStudentId())) {
                    stuCombo.setValue(item);
                    break;
                }
            }
            stuCombo.setDisable(true);
        } else if (!stuCombo.getItems().isEmpty()) {
            stuCombo.getSelectionModel().selectFirst();
        }

        ComboBox<String> subCombo = new ComboBox<>();
        subCombo.setStyle(inputStyle());
        for (Subject s : academicDAO.getAllSubjects()) {
            subCombo.getItems().add(s.getCode() + " - " + s.getName());
        }
        if (isEdit) {
            for (String item : subCombo.getItems()) {
                if (item.startsWith(existing.getSubjectCode())) {
                    subCombo.setValue(item);
                    break;
                }
            }
            subCombo.setDisable(true);
        } else if (!subCombo.getItems().isEmpty()) {
            subCombo.getSelectionModel().selectFirst();
        }

        TextField marksF = new TextField(isEdit ? String.valueOf(existing.getMarks()) : "");
        marksF.setPromptText("e.g. 85.5");
        marksF.setStyle(inputStyle());

        TextField totalF = new TextField(isEdit ? String.valueOf(existing.getTotal()) : "100.0");
        totalF.setStyle(inputStyle());

        Label calcLabel = new Label("Grade Preview: -");
        calcLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        calcLabel.setTextFill(Color.web("#4054F4"));

        // Live calculation listener
        Runnable updateCalc = () -> {
            try {
                double m = Double.parseDouble(marksF.getText().trim());
                double t = Double.parseDouble(totalF.getText().trim());
                if (t > 0 && m >= 0 && m <= t) {
                    double p = (m / t) * 100.0;
                    String gr = ExamDAO.calculateGrade(m, t);
                    calcLabel.setText(String.format("Percentage: %.1f%%  |  Grade: %s", p, gr));
                } else {
                    calcLabel.setText("Invalid marks (must be 0 to " + t + ")");
                }
            } catch (Exception e) {
                calcLabel.setText("Grade Preview: -");
            }
        };

        marksF.textProperty().addListener((obs, old, val) -> updateCalc.run());
        totalF.textProperty().addListener((obs, old, val) -> updateCalc.run());
        updateCalc.run();

        g.add(new Label("Student *"), 0, 0); g.add(stuCombo, 1, 0);
        g.add(new Label("Subject *"), 0, 1); g.add(subCombo, 1, 1);
        g.add(new Label("Marks Obtained *"), 0, 2); g.add(marksF, 1, 2);
        g.add(new Label("Maximum Marks *"), 0, 3); g.add(totalF, 1, 3);
        g.add(calcLabel, 1, 4);

        pane.setContent(g);

        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String stuSel = stuCombo.getValue();
            String subSel = subCombo.getValue();
            if (stuSel == null || subSel == null) {
                showAlert("Please select a student and a subject.");
                ev.consume();
                return;
            }
            double m, t;
            try {
                m = Double.parseDouble(marksF.getText().trim());
                t = Double.parseDouble(totalF.getText().trim());
                if (t <= 0 || m < 0 || m > t) throw new Exception();
            } catch (Exception e) {
                showAlert("Marks must be between 0 and Maximum Marks.");
                ev.consume();
                return;
            }

            String sId = stuSel.split(" - ")[0].trim();
            String subCode = subSel.split(" - ")[0].trim();

            boolean success = isEdit ? examDAO.updateExamResult(existing.getId(), m, t) : examDAO.addExamResult(sId, subCode, m, t);
            if (!success) {
                showAlert("Failed to save exam result to MySQL database.");
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) loadResults();
        });
    }

    // ========================================================
    // TAB 2: MARKSHEET
    // ========================================================
    private Tab createMarksheetTab() {
        Tab tab = new Tab("Official Marksheet");
        tab.setClosable(false);

        VBox content = new VBox(14);
        content.setPadding(new Insets(16, 0, 0, 0));

        // Selector Bar
        HBox selBar = new HBox(12);
        selBar.setAlignment(Pos.CENTER_LEFT);

        Label selLbl = new Label("Select Student:");
        selLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));

        ComboBox<String> studentSelect = new ComboBox<>();
        studentSelect.setStyle(inputStyle());
        studentSelect.setPrefWidth(280);
        for (sms.model.Student s : studentDAO.getAllStudents()) {
            studentSelect.getItems().add(s.getId() + " - " + s.getName());
        }

        Button generateBtn = new Button("Generate Marksheet");
        generateBtn.setStyle("-fx-background-color: #4054F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button printBtn = new Button("🖨 Print Marksheet");
        printBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-cursor: hand; -fx-padding: 7px 14px; -fx-font-weight: bold;");

        selBar.getChildren().addAll(selLbl, studentSelect, generateBtn, sp, printBtn);

        // Marksheet Container Card
        VBox marksheetCard = new VBox(16);
        marksheetCard.setPadding(new Insets(24));
        marksheetCard.setMaxWidth(700);
        marksheetCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #CBD5E1; -fx-border-radius: 12px;");
        VBox.setVgrow(marksheetCard, Priority.ALWAYS);

        generateBtn.setOnAction(e -> {
            String sel = studentSelect.getValue();
            if (sel == null) return;
            String sid = sel.split(" - ")[0].trim();
            renderMarksheet(marksheetCard, sid);
        });

        printBtn.setOnAction(e -> printNode(marksheetCard));

        if (!studentSelect.getItems().isEmpty()) {
            studentSelect.getSelectionModel().selectFirst();
            String sid = studentSelect.getValue().split(" - ")[0].trim();
            renderMarksheet(marksheetCard, sid);
        }

        content.getChildren().addAll(selBar, new ScrollPane(marksheetCard));
        tab.setContent(content);
        return tab;
    }

    private void renderMarksheet(VBox card, String studentId) {
        card.getChildren().clear();

        sms.model.Student s = studentDAO.getStudentById(studentId);
        if (s == null) return;

        List<ExamResult> results = examDAO.getResultsByStudent(studentId);

        // College Header
        VBox colHeader = new VBox(4);
        colHeader.setAlignment(Pos.CENTER);
        Label instTitle = new Label("SMS INSTITUTE OF HIGHER EDUCATION & TECHNOLOGY");
        instTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        instTitle.setTextFill(Color.web("#10265C"));

        Label docTitle = new Label("OFFICIAL SEMESTER GRADE REPORT & MARKSHEET");
        docTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        docTitle.setTextFill(Color.web("#4054F4"));

        Label docSub = new Label("Academic Year 2024 - 2025");
        docSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        docSub.setTextFill(Color.web("#64748B"));

        colHeader.getChildren().addAll(instTitle, docTitle, docSub, new Separator());

        // Student Info Grid
        GridPane info = new GridPane();
        info.setHgap(20); info.setVgap(6);
        info.setPadding(new Insets(8, 0, 8, 0));

        info.add(boldLbl("Student ID:"), 0, 0); info.add(new Label(s.getId()), 1, 0);
        info.add(boldLbl("Student Name:"), 2, 0); info.add(new Label(s.getName()), 3, 0);
        info.add(boldLbl("GR Number:"), 0, 1); info.add(new Label(s.getGrNo()), 1, 1);
        info.add(boldLbl("Roll Number:"), 2, 1); info.add(new Label(s.getRollNo()), 3, 1);
        info.add(boldLbl("Course Code:"), 0, 2); info.add(new Label(s.getCourse()), 1, 2);
        info.add(boldLbl("Semester / Div:"), 2, 2); info.add(new Label("Semester " + s.getSemester() + " (Div " + s.getDivision() + ")"), 3, 2);

        // Table Header
        HBox tblHeader = new HBox(8);
        tblHeader.setPadding(new Insets(8, 12, 8, 12));
        tblHeader.setStyle("-fx-background-color: #EEF3FF; -fx-background-radius: 6px;");

        Label hSub = new Label("Subject"); hSub.setPrefWidth(220); hSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label hMax = new Label("Max Marks"); hMax.setPrefWidth(90); hMax.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label hObt = new Label("Marks Obtained"); hObt.setPrefWidth(110); hObt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label hPct = new Label("Percentage"); hPct.setPrefWidth(90); hPct.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label hGrd = new Label("Grade"); hGrd.setPrefWidth(60); hGrd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        tblHeader.getChildren().addAll(hSub, hMax, hObt, hPct, hGrd);

        // Result Rows
        VBox rowsBox = new VBox(2);
        double totalObt = 0;
        double totalMax = 0;

        for (ExamResult r : results) {
            totalObt += r.getMarks();
            totalMax += r.getTotal();

            HBox row = new HBox(8);
            row.setPadding(new Insets(6, 12, 6, 12));
            row.setStyle("-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

            Label rSub = new Label(r.getSubjectCode()); rSub.setPrefWidth(220);
            Label rMax = new Label(String.valueOf(r.getTotal())); rMax.setPrefWidth(90);
            Label rObt = new Label(String.valueOf(r.getMarks())); rObt.setPrefWidth(110);
            double pct = r.getTotal() > 0 ? (r.getMarks() / r.getTotal()) * 100.0 : 0.0;
            Label rPct = new Label(String.format("%.1f%%", pct)); rPct.setPrefWidth(90);
            Label rGrd = new Label(r.getGrade()); rGrd.setPrefWidth(60);
            rGrd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

            row.getChildren().addAll(rSub, rMax, rObt, rPct, rGrd);
            rowsBox.getChildren().add(row);
        }

        // Summary Bar
        double overallPct = totalMax > 0 ? (totalObt * 100.0) / totalMax : 0.0;
        String finalGrade = ExamDAO.calculateGrade(totalObt, totalMax);
        boolean passed = !"F".equals(finalGrade);

        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER_RIGHT);
        summaryBox.setPadding(new Insets(12, 16, 12, 16));
        summaryBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");

        Label totLbl = new Label(String.format("Grand Total: %.1f / %.1f", totalObt, totalMax));
        totLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label ovrLbl = new Label(String.format("Overall: %.1f%%", overallPct));
        ovrLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label grdLbl = new Label("Final Grade: " + finalGrade);
        grdLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        grdLbl.setTextFill(passed ? Color.web("#16A34A") : Color.web("#EF4444"));

        Label statusLbl = new Label(passed ? "RESULT: PASS" : "RESULT: FAIL");
        statusLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLbl.setTextFill(passed ? Color.web("#16A34A") : Color.web("#EF4444"));

        summaryBox.getChildren().addAll(totLbl, ovrLbl, grdLbl, statusLbl);

        // Signatures
        HBox sigBox = new HBox(60);
        sigBox.setAlignment(Pos.CENTER);
        sigBox.setPadding(new Insets(40, 20, 10, 20));

        VBox sig1 = new VBox(2, new Label("_______________________"), new Label("Controller of Examinations"));
        sig1.setAlignment(Pos.CENTER);
        VBox sig2 = new VBox(2, new Label("_______________________"), new Label("Principal / Academic Dean"));
        sig2.setAlignment(Pos.CENTER);
        sigBox.getChildren().addAll(sig1, sig2);

        card.getChildren().addAll(colHeader, info, tblHeader, rowsBox, summaryBox, sigBox);
    }

    private void printNode(Node node) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(getScene().getWindow())) {
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
            }
        }
    }

    private Label boldLbl(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#172554"));
        return l;
    }

    private void loadResults() {
        resultList.setAll(examDAO.getAllExamResults());
    }

    private boolean confirmDelete(String desc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete " + desc);
        alert.setContentText("Are you sure you want to delete this result? This action cannot be undone.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Notice");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String inputStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 10px; -fx-font-size: 12px; -fx-text-fill: #172554;";
    }
}
