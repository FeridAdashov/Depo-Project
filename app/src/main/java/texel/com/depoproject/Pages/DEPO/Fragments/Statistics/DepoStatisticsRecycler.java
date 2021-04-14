package texel.com.depoproject.Pages.DEPO.Fragments.Statistics;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.TreeMap;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoStatisticsRecycler extends RecyclerView.Adapter<DepoStatisticsRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final TreeMap<String, Double> sumList;
    private final Activity activity;

    public DepoStatisticsRecycler(Activity activity,
                                  ArrayList<String> nameList,
                                  ArrayList<String> searchNameList,
                                  TreeMap<String, Double> sumList) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.sumList = sumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_statistics_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String key = nameList.get(position);

        holder.name.setText(key);
        holder.sum.setText(String.valueOf(SharedClass.twoDigitDecimal(sumList.get(key))));
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView sum;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewStatisticName);
            sum = itemView.findViewById(R.id.cardTextViewSum);
        }
    }
}