package texel.com.depoproject.Pages.DEPO.Fragments.ReturnToDepo;

import texel.com.depoproject.HelperClasses.SharedClass;

public class ReturnToDepoModel {
    final String date;
    final String userName;
    final String name;
    final Double pureSum;
    final Double rottenSum;

    public ReturnToDepoModel(String date, String userName,
                             String name, Double pureSum, Double rottenSum) {
        this.date = date;
        this.userName = userName;
        this.name = name;
        this.pureSum = SharedClass.twoDigitDecimal(pureSum);
        this.rottenSum = SharedClass.twoDigitDecimal(rottenSum);
    }
}
