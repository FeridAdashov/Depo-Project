package texel.com.depoproject.DataClasses;

public class MarketInfo {
    public String name;
    public String phone;
    public String address;
    public Double debt;
    public boolean active;

    public MarketInfo(String name, String phone, String address, Boolean active) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.active = active != null && active;
    }
}

