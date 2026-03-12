import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;

public class ContractDAO {
    public ObservableList<Contract> getAllContracts() {
        ObservableList<Contract> list = FXCollections.observableArrayList();
        String sql = "SELECT c.id, c.contract_date, c.amount, c.rate, b.name AS branch_name, t.type_name " +
                "FROM contracts c " +
                "JOIN branches b ON c.branch_id = b.id " +
                "JOIN insurance_types t ON c.type_id = t.id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Contract(
                        rs.getInt("id"),
                        rs.getDate("contract_date").toLocalDate(),
                        rs.getDouble("amount"),
                        rs.getDouble("rate"),
                        rs.getString("branch_name"),
                        rs.getString("type_name")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ObservableList<Branch> getBranches() {
        ObservableList<Branch> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM branches")) {
            while (rs.next()) {
                list.add(new Branch(rs.getInt("id"), rs.getString("name"), rs.getString("address"), rs.getString("phone")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ObservableList<InsuranceType> getTypes() {
        ObservableList<InsuranceType> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM insurance_types")) {
            while (rs.next()) {
                list.add(new InsuranceType(rs.getInt("id"), rs.getString("type_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addContract(LocalDate date, double amount, double rate, int branchId, int typeId) {
        String sql = "INSERT INTO contracts (contract_date, amount, rate, branch_id, type_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setDouble(2, amount);
            pstmt.setDouble(3, rate);
            pstmt.setInt(4, branchId);
            pstmt.setInt(5, typeId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}