package texel.com.depoproject.Pages.DEPO.Fragments.Products;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.DataClasses.ProductInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoRecyclerProducts extends RecyclerView.Adapter<DepoRecyclerProducts.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, ProductInfo> hashMap;
    private final Activity activity;

    public DepoRecyclerProducts(Activity activity,
                                ArrayList<String> nameList,
                                ArrayList<String> searchNameList,
                                HashMap<String, ProductInfo> hashMap) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_products_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        ProductInfo info = hashMap.get(name);

        if (info == null) return;

        holder.name.setText(info.name);
        holder.buyPrice.setText(String.valueOf(info.buyPrice));
        holder.sellPrice.setText(String.valueOf(info.sellPrice));

        holder.buttonEdit.setOnClickListener(v -> changeProductInfo(position));
        holder.buttonDelete.setOnClickListener(v -> deleteProduct(name));

        if (info.active)
            holder.cardViewLine.setBackgroundColor(activity.getResources().getColor(R.color.primary_light));
        else
            holder.cardViewLine.setBackgroundColor(activity.getResources().getColor(R.color.black));
    }

    private void changeProductInfo(int position) {
        String name = nameList.get(position);

        ProductInfo info = hashMap.get(name);
        if (info == null) return;

        final View view = activity.getLayoutInflater().inflate(R.layout.new_product_dialog_view, null);
        final EditText nameEditText = view.findViewById(R.id.cardTextViewProductName);
        final EditText buyPriceEditText = view.findViewById(R.id.cardTextViewProductBuyPrice);
        final EditText sellPriceEditText = view.findViewById(R.id.cardTextViewProductPrice);
        final SwitchMaterial switchActiveness = view.findViewById(R.id.switchActiveness);

        nameEditText.setText(info.name);
        buyPriceEditText.setText(String.valueOf(info.buyPrice));
        sellPriceEditText.setText(String.valueOf(info.sellPrice));
        switchActiveness.setChecked(info.active);

        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.save, (dialog, which) -> {
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setView(view);

        final AlertDialog dialog = b.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                String _name = nameEditText.getText().toString().trim();
                Double _buyPrice = Double.parseDouble(buyPriceEditText.getText().toString().trim());
                Double _sellPrice = Double.parseDouble(sellPriceEditText.getText().toString().trim());
                boolean _active = switchActiveness.isChecked();

                saveProduct(name, new ProductInfo(_name, _buyPrice, _sellPrice, _active));
                dialog.dismiss();
            } catch (Exception e) {
                SharedClass.showSnackBar(activity, "Yanlis Deyer Daxil Etmisiniz!!!");
                Log.d("AAAAAA", e.toString());
            }
        });
    }

    private void saveProduct(String name, ProductInfo productInfo) {
        if (!nameList.contains(name)) {
            nameList.add(name);
            searchNameList.add(name);
        }
        hashMap.put(name, productInfo);
        notifyDataSetChanged();

        DatabaseFunctions.getDatabases(activity).get(0).child("PRODUCTS").child(name).setValue(productInfo);
    }

    private void deleteProduct(String name) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.delete, (dialog, which) -> {
            hashMap.remove(name);
            nameList.remove(name);
            searchNameList.remove(name);
            notifyDataSetChanged();

            DatabaseFunctions.getDatabases(activity).get(0).child("PRODUCTS").child(name).removeValue();
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setTitle(Objects.requireNonNull(hashMap.get(name)).name);
        b.setMessage("Bu Məhsul Silinsinmi?");
        b.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView buyPrice;
        private final TextView sellPrice;
        private final ImageButton buttonEdit, buttonDelete;
        private final View cardViewLine;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            buyPrice = itemView.findViewById(R.id.cardTextViewProductBuyPrice);
            sellPrice = itemView.findViewById(R.id.cardTextViewProductPrice);
            buttonEdit = itemView.findViewById(R.id.cardButtonEditProduct);
            buttonDelete = itemView.findViewById(R.id.cardButtonDeleteProduct);
            cardViewLine = itemView.findViewById(R.id.cardViewLine);
        }
    }
}