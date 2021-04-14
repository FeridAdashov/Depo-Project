package texel.com.depoproject.DataClasses;

import java.util.ArrayList;

public class DepoSellInfo {
    public ArrayList<String> name;
    public ArrayList<Double> price;
    public ArrayList<Double> amount;

    public DepoSellInfo(ArrayList<String> name, ArrayList<Double> price, ArrayList<Double> amount) {
        this.name = name;
        this.price = price;
        this.amount = amount;
    }
}
