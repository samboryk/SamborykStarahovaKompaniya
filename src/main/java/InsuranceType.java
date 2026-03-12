public class InsuranceType {
    private int id;
    private String name;

    public InsuranceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() { return name; }

    public int getId() { return id; }
    public String getTypeName() { return name; }
}
