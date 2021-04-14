package texel.com.depoproject.DataClasses;

import java.util.ArrayList;

public class BuyerBuyInfo {
    public ArrayList<String> name;
    public ArrayList<Double> price;
    public ArrayList<Double> amount;
    public Double percent;
    public Double sum;

    public BuyerBuyInfo(ArrayList<String> name, ArrayList<Double> price,
                        ArrayList<Double> amount, Double percent, Double sum) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.percent = percent;
        this.sum = sum;
    }
}
