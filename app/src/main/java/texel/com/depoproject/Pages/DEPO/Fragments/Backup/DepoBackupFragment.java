package texel.com.depoproject.Pages.DEPO.Fragments.Backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import texel.com.depoproject.ExternalStorage.StorageFunctions;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.HelperClasses.TableGenerator;
import texel.com.depoproject.R;

public class DepoBackupFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private View view;
    private TextView textViewBeginDate;
    private TextView textViewEndDate;
    private final ArrayList<TextView> radioTextViewList = new ArrayList<>();
    private final boolean[] radioButtonStatus = {false, false, false, false, false, false, false};

    private DatabaseReference dr;

    private Activity activity;
    private boolean dateStatus;
    private String beginDate;
    private String endDate;

    private final GenericTypeIndicator<ArrayList<String>> s = new GenericTypeIndicator<ArrayList<String>>() {
    };
    private final GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_depo_backup, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;
        this.activity = getActivity();

        dr = DatabaseFunctions.getDatabases(activity).get(0);

        configureRadioButtons();
        configureDateSection();
        configureButtons();
    }

    private void configureRadioButtons() {
        radioTextViewList.add(view.findViewById(R.id.textViewExpenses));
        radioTextViewList.add(view.findViewById(R.id.textViewSells));
        radioTextViewList.add(view.findViewById(R.id.textViewTextures));
        radioTextViewList.add(view.findViewById(R.id.textViewBuys));
        radioTextViewList.add(view.findViewById(R.id.textViewDebts));
        radioTextViewList.add(view.findViewById(R.id.textViewReturns));
        radioTextViewList.add(view.findViewById(R.id.textViewReturnToDepo));

        for (int i = 0; i < radioTextViewList.size(); i++) {
            int finalI = i;
            radioTextViewList.get(i).setOnClickListener(v -> checkRadioButton(finalI));
        }
    }

    private void checkRadioButton(int i) {
        radioButtonStatus[i] = !radioButtonStatus[i];

        radioTextViewList.get(i).setBackgroundColor(
                getResources().getColor(radioButtonStatus[i] ? R.color.white : R.color.edit_text_back));

        radioTextViewList.get(i).setTextColor(
                getResources().getColor(radioButtonStatus[i] ? R.color.colorNavText : R.color.white));

        radioTextViewList.get(i).setCompoundDrawablesWithIntrinsicBounds(
                radioButtonStatus[i] ? R.drawable.ic_checked_box : R.drawable.ic_unchecked_box_white,
                0, 0, 0);
    }

    private void configureDateSection() {
        textViewBeginDate = view.findViewById(R.id.textViewBeginDate);
        textViewEndDate = view.findViewById(R.id.textViewEndDate);

        beginDate = CustomDateTime.getDate(new Date());
        endDate = CustomDateTime.getDate(new Date());
        textViewBeginDate.setText(beginDate);
        textViewEndDate.setText(endDate);

        textViewBeginDate.setOnClickListener(v -> {
            dateStatus = true;
            SharedClass.showDatePickerDialog(activity, this);
        });

        textViewEndDate.setOnClickListener(v -> {
            dateStatus = false;
            SharedClass.showDatePickerDialog(activity, this);
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        ++month;
        String m = month < 10 ? "0" + month : "" + month;
        String d = day < 10 ? "0" + day : "" + day;

        if (dateStatus) {
            beginDate = year + "_" + m + "_" + d;
            textViewBeginDate.setText(beginDate);
        } else {
            endDate = year + "_" + m + "_" + d;
            textViewEndDate.setText(endDate);
        }
    }

    private void configureButtons() {
        TextView textViewDeleteInfo = view.findViewById(R.id.textViewDeleteInfo);
        TextView textViewSaveInfo = view.findViewById(R.id.textViewSaveInfo);

        textViewDeleteInfo.setOnClickListener(v -> delete());
        textViewSaveInfo.setOnClickListener(v -> save());
    }

    private void save() {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.save, (dialog, which) -> {
            if(radioButtonStatus[0]) saveExpenses();
            if(radioButtonStatus[1]) saveSells();
            if(radioButtonStatus[2]) saveTextures();
            if(radioButtonStatus[3]) saveBuys();
            if(radioButtonStatus[4]) saveDebts();
            if(radioButtonStatus[5]) saveReturn();
            if(radioButtonStatus[6]) saveReturnToDepo();
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.show();
    }

    private void saveReturnToDepo() {
        dr.child("RETURN_TO_DEPO")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> headersList = new ArrayList<>();
                        headersList.add("Ad");
                        headersList.add("Saf Miqdar");
                        headersList.add("Çürük Miqdar");
                        headersList.add("Qiymət");

                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            String date = snapshotDates.getKey();

                            if (!SharedClass.checkDate(beginDate, endDate, date))
                                continue;

                            StringBuilder text = new StringBuilder();

                            for (DataSnapshot snapshotSellers : snapshotDates.getChildren()) {
                                String seller = snapshotSellers.child("seller").getValue(String.class);
                                Double pureSum = snapshotSellers.child("pureSum").getValue(Double.class);
                                Double rottenSum = snapshotSellers.child("rottenSum").getValue(Double.class);

                                text.append("\n\n\n")
                                        .append(seller)
                                        .append("  -  Ümumi Saf Qiymət: ")
                                        .append(pureSum)
                                        .append("  -  Ümumi Çürük Qiymət: ")
                                        .append(rottenSum).append("\n\n");

                                for (DataSnapshot snapshotTime : snapshotSellers.getChildren()) {
                                    String time = snapshotTime.getKey();

                                    if (time == null || !time.contains("_")) continue;

                                    text.append(time);

                                    ArrayList<ArrayList<String>> rowsList = new ArrayList<>();

                                    ArrayList<String> nameList = snapshotTime.child("name").getValue(s);
                                    ArrayList<Double> priceList = snapshotTime.child("price").getValue(d);
                                    ArrayList<Double> pureAmountList = snapshotTime.child("pureAmount").getValue(d);
                                    ArrayList<Double> rottenAmountList = snapshotTime.child("rottenAmount").getValue(d);

                                    for (int i = 0; i < nameList.size(); ++i) {
                                        ArrayList<String> row = new ArrayList<>();
                                        row.add(nameList.get(i));
                                        row.add(String.valueOf(pureAmountList.get(i)));
                                        row.add(String.valueOf(rottenAmountList.get(i)));
                                        row.add(String.valueOf(priceList.get(i)));
                                        rowsList.add(row);
                                    }

                                    TableGenerator tableGenerator = new TableGenerator();
                                    text.append(tableGenerator.generateTable(headersList, rowsList)).append("\n\n");
                                }
                            }
                            StorageFunctions.store(activity,
                                    "İadə",
                                    date,
                                    text.toString());
                        }
                        SharedClass.showSnackBar(activity, "İadə saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void saveReturn() {
        _saveReturnSell();
        _saveReturnBuy();
    }

    private void _saveReturnSell() {
        dr.child("RETURN/SELL")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> headersList = new ArrayList<>();
                        headersList.add("Ad");
                        headersList.add("Miqdar");
                        headersList.add("Qiymət");

                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            String date = snapshotDates.getKey();

                            if (!SharedClass.checkDate(beginDate, endDate, date))
                                continue;

                            StringBuilder text = new StringBuilder();

                            for (DataSnapshot snapshotSellers : snapshotDates.getChildren()) {
                                String seller = snapshotSellers.getKey();
                                Double percentSum = snapshotSellers.child("percentSum").getValue(Double.class);
                                Double commonSum = snapshotSellers.child("sum").getValue(Double.class);

                                text.append("\n\n\n")
                                        .append(seller)
                                        .append("  --  Ümumi faiz: ")
                                        .append(percentSum)
                                        .append("  --  Ümumi Cəm: ")
                                        .append(commonSum).append("\n\n");

                                for (DataSnapshot snapshotTime : snapshotSellers.getChildren()) {
                                    String time = snapshotTime.getKey();

                                    if (time == null || !time.contains("_")) continue;

                                    text.append(time);

                                    ArrayList<ArrayList<String>> rowsList = new ArrayList<>();

                                    ArrayList<String> nameList = snapshotTime.child("name").getValue(s);
                                    ArrayList<Double> priceList = snapshotTime.child("price").getValue(d);
                                    ArrayList<Double> amountList = snapshotTime.child("amount").getValue(d);
                                    Double percent = snapshotTime.child("percent").getValue(Double.class);

                                    for (int i = 0; i < nameList.size(); ++i) {
                                        ArrayList<String> row = new ArrayList<>();
                                        row.add(nameList.get(i));
                                        row.add(String.valueOf(amountList.get(i)));
                                        row.add(String.valueOf(priceList.get(i)));
                                        rowsList.add(row);
                                    }
                                    ArrayList<String> row = new ArrayList<>();
                                    row.add("Faiz");
                                    row.add(String.valueOf(percent));
                                    rowsList.add(row);

                                    TableGenerator tableGenerator = new TableGenerator();
                                    text.append(tableGenerator.generateTable(headersList, rowsList)).append("\n\n");
                                }
                            }
                            StorageFunctions.store(activity,
                                    "Geri Alim - Satışlar",
                                    date,
                                    text.toString());
                        }
                        SharedClass.showSnackBar(activity, "Geri Alim - Satışlar yadda saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void _saveReturnBuy() {
        dr.child("RETURN/BUY")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> headersList = new ArrayList<>();
                        headersList.add("Ad");
                        headersList.add("Miqdar");
                        headersList.add("Qiymət");

                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            String date = snapshotDates.getKey();

                            if (!SharedClass.checkDate(beginDate, endDate, date)) continue;

                            StringBuilder text = new StringBuilder();

                            Double percentSum = snapshotDates.child("percentSum").getValue(Double.class);
                            Double commonSum = snapshotDates.child("sum").getValue(Double.class);

                            text.append("\n\n\n")
                                    .append("Ümumi faiz: ")
                                    .append(percentSum)
                                    .append("  --  Ümumi Cəm: ")
                                    .append(commonSum).append("\n\n");

                            for (DataSnapshot snapshotTime : snapshotDates.getChildren()) {
                                String time = snapshotTime.getKey();

                                if (time == null || !time.contains("_")) continue;

                                text.append(time);

                                ArrayList<ArrayList<String>> rowsList = new ArrayList<>();

                                ArrayList<String> nameList = snapshotTime.child("name").getValue(s);
                                ArrayList<Double> priceList = snapshotTime.child("price").getValue(d);
                                ArrayList<Double> amountList = snapshotTime.child("amount").getValue(d);
                                Double percent = snapshotTime.child("percent").getValue(Double.class);
                                Double sum = snapshotTime.child("sum").getValue(Double.class);

                                for (int i = 0; i < nameList.size(); ++i) {
                                    ArrayList<String> row = new ArrayList<>();
                                    row.add(nameList.get(i));
                                    row.add(String.valueOf(amountList.get(i)));
                                    row.add(String.valueOf(priceList.get(i)));
                                    rowsList.add(row);
                                }
                                ArrayList<String> row = new ArrayList<>();
                                row.add("Cəm");
                                row.add(String.valueOf(sum));
                                rowsList.add(row);

                                row = new ArrayList<>();
                                row.add("Faiz");
                                row.add(String.valueOf(percent));
                                rowsList.add(row);

                                TableGenerator tableGenerator = new TableGenerator();
                                text.append(tableGenerator.generateTable(headersList, rowsList)).append("\n\n");
                            }

                            StorageFunctions.store(activity,
                                    "Geri Alim - Alışlar",
                                    date,
                                    text.toString());
                        }
                        SharedClass.showSnackBar(activity, "Geri Alim - Alışlar yadda saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void saveDebts() {
        _saveDebt(true);
        _saveDebt(false);
    }

    private void _saveDebt(boolean isFabric) {
        dr.child("DEBTS/" + (isFabric ? "FABRIC" : "MARKET"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotName : snapshot.getChildren()) {
                            ArrayList<String> headersList = new ArrayList<>();

                            headersList.add("Saat");
                            headersList.add("Miqdar");

                            Double sum = snapshotName.child("sum").getValue(Double.class);

                            String text = "";
                            for (DataSnapshot snapshotDates : snapshotName.getChildren()) {
                                String date = snapshotDates.getKey();
                                if (date == null || date.equals("sum")) continue;
                                if (!SharedClass.checkDate(beginDate, endDate, date)) continue;

                                text = date + "    ---    " + "Cem: " + sum;

                                ArrayList<ArrayList<String>> rowsList = new ArrayList<>();

                                for (DataSnapshot snapshotTime : snapshotDates.getChildren()) {
                                    ArrayList<String> row = new ArrayList<>();
                                    row.add(snapshotTime.getKey());
                                    row.add(String.valueOf(snapshotTime.getValue(Double.class)));
                                    rowsList.add(row);
                                }

                                TableGenerator tableGenerator = new TableGenerator();
                                text += tableGenerator.generateTable(headersList, rowsList) + "\n";
                            }

                            StorageFunctions.store(activity, "Borclar/" + (isFabric ? "ZAVOD" : "MARKET"),
                                    snapshotName.getKey(), text);
                            SharedClass.showSnackBar(activity, "Borclar yadda saxlanıldı");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void saveBuys() {
        dr.child("BUYS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            String date = snapshotDates.getKey();

                            if (!SharedClass.checkDate(beginDate, endDate, date))
                                continue;

                            for (DataSnapshot snapshotTime : snapshotDates.getChildren()) {
                                String time = snapshotTime.getKey();
                                if (time == null || !time.contains("_")) continue;

                                ArrayList<String> nameList = snapshotTime.child("name").getValue(s);
                                ArrayList<Double> amountList = snapshotTime.child("amount").getValue(d);
                                ArrayList<Double> priceList = snapshotTime.child("price").getValue(d);
                                Double sum = snapshotTime.child("sum").getValue(Double.class);
                                Double percent = snapshotTime.child("percent").getValue(Double.class);

                                if (nameList == null || nameList.size() == 0) return;

                                TableGenerator tableGenerator = new TableGenerator();

                                ArrayList<String> headersList = new ArrayList<>();
                                headersList.add("Ad");
                                headersList.add("Miqdar");
                                headersList.add("Qiymət");

                                ArrayList<ArrayList<String>> rowsList = new ArrayList<>();

                                for (int i = 0; i < nameList.size(); ++i) {
                                    ArrayList<String> row = new ArrayList<>();
                                    row.add(nameList.get(i));
                                    row.add(String.valueOf(amountList.get(i)));
                                    row.add(String.valueOf(priceList.get(i)));
                                    rowsList.add(row);
                                }

                                String text = tableGenerator.generateTable(headersList, rowsList);
                                StorageFunctions.store(activity, "Alışlar/" + date, time,
                                        text + "\nCəm: " + sum + "\nFaiz: " + percent);
                            }
                        }
                        SharedClass.showSnackBar(activity, "Alışlar yadda saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void saveExpenses() {
        saveExpenses("Percent", "Faiz");
        saveExpenses("Other", "Digər");
    }

    private void saveExpenses(String childName, String folderName) {
        dr.child("EXPENSES/" + childName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        TableGenerator tableGenerator = new TableGenerator();
                        ArrayList<String> headersList = new ArrayList<>();
                        headersList.add("Saat");
                        headersList.add("Ad");
                        headersList.add("Qiymət");

                        for (DataSnapshot snapshotUsers : snapshot.getChildren()) {
                            String userName = snapshotUsers.getKey();

                            StringBuilder text = new StringBuilder();
                            for (DataSnapshot snapshotDates : snapshotUsers.getChildren()) {
                                String date = snapshotDates.getKey();
                                Double sum = snapshotDates.child("sum").getValue(Double.class);

                                if (!SharedClass.checkDate(beginDate, endDate, date)) continue;

                                text.append(date).append("  ---  Cəm: ").append(sum).append("\n");

                                ArrayList<ArrayList<String>> rowsList = new ArrayList<>();
                                for (DataSnapshot snapshotTime : snapshotDates.getChildren()) {
                                    String time = snapshotTime.getKey();
                                    if (time == null || !time.contains("_")) continue;

                                    ArrayList<String> row = new ArrayList<>();
                                    row.add(time);
                                    row.add(String.valueOf(snapshotTime.child("name").getValue(String.class)));
                                    row.add(String.valueOf(snapshotTime.child("value").getValue(Double.class)));
                                    rowsList.add(row);
                                }
                                text.append(tableGenerator.generateTable(headersList, rowsList));
                            }
                            StorageFunctions.store(activity,
                                    "Xərclər/" + folderName,
                                    userName, text.toString());
                        }
                        SharedClass.showSnackBar(activity, "Xərclər yadda saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void saveSells() {
        dr.child("SELLS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        TableGenerator tableGenerator = new TableGenerator();
                        ArrayList<String> headersList = new ArrayList<>();
                        headersList.add("Ad");
                        headersList.add("Qiymət");
                        headersList.add("Miqdar");

                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            String date = snapshotDates.getKey();

                            if (!SharedClass.checkDate(beginDate, endDate, date)) continue;

                            StringBuilder text = new StringBuilder();
                            for (DataSnapshot snapshotSellers : snapshotDates.getChildren()) {
                                String seller = snapshotSellers.getKey();
                                Double sum = snapshotSellers.child("sum").getValue(Double.class);
                                Double percentSum = snapshotSellers.child("percentSum").getValue(Double.class);
                                Double netSum = snapshotSellers.child("netIncome").getValue(Double.class);

                                text.append(seller)
                                        .append(" ** Ümumi cəm: ").append(sum)
                                        .append(" ** Ümumi faiz: ").append(SharedClass.twoDigitDecimal(percentSum))
                                        .append(" ** Ümumi xalis: ").append(SharedClass.twoDigitDecimal(netSum)).append("\n\n");

                                for (DataSnapshot snapshotTime : snapshotSellers.getChildren()) {
                                    String time = snapshotTime.getKey();
                                    if (time == null || !time.contains("_")) continue;

                                    text.append(time);

                                    ArrayList<String> _nameList = snapshotTime.child("name").getValue(s);
                                    ArrayList<Double> _priceList = snapshotTime.child("price").getValue(d);
                                    ArrayList<Double> _amountList = snapshotTime.child("amount").getValue(d);
                                    String _market = snapshotTime.child("market").getValue(String.class);
                                    Double _percent = snapshotTime.child("percent").getValue(Double.class);
                                    Double _netIncome = snapshotTime.child("netIncome").getValue(Double.class);

                                    ArrayList<ArrayList<String>> rowsList = new ArrayList<>();
                                    for (int i = 0; i < _nameList.size(); ++i) {
                                        ArrayList<String> row = new ArrayList<>();
                                        row.add(_nameList.get(i));
                                        row.add(String.valueOf(_priceList.get(i)));
                                        row.add(String.valueOf(_amountList.get(i)));
                                        rowsList.add(row);
                                    }
                                    ArrayList<String> row = new ArrayList<>();
                                    row.add("Market");
                                    row.add(_market);
                                    rowsList.add(row);

                                    row = new ArrayList<>();
                                    row.add("Faiz");
                                    row.add(String.valueOf(SharedClass.twoDigitDecimal(_percent)));
                                    rowsList.add(row);

                                    row = new ArrayList<>();
                                    row.add("Xalis Gəlir");
                                    row.add(String.valueOf(SharedClass.twoDigitDecimal(_netIncome)));
                                    rowsList.add(row);

                                    text.append(tableGenerator.generateTable(headersList, rowsList)).append("\n\n");
                                }
                            }
                            StorageFunctions.store(activity, "Satışlar", date, text.toString());
                        }
                        SharedClass.showSnackBar(activity, "Satışlar yadda saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void saveTextures() {
        dr.child("TEXTURES")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        TableGenerator tableGenerator = new TableGenerator();
                        ArrayList<String> headersList = new ArrayList<>();
                        headersList.add("Ad");
                        headersList.add("Qiymət");
                        headersList.add("Miqdar");

                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            String date = snapshotDates.getKey();

                            if (!SharedClass.checkDate(beginDate, endDate, date)) continue;

                            StringBuilder text = new StringBuilder();

                            for (DataSnapshot snapshotUsers : snapshotDates.getChildren()) {
                                String user = snapshotUsers.getKey().split("_")[1];
                                Double sum = snapshotUsers.child("sum").getValue(Double.class);

                                text.append(user).append("  *** Cəm: ").append(sum).append("\n\n");

                                for (DataSnapshot snapshotTime : snapshotUsers.getChildren()) {
                                    String time = snapshotTime.getKey();
                                    if (time == null || time.equals("sum")) continue;

                                    text.append(time);

                                    ArrayList<String> _nameList = snapshotTime.child("name").getValue(s);
                                    ArrayList<Double> _priceList = snapshotTime.child("price").getValue(d);
                                    ArrayList<Double> _amountList = snapshotTime.child("amount").getValue(d);

                                    ArrayList<ArrayList<String>> rowsList = new ArrayList<>();
                                    for (int i = 0; i < _nameList.size(); ++i) {
                                        ArrayList<String> row = new ArrayList<>();
                                        row.add(_nameList.get(i));
                                        row.add(String.valueOf(_priceList.get(i)));
                                        row.add(String.valueOf(_amountList.get(i)));
                                        rowsList.add(row);
                                    }
                                    text.append(tableGenerator.generateTable(headersList, rowsList)).append("\n\n");
                                }
                            }
                            StorageFunctions.store(activity, "Fakturalar", date, text.toString());
                        }
                        SharedClass.showSnackBar(activity, "Fakturalar yadda saxlanıldı");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }


    private void delete() {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.delete, (dialog, which) -> {
            if(radioButtonStatus[0]) deleteExpenses();
            if(radioButtonStatus[1]) deleteSells();
            if(radioButtonStatus[2]) deleteTextures();
            if(radioButtonStatus[3]) deleteBuys();
            if(radioButtonStatus[4]) deleteDebts();
            if(radioButtonStatus[5]) deleteReturn();
            if(radioButtonStatus[6]) deleteReturnToDepo();
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.show();
    }

    private void deleteExpenses() {
        _deleteExpenses("Percent");
        _deleteExpenses("Other");
    }

    private void _deleteExpenses(String childName) {
        dr.child("EXPENSES/" + childName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotUsers : snapshot.getChildren()) {
                            for (DataSnapshot snapshotDates : snapshotUsers.getChildren()) {
                                if (!SharedClass.checkDate(beginDate, endDate, snapshotDates.getKey()))
                                    continue;

                                snapshotDates.getRef().removeValue();
                            }
                        }
                        Toast.makeText(activity, "Xərclər silindi", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(activity, "Uğusruz əməliyyat", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteSells() {
        dr.child("SELLS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            if (!SharedClass.checkDate(beginDate, endDate, snapshotDates.getKey()))
                                continue;

                            snapshotDates.getRef().removeValue();
                        }
                        SharedClass.showSnackBar(activity, "Satışlar silindi");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void deleteTextures() {
        dr.child("TEXTURES")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            if (!SharedClass.checkDate(beginDate, endDate, snapshotDates.getKey()))
                                continue;

                            snapshotDates.getRef().removeValue();
                        }
                        Toast.makeText(activity, "Məlumat silindi", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(activity, "Uğusruz əməliyyat", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteBuys() {
        dr.child("BUYS")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            if (!SharedClass.checkDate(beginDate, endDate, snapshotDates.getKey()))
                                continue;

                            snapshotDates.getRef().removeValue();
                        }
                        SharedClass.showSnackBar(activity, "Alışlar silindi");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void deleteDebts() {
        _deleteDebts("FABRIC");
        _deleteDebts("MARKET");
    }

    private void _deleteDebts(String child) {
        dr.child("DEBTS/" + child)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotName : snapshot.getChildren())
                            for (DataSnapshot snapshotDates : snapshotName.getChildren()) {
                                String date = snapshotDates.getKey();
                                if (date == null || !date.contains("_") ||
                                        !SharedClass.checkDate(beginDate, endDate, date)) continue;

                                snapshotDates.getRef().removeValue();
                            }

                        SharedClass.showSnackBar(activity, "Borclar silindi");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void deleteReturn() {
        _deleteReturn("BUY");
        _deleteReturn("SELL");
    }

    private void _deleteReturn(String child) {
        dr.child("RETURN/" + child)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            if (!SharedClass.checkDate(beginDate, endDate, snapshotDates.getKey()))
                                continue;

                            snapshotDates.getRef().removeValue();
                        }
                        SharedClass.showSnackBar(activity, "Geri alımlar silindi");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }

    private void deleteReturnToDepo() {
        dr.child("RETURN_TO_DEPO")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshotDates : snapshot.getChildren()) {
                            if (!SharedClass.checkDate(beginDate, endDate, snapshotDates.getKey()))
                                continue;

                            snapshotDates.getRef().removeValue();
                        }
                        SharedClass.showSnackBar(activity, "İadələr silindi");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        SharedClass.showSnackBar(activity, "Uğusruz əməliyyat");
                    }
                });
    }
}