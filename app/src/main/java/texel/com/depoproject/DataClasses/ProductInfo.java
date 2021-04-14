package texel.com.depoproject.DataClasses;

public class ProductInfo {
    public String name;
    public Double buyPrice;
    public Double sellPrice;
    public boolean active;

    public ProductInfo(String name, Double buyPrice, Double sellPrice, Boolean active) {
        this.name = name;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.active = active != null && active;
    }
}

