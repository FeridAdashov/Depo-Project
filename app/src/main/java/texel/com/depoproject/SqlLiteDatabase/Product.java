package texel.com.depoproject.SqlLiteDatabase;

import texel.com.depoproject.HelperClasses.SharedClass;

public class Product {
    public String id_name;
    public String name;
    public Double buyPrice;
    public Double sellPrice;
    public Double wantedAmount;
    public Double wantedAmountRotten = 0.0;

    public Product(String id_name, String name, Double buyPrice, Double sellPrice, Double wantedAmount) {
        this.id_name = id_name;
        this.name = name;
        this.buyPrice = SharedClass.twoDigitDecimal(buyPrice);
        this.sellPrice = SharedClass.twoDigitDecimal(sellPrice);
        this.wantedAmount = SharedClass.twoDigitDecimal(wantedAmount);
    }
}
