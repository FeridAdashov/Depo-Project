package texel.com.depoproject.Pages.SELLER.Fragments.Markets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import texel.com.depoproject.DataClasses.MarketInfo;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.SELLER.Activities.CreateSell.CreateSellActivity;
import texel.com.depoproject.R;

public class SellerRecyclerMarkets extends RecyclerView.Adapter<SellerRecyclerMarkets.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, MarketInfo> hashMap;
    private final Activity activity;

    public SellerRecyclerMarkets(Activity activity,
                                 ArrayList<String> nameList,
                                 ArrayList<String> searchNameList,
                                 HashMap<String, MarketInfo> hashMap) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_markets_seller, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        MarketInfo info = hashMap.get(name);
        if (info == null) return;

        holder.name.setText(info.name);

        holder.debt.setText("Borc: " + (info.debt == null ? "..." : info.debt));
        if (info.debt == null)
            holder.debt.setTextColor(Color.parseColor("#000000"));
        else holder.debt.setTextColor(Color.parseColor(info.debt > 0 ? "#3DDC84" : "#FF5722"));

        holder.debt.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            EditText editText = new EditText(activity);
            editText.setTextColor(Color.parseColor("#FF5722"));
            editText.setHint("Silinəcək borcun miqdarı");
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(editText);
            builder.setPositiveButton("Ödə", (dialog, which) -> {
                try {
                    double value = SharedClass.twoDigitDecimal(Double.parseDouble(editText.getText().toString()));
                    DatabaseFunctions.getDatabases(activity).get(0).child("DEBTS/MARKET/" + name).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Double sum = snapshot.child("sum").getValue(Double.class);
                            if (sum != null && sum >= value) {
                                sum -= value;
                                sum = SharedClass.twoDigitDecimal(sum);

                                snapshot.getRef().child("sum").setValue(sum);
                                snapshot.getRef()
                                        .child(CustomDateTime.getDate(new Date()) + "/"
                                                + CustomDateTime.getTime(new Date()))
                                        .setValue(-value);
                                holder.debt.setText("Borc: " + sum);
                            } else
                                Toast.makeText(activity, "Yanlış dəyər!!!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(activity, "Yanlış dəyər!!!", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });


        holder.buttonInfo.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(activity);
            b.setPositiveButton("Zəng", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + info.phone));
                activity.startActivity(intent);
            });
            b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            b.setTitle(name);
            b.setMessage(info.address);
            b.show();
        });

        Intent intent = new Intent(activity, CreateSellActivity.class);
        intent.putExtra("market_id_name", name);
        intent.putExtra("market_name", info.name);
        holder.buttonReturn.setOnClickListener(v -> {
            intent.putExtra("isNewSell", false);
            activity.startActivity(intent);
        });
        holder.buttonSell.setOnClickListener(v -> {
            intent.putExtra("isNewSell", true);
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, debt;
        private final ImageButton buttonInfo;
        private final Button buttonReturn, buttonSell;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewMarketName);
            debt = itemView.findViewById(R.id.cardTextViewMarketDebt);
            buttonInfo = itemView.findViewById(R.id.cardButtonInfoMarket);
            buttonReturn = itemView.findViewById(R.id.cardButtonReturn);
            buttonSell = itemView.findViewById(R.id.cardButtonSell);
        }
    }
}