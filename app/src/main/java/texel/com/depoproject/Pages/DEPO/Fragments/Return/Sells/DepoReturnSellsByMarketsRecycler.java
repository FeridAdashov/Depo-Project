package texel.com.depoproject.Pages.DEPO.Fragments.Return.Sells;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.Pages.DEPO.Activities.ShowSell.ShowSellActivity;
import texel.com.depoproject.R;

public class DepoReturnSellsByMarketsRecycler extends RecyclerView.Adapter<DepoReturnSellsByMarketsRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, ReturnSellByMarketModel> sellList;
    private final Activity activity;

    public DepoReturnSellsByMarketsRecycler(Activity activity,
                                            ArrayList<String> nameList,
                                            ArrayList<String> searchNameList,
                                            HashMap<String, ReturnSellByMarketModel> sellList) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.sellList = sellList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_return_sells_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String key = nameList.get(position);
        ReturnSellByMarketModel sell = sellList.get(key);

        holder.name.setText(key);
        holder.sum.setText(String.format("%.2f", sell.sum));

        holder.sells.setOnClickListener(v -> showTimesDialog("RETURN/SELL/" + sell.date + "/" + sell.seller, sell.times));
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
        private final TextView sum;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewReturnName);
            sells = itemView.findViewById(R.id.cardTextViewReturns);
            sum = itemView.findViewById(R.id.cardTextViewSum);
        }
    }
}