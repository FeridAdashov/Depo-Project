package texel.com.depoproject.Pages.DEPO.Fragments.Return.Sells;

import texel.com.depoproject.HelperClasses.SharedClass;

public class ReturnSellBySellerModel {
    final String date;
    final String userName;
    final String name;
    final Double sellSum;
    final Double percentSum;

    public ReturnSellBySellerModel(String date, String userName,
                                   String name, Double sellSum, Double percentSum) {
        this.date = date;
        this.userName = userName;
        this.name = name;
        this.sellSum = SharedClass.twoDigitDecimal(sellSum);
        this.percentSum = SharedClass.twoDigitDecimal(percentSum);
    }
}
