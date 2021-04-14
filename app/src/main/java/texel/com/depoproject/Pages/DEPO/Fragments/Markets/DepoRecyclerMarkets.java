package texel.com.depoproject.Pages.DEPO.Fragments.Markets;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.DataClasses.MarketInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.R;

public class DepoRecyclerMarkets extends RecyclerView.Adapter<DepoRecyclerMarkets.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, MarketInfo> hashMap;
    private final Activity activity;

    public DepoRecyclerMarkets(Activity activity,
                               ArrayList<String> nameList,
                               ArrayList<String> searchNameList,
                               HashMap<String, MarketInfo> hashMap) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_markets_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        MarketInfo info = hashMap.get(name);

        if (info == null) return;

        holder.name.setText(info.name);
        holder.address.setText(info.address);
        holder.phone.setText(info.phone);

        holder.buttonEdit.setOnClickListener(v -> changeMarketInfo(position));
        holder.buttonDelete.setOnClickListener(v -> deleteMarket(name));

        if (info.active)
            holder.cardViewLine.setBackgroundColor(activity.getResources().getColor(R.color.primary_light));
        else
            holder.cardViewLine.setBackgroundColor(activity.getResources().getColor(R.color.black));
    }

    private void changeMarketInfo(int position) {
        String name = nameList.get(position);

        MarketInfo info = hashMap.get(name);
        if (info == null) return;

        final View view = activity.getLayoutInflater().inflate(R.layout.new_market_dialog_view, null);
        final TextView nameTextView = view.findViewById(R.id.cardTextViewMarketName);
        final EditText phoneEditText = view.findViewById(R.id.cardTextViewMarketPhone);
        final EditText addressEditText = view.findViewById(R.id.cardTextViewMarketAddress);
        final SwitchMaterial switchActiveness = view.findViewById(R.id.switchActiveness);

        nameTextView.setText(info.name);
        phoneEditText.setText(info.phone);
        addressEditText.setText(info.address);
        switchActiveness.setChecked(info.active);

        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.save, (dialog, which) -> {
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setView(view);

        final AlertDialog dialog = b.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String _phone = phoneEditText.getText().toString().trim();
            String _address = addressEditText.getText().toString().trim();
            boolean _active = switchActiveness.isChecked();

            saveMarket(name, new MarketInfo(info.name, _phone, _address, _active));
            dialog.dismiss();
        });
    }

    private void saveMarket(String name, MarketInfo marketInfo) {
        if (!nameList.contains(name)) {
            nameList.add(name);
            searchNameList.add(name);
        }

        hashMap.put(name, marketInfo);
        notifyDataSetChanged();

        DatabaseFunctions.getDatabases(activity).get(0).child("MARKETS").child(name).setValue(marketInfo);
    }

    private void deleteMarket(String name) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.delete, (dialog, which) -> {
            hashMap.remove(name);
            nameList.remove(name);
            searchNameList.remove(name);
            notifyDataSetChanged();

            DatabaseFunctions.getDatabases(activity).get(0).child("MARKETS").child(name).removeValue();
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setTitle(Objects.requireNonNull(hashMap.get(name)).name);
        b.setMessage("Bu Market Silinsinmi?");
        b.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView phone;
        private final TextView address;
        private final ImageButton buttonEdit, buttonDelete;
        private final View cardViewLine;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewMarketName);
            phone = itemView.findViewById(R.id.cardTextViewMarketPhone);
            address = itemView.findViewById(R.id.cardTextViewMarketAddress);
            buttonEdit = itemView.findViewById(R.id.cardButtonEditMarket);
            buttonDelete = itemView.findViewById(R.id.cardButtonDeleteMarket);
            cardViewLine = itemView.findViewById(R.id.cardViewLine);
        }
    }
}