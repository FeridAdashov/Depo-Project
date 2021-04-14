package texel.com.depoproject.HelperClasses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateTime {
    public static SimpleDateFormat df_date = new SimpleDateFormat("yyyy_MM_dd");
    public static SimpleDateFormat df_time = new SimpleDateFormat("HH_mm_ss");

    public static String getDate(Date date) {
        return df_date.format(date);
    }

    public static Date getDate(String date) {
        try {
            return df_date.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTime(Date date) {
        return df_time.format(date);
    }
}
