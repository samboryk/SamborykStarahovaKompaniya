import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

    @FXML private TextField txtAmount, txtRate, txtSearch;
    @FXML private Label lblPremium;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Branch> comboBranch, comboFilterBranch;
    @FXML private ComboBox<InsuranceType> comboType;

    private ContractDAO dao = new ContractDAO();

    private ObservableList<Contract> masterData = FXCollections.observableArrayList();

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
        comboFilterBranch.setItems(dao.getBranches());

        masterData.setAll(dao.getAllContracts());

        FilteredList<Contract> filteredData = new FilteredList<>(masterData, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(contract -> matchFilters(contract, newValue, comboFilterBranch.getValue()));
        });

        comboFilterBranch.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(contract -> matchFilters(contract, txtSearch.getText(), newValue));
        });

        SortedList<Contract> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableContracts.comparatorProperty());
        tableContracts.setItems(sortedData);

        txtAmount.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) txtAmount.setText(old);
        });
        txtRate.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) txtRate.setText(old);
        });
    }


    private boolean matchFilters(Contract contract, String searchText, Branch filterBranch) {
        boolean matchBranch = filterBranch == null || contract.getBranchName().equals(filterBranch.getName());

        if (searchText == null || searchText.isEmpty()) {
            return matchBranch;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        boolean matchSearch = String.valueOf(contract.getId()).contains(lowerCaseFilter) ||
                contract.getTypeName().toLowerCase().contains(lowerCaseFilter) ||
                contract.getBranchName().toLowerCase().contains(lowerCaseFilter);

        return matchBranch && matchSearch;
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
                showAlert(Alert.AlertType.WARNING, "Помилка валідації", "Дата укладання договору не може бути порожньою або з майбутнього!");
                return;
            }

            if (selectedBranch == null || selectedType == null || txtAmount.getText().isEmpty() || txtRate.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Помилка валідації", "Будь ласка, заповніть усі поля перед збереженням!");
                return;
            }

            double amount = Double.parseDouble(txtAmount.getText());
            double rate = Double.parseDouble(txtRate.getText());


            dao.addContract(date, amount, rate, selectedBranch.getId(), selectedType.getId());


            refreshTable();


            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Новий договір успішно додано до бази!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка вводу", "Введіть числові значення у поля 'Сума страхування' та 'Тарифна ставка'!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Сталася помилка при збереженні: " + e.getMessage());
        }
    }

    private void refreshTable() {

        masterData.setAll(dao.getAllContracts());
    }

    @FXML
    private void handleExport() {
        ObservableList<Contract> data = tableContracts.getItems();

        if (data.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Експорт", "Немає даних для експорту!");
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

            showAlert(Alert.AlertType.INFORMATION, "Експорт", "Дані успішно збережено у файл:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка запису", "Не вдалося зберегти файл: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}