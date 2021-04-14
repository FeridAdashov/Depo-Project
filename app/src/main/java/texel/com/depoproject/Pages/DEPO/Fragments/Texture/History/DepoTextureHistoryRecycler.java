package texel.com.depoproject.Pages.DEPO.Fragments.Texture.History;

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

public class DepoTextureHistoryRecycler extends RecyclerView.Adapter<DepoTextureHistoryRecycler.MyViewHolder> {

    private final ArrayList<String> dateList;
    private final ArrayList<Double> sumList;
    private final CustomProgressDialog progressDialog;
    private final Activity activity;

    public DepoTextureHistoryRecycler(Activity activity,
                                      ArrayList<String> dateList,
                                      ArrayList<Double> sumList) {
        this.activity = activity;
        this.dateList = dateList;
        this.sumList = sumList;
        progressDialog = new CustomProgressDialog(activity, activity.getString(R.string.data_loading));
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_texture_history_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String date = dateList.get(position);
        Double sum = sumList.get(position);

        holder.date.setText(date);
        holder.sum.setText(String.valueOf(sum));

        holder.sells.setOnClickListener(v -> {
            progressDialog.show();
            ArrayList<String> times = new ArrayList<>();
            DatabaseFunctions.getDatabases(activity).get(0).child("TEXTURES/" + date + "/" + TextureHistoryActivity.texture_child_name).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot timesSnapshot : snapshot.getChildren()) {
                                String time = timesSnapshot.getKey();
                                if (time == null || time.equals("sum") || time.equals("netIncome") || time.equals("percentSum"))
                                    continue;

                                times.add(timesSnapshot.getKey());
                            }

                            progressDialog.dismiss();
                            showTimesDialog("TEXTURES/" + date + "/" + TextureHistoryActivity.texture_child_name, times);
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
            intent.putExtra("is_texture", true);
            activity.startActivity(intent);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView sells;
        private final TextView sum;

        MyViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.cardTextViewTextureDate);
            sells = itemView.findViewById(R.id.cardTextViewReturns);
            sum = itemView.findViewById(R.id.cardTextViewSum);
        }
    }
}