package texel.com.depoproject.Pages.DEPO.Fragments.Return.Sells;

import java.util.ArrayList;


public class ReturnSellByMarketModel {
    String seller;
    Double sum;
    String date;
    ArrayList<String> times = new ArrayList<>();

    public ReturnSellByMarketModel(String seller, Double sum, String date, String time) {
        this.seller = seller;
        this.sum = sum;
        this.date = date;
        this.times.add(time);
    }
}
