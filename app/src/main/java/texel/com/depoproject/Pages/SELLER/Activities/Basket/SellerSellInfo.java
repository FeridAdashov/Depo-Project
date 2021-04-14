package texel.com.depoproject.Pages.SELLER.Activities.Basket;

import java.util.ArrayList;

public class SellerSellInfo {
    public String market;
    public ArrayList<String> name;
    public ArrayList<Double> price;
    public ArrayList<Double> amount;
    public Double percent;
    public Double netIncome;

    public SellerSellInfo(String market, ArrayList<String> name, ArrayList<Double> price, ArrayList<Double> amount, Double percent, Double netIncome) {
        this.market = market;
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.percent = percent;
        this.netIncome = netIncome;
    }

    public ArrayList<String> getName() {
        return name;
    }

    public void setName(ArrayList<String> name) {
        this.name = name;
    }

    public ArrayList<Double> getPrice() {
        return price;
    }

    public void setPrice(ArrayList<Double> price) {
        this.price = price;
    }

    public ArrayList<Double> getAmount() {
        return amount;
    }

    public void setAmount(ArrayList<Double> amount) {
        this.amount = amount;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(Double netIncome) {
        this.netIncome = netIncome;
    }

}
