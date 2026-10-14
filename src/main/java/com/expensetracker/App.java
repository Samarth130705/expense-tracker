package com.expensetracker;

import com.expensetracker.db.Database;
import com.expensetracker.db.ExpenseDao;
import com.expensetracker.model.Expense;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class App extends Application {

    private static final String[] CATEGORIES =
            {"Food", "Transport", "Rent", "Utilities", "Shopping", "Health", "Entertainment", "Other"};

    private final ExpenseDao dao = new ExpenseDao();

    private final TableView<Expense> table = new TableView<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(CATEGORIES));
    private final TextField descField = new TextField();
    private final TextField amountField = new TextField();
    private final ComboBox<Month> monthBox = new ComboBox<>(FXCollections.observableArrayList(Month.values()));
    private final Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, LocalDate.now().getYear());
    private final Label totalLabel = new Label();
    private final PieChart pieChart = new PieChart();
    private final BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    @Override
    public void start(Stage stage) {
        Database.init();

        // ---- form ----
        categoryBox.getSelectionModel().selectFirst();
        descField.setPromptText("Description");
        amountField.setPromptText("Amount");
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        addBtn.setOnAction(e -> save(false));
        updateBtn.setOnAction(e -> save(true));
        deleteBtn.setOnAction(e -> deleteSelected());
        clearBtn.setOnAction(e -> clearForm());
        HBox form = new HBox(8, datePicker, categoryBox, descField, amountField, addBtn, updateBtn, deleteBtn, clearBtn);
        form.setPadding(new Insets(10));
        HBox.setHgrow(descField, Priority.ALWAYS);

        // ---- month selector ----
        monthBox.setValue(LocalDate.now().getMonth());
        monthBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Month m) { return m == null ? "" : m.getDisplayName(TextStyle.FULL, Locale.ENGLISH); }
            public Month fromString(String s) { return null; }
        });
        yearSpinner.setEditable(true);
        monthBox.setOnAction(e -> refresh());
        yearSpinner.valueProperty().addListener((o, a, b) -> refresh());
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        HBox monthBar = new HBox(10, new Label("Month:"), monthBox, new Label("Year:"), yearSpinner, totalLabel);
        monthBar.setPadding(new Insets(0, 10, 10, 10));

        // ---- table ----
        TableColumn<Expense, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Expense, Double> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        table.getColumns().addAll(dateCol, catCol, descCol, amtCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener((o, old, e) -> {
            if (e != null) {
                datePicker.setValue(e.getDate());
                categoryBox.setValue(e.getCategory());
                descField.setText(e.getDescription());
                amountField.setText(String.valueOf(e.getAmount()));
            }
        });

        // ---- charts ----
        pieChart.setTitle("Spending by category");
        barChart.setTitle("Monthly totals");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        pieChart.setAnimated(false);
        HBox charts = new HBox(10, pieChart, barChart);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        charts.setPrefHeight(300);

        VBox root = new VBox(form, monthBar, table, charts);
        VBox.setVgrow(table, Priority.ALWAYS);

        refresh();
        stage.setTitle("Expense Tracker");
        stage.setScene(new Scene(root, 1050, 720));
        stage.show();
    }

    private YearMonth selectedMonth() {
        return YearMonth.of(yearSpinner.getValue(), monthBox.getValue());
    }

    private void save(boolean isUpdate) {
        try {
            if (datePicker.getValue() == null || categoryBox.getValue() == null)
                throw new IllegalArgumentException("Pick a date and category.");
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");

            if (isUpdate) {
                Expense sel = table.getSelectionModel().getSelectedItem();
                if (sel == null) throw new IllegalArgumentException("Select a row to update.");
                dao.update(new Expense(sel.getId(), datePicker.getValue(), categoryBox.getValue(),
                        descField.getText().trim(), amount));
            } else {
                dao.add(new Expense(0, datePicker.getValue(), categoryBox.getValue(),
                        descField.getText().trim(), amount));
            }
            // jump to the month of the saved expense so the user sees it
            YearMonth ym = YearMonth.from(datePicker.getValue());
            yearSpinner.getValueFactory().setValue(ym.getYear());
            monthBox.setValue(ym.getMonth());
            clearForm();
            refresh();
        } catch (NumberFormatException ex) {
            error("Please enter a valid number for amount.");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void deleteSelected() {
        Expense sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { error("Select a row to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this expense?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { dao.delete(sel.getId()); clearForm(); refresh(); }
            catch (Exception ex) { error(ex.getMessage()); }
        }
    }

    private void clearForm() {
        datePicker.setValue(LocalDate.now());
        categoryBox.getSelectionModel().selectFirst();
        descField.clear();
        amountField.clear();
        table.getSelectionModel().clearSelection();
    }

    private void refresh() {
        if (monthBox.getValue() == null || yearSpinner.getValue() == null) return;
        try {
            YearMonth ym = selectedMonth();
            var expenses = dao.findByMonth(ym);
            table.setItems(FXCollections.observableArrayList(expenses));
            double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
            totalLabel.setText(String.format("   Total this month: %.2f", total));

            pieChart.getData().clear();
            for (Map.Entry<String, Double> en : dao.categoryTotals(ym).entrySet())
                pieChart.getData().add(new PieChart.Data(en.getKey(), en.getValue()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            double[] totals = dao.monthlyTotals(ym.getYear());
            for (int i = 0; i < 12; i++)
                series.getData().add(new XYChart.Data<>(
                        Month.of(i + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH), totals[i]));
            barChart.getData().setAll(series);
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
