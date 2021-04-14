package texel.com.depoproject.Pages.DEPO.Fragments.Buys.History;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.Pages.DEPO.Fragments.Buys.ShowBuy.ShowBuyActivity;
import texel.com.depoproject.Pages.DEPO.Fragments.Return.Buys.ShowBuy.ShowReturnBuyActivity;
import texel.com.depoproject.R;

public class DepoRecyclerBuyHistory extends RecyclerView.Adapter<DepoRecyclerBuyHistory.MyViewHolder> {

    private final ArrayList<String> dateList;
    private final ArrayList<String> searchDateList;
    private final ArrayList<Double> buyValueList;
    private final ArrayList<Double> percentValueList;
    private final Activity activity;

    public DepoRecyclerBuyHistory(Activity activity,
                                  ArrayList<String> dateList,
                                  ArrayList<String> searchDateList,
                                  ArrayList<Double> buyValueList,
                                  ArrayList<Double> percentValueList) {
        this.activity = activity;
        this.dateList = dateList;
        this.searchDateList = searchDateList;
        this.buyValueList = buyValueList;
        this.percentValueList = percentValueList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_buy_history_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = dateList.indexOf(searchDateList.get(pos));

        String date = dateList.get(position);
        Double buyValue = buyValueList.get(position);
        Double percentValue = percentValueList.get(position);

        holder.date.setText(date);
        holder.commonValue.setText(String.format("%.2f", buyValue + percentValue));
        holder.buyValue.setText(String.format("%.2f", buyValue));
        holder.percentValue.setText(String.format("%.2f", percentValue));

        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(activity);
            b.setPositiveButton(R.string.ok, (dialog, which) -> {
                showTimesDialog("BUYS/" + date);
            });
            b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            b.setTitle("Bütün alışlara baxın");
            b.show();
        });
    }

    private void showTimesDialog(String child_name) {
        CustomProgressDialog dialog = new CustomProgressDialog(activity, activity.getString(R.string.data_loading));
        dialog.show();
        ArrayList<String> times = new ArrayList<>();
        DatabaseFunctions.getDatabases(activity).get(0).child(child_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotTime : snapshot.getChildren())
                    if (snapshotTime.getKey().contains("_"))
                        times.add(snapshotTime.getKey());

                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Alış seçin");
                builder.setItems(times.toArray(new String[0]), (dialog, which) -> {
                    Intent intent = new Intent(activity, ShowBuyActivity.class);
                    intent.putExtra("child_name", child_name + "/" + times.get(which));
                    activity.startActivity(intent);
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return searchDateList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView date;
        private final TextView commonValue;
        private final TextView buyValue;
        private final TextView percentValue;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExpense);
            date = itemView.findViewById(R.id.cardTextViewDate);
            commonValue = itemView.findViewById(R.id.cardTextViewCommonValue);
            buyValue = itemView.findViewById(R.id.cardTextViewBuyValue);
            percentValue = itemView.findViewById(R.id.cardTextViewPercentValue);
        }
    }
}