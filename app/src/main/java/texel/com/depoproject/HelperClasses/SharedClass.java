package texel.com.depoproject.HelperClasses;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import texel.com.depoproject.R;

public class SharedClass {

    public static double twoDigitDecimal(Double d) {
        if (d == null) return 0.0;
        return Math.round(d * 100) / 100.0;
    }

    public static void configureSearchEditText(final EditText etSearch,
                                               ArrayList<String> nameList,
                                               ArrayList<String> searchNameList,
                                               RecyclerView.Adapter adapter) {
        etSearch.clearFocus();
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNameList.clear();

                String search = s.toString().toLowerCase();

                for (String name : nameList)
                    if (name.toLowerCase().contains(search)) searchNameList.add(name);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static boolean checkDate(String begin, String end, String between) {
        Date databaseDate = CustomDateTime.getDate(between);
        Date beginDate = CustomDateTime.getDate(begin);
        Date endDate = CustomDateTime.getDate(end);

        Calendar c = Calendar.getInstance();
        c.setTime(beginDate);
        c.add(Calendar.DATE, -1);
        beginDate = CustomDateTime.getDate(CustomDateTime.getDate(c.getTime()));

        c.setTime(endDate);
        c.add(Calendar.DATE, 1);
        endDate = CustomDateTime.getDate(CustomDateTime.getDate(c.getTime()));

        return databaseDate.after(beginDate) && databaseDate.before(endDate);
    }

    public static void showDatePickerDialog(Activity activity, DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity, listener,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, activity.getString(R.string.cancel), datePickerDialog);
        datePickerDialog.show();
    }

    public static void showSnackBar(Activity activity, String message) {
        View view = activity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(view,
                Html.fromHtml("<font color=\"#ffffff\"><big>" + message + "</big></font>"),
                Snackbar.LENGTH_LONG);

        snackbar.getView().setBackground(
                ResourcesCompat.getDrawable(activity.getResources(),
                        R.drawable.gradient_snack_bar, null));
        snackbar.show();
    }

    public static String convertAzToEn(String text)
    {
        return text.replace('ı', 'i')
                .replace('ə', 'e')
                .replace('ş', 's')
                .replace('ğ', 'g')
                .replace("ç", "c")
                .replace('ü', 'u');
    }
}
