import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

public class MainController {
    @FXML private TableView<Contract> tableContracts;
    @FXML private TableColumn<Contract, Integer> colId;
    @FXML private TableColumn<Contract, LocalDate> colDate;
    @FXML private TableColumn<Contract, Double> colAmount, colRate;
    @FXML private TableColumn<Contract, String> colBranch, colType;

    @FXML private TextField txtAmount, txtRate;
    @FXML private Label lblPremium;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Branch> comboBranch;
    @FXML private ComboBox<InsuranceType> comboType;

    private ContractDAO dao = new ContractDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeName"));

        comboBranch.setItems(dao.getBranches());
        comboType.setItems(dao.getTypes());
        refreshTable();

        txtAmount.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) txtAmount.setText(old);
        });
        txtRate.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) txtRate.setText(old);
        });
    }

    @FXML
    private void handleCalculatePremium() {
        try {
            double amount = Double.parseDouble(txtAmount.getText());
            double rate = Double.parseDouble(txtRate.getText());
            lblPremium.setText(String.format("%.2f", (amount * rate) / 100));
        } catch (Exception e) {
            lblPremium.setText("0.00");
        }
    }

    @FXML
    private void handleSaveContract() {
        try {
            LocalDate date = datePicker.getValue();
            Branch selectedBranch = comboBranch.getValue();
            InsuranceType selectedType = comboType.getValue();

            if (date == null || date.isAfter(LocalDate.now())) {
                System.out.println("Помилка: Некоректна дата!");
                return;
            }

            if (selectedBranch == null || selectedType == null || txtAmount.getText().isEmpty()) {
                System.out.println("Помилка: Заповніть усі поля!");
                return;
            }

            double amount = Double.parseDouble(txtAmount.getText());
            double rate = Double.parseDouble(txtRate.getText());

            dao.addContract(date, amount, rate, selectedBranch.getId(), selectedType.getId());

            tableContracts.setItems(dao.getAllContracts());

        } catch (NumberFormatException e) {
            System.out.println("Помилка: Введіть числові значення у поля Сума та Ставка!");
        }
    }

    private void refreshTable() {
        tableContracts.setItems(dao.getAllContracts());
    }

    @FXML
    private void handleExport() {
        ObservableList<Contract> data = tableContracts.getItems();

        if (data.isEmpty()) {
            System.out.println("Немає даних для експорту!");
            return;
        }

        File file = new File("contracts_export.txt");

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ЗВІТ: СПИСОК ДОГОВОРІВ СТРАХУВАННЯ");
            writer.println("--------------------------------------------------");
            writer.println(String.format("%-5s | %-12s | %-15s | %-15s | %-10s",
                    "№", "Дата", "Сума", "Філія", "Вид"));
            writer.println("--------------------------------------------------");

            for (Contract c : data) {
                writer.println(String.format("%-5d | %-12s | %-15.2f | %-15s | %-10s",
                        c.getId(),
                        c.getDate().toString(),
                        c.getAmount(),
                        c.getBranchName(),
                        c.getTypeName()
                ));
            }

            System.out.println("Експорт успішно завершено у файл: " + file.getAbsolutePath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Експорт");
            alert.setHeaderText(null);
            alert.setContentText("Дані успішно збережено у файл contracts_export.txt");
            alert.showAndWait();

        } catch (IOException e) {
            System.err.println("Помилка при записі у файл: " + e.getMessage());
            e.printStackTrace();
        }
    }
}