package texel.com.depoproject.Pages.DEPO.Fragments.Sells;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.Pages.DEPO.Activities.ShowSell.ShowSellActivity;
import texel.com.depoproject.R;

public class DepoSellsBySellerRecycler extends RecyclerView.Adapter<DepoSellsBySellerRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final ArrayList<SellBySellerModel> sellList;
    private final CustomProgressDialog progressDialog;
    private final Activity activity;

    public DepoSellsBySellerRecycler(Activity activity,
                                     ArrayList<String> nameList,
                                     ArrayList<String> searchNameList,
                                     ArrayList<SellBySellerModel> sellList) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.sellList = sellList;
        progressDialog = new CustomProgressDialog(activity, activity.getString(R.string.data_loading));
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_sells_by_seller_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        SellBySellerModel sell = sellList.get(position);

        holder.name.setText(nameList.get(position));
        holder.sellSum.setText(String.valueOf(sell.sellSum));
        holder.netIncome.setText(String.valueOf(sell.netIncome));
        holder.percentSum.setText(String.valueOf(sell.percentSum));

        holder.sells.setOnClickListener(v -> {
            progressDialog.show();
            ArrayList<String> times = new ArrayList<>();
            DatabaseFunctions.getDatabases(activity).get(0).child("SELLS/" + sell.date + "/" + sell.userName).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshotTime : snapshot.getChildren()) {
                                String time = snapshotTime.getKey();

                                if (time == null || time.equals("sum") || time.equals("netIncome") || time.equals("percentSum"))
                                    continue;

                                times.add(time);
                            }

                            progressDialog.dismiss();
                            showTimesDialog("SELLS/" + sell.date + "/" + sell.userName, times);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                        }
                    });
        });
    }

    private void showTimesDialog(String child_name, ArrayList<String> times) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Satış seçin");

        builder.setItems(times.toArray(new String[0]), (dialog, which) -> {
            Intent intent = new Intent(activity, ShowSellActivity.class);
            intent.putExtra("child_name", child_name + "/" + times.get(which));
            activity.startActivity(intent);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView sells;
        private final TextView sellSum;
        private final TextView netIncome;
        private final TextView percentSum;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewReturnName);
            sells = itemView.findViewById(R.id.cardTextViewReturns);
            sellSum = itemView.findViewById(R.id.cardTextViewPureSum);
            netIncome = itemView.findViewById(R.id.cardTextViewNetIncome);
            percentSum = itemView.findViewById(R.id.cardTextViewRottenSum);
        }
    }
}