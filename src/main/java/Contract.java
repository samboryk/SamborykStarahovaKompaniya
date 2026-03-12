import java.time.LocalDate;

public class Contract {
    private int id;
    private LocalDate date;
    private double amount;
    private double rate;
    private String branchName;
    private String TypeName;

    public Contract(int id, LocalDate date, double amount, double rate, String branchName, String TypeName) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.rate = rate;
        this.branchName = branchName;
        this.TypeName = TypeName;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public double getRate() { return rate; }
    public String getBranchName() { return branchName; }
    public String getTypeName() { return TypeName; }
}