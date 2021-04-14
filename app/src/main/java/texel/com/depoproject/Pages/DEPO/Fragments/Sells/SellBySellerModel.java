package texel.com.depoproject.Pages.DEPO.Fragments.Sells;

import texel.com.depoproject.HelperClasses.SharedClass;

public class SellBySellerModel {
    final String date;
    final String userName;
    final String name;
    final Double sellSum;
    final Double netIncome;
    final Double percentSum;

    public SellBySellerModel(String date, String userName,
                             String name, Double sellSum,
                             Double netIncome, Double percentSum) {
        this.date = date;
        this.userName = userName;
        this.name = name;
        this.sellSum = SharedClass.twoDigitDecimal(sellSum);
        this.netIncome = SharedClass.twoDigitDecimal(netIncome);
        this.percentSum = SharedClass.twoDigitDecimal(percentSum);
    }
}
