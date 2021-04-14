package texel.com.depoproject.Pages.SELLER.Activities.Basket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.SELLER.Activities.CreateSell.CreateSellActivity;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;
import texel.com.depoproject.SqlLiteDatabase.TableInfo;

public class SellerBasketProductsRecycler extends RecyclerView.Adapter<SellerBasketProductsRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, Product> hashMap;
    private final Activity activity;
    private final TextView textViewResult;

    public SellerBasketProductsRecycler(Activity activity,
                                        ArrayList<String> nameList,
                                        ArrayList<String> searchNameList,
                                        HashMap<String, Product> hashMap,
                                        TextView textViewResult) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
        this.textViewResult = textViewResult;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_product_basket, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        Product product = hashMap.get(name);
        if (product == null) return;

        holder.name.setText(product.name);
        holder.price.setText(String.valueOf(product.sellPrice));
        holder.amount.setText(String.valueOf(product.wantedAmount));
        holder.commonPrice.setText(String.format("%.2f", product.wantedAmount * product.sellPrice));

        holder.price.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setNegativeButton("Faiz", (dialog, which) -> changeValue(holder.price, holder.commonPrice, name, product.sellPrice, product.wantedAmount, CHANGE_VALUE_STATUS.PERCENT));
            builder.setPositiveButton("Yeni qiymət", (dialog, which) -> changeValue(holder.price, holder.commonPrice, name, product.sellPrice, product.wantedAmount, CHANGE_VALUE_STATUS.NEW_PRICE));
            builder.setNeutralButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });

        holder.amount.setOnClickListener(v -> changeValue(holder.amount, holder.commonPrice, name, product.sellPrice, product.wantedAmount, CHANGE_VALUE_STATUS.NEW_AMOUNT));
    }

    private void changeValue(final TextView textViewFirst, final TextView textViewSecond, String name, double price, double amount, CHANGE_VALUE_STATUS status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        String hint = "";
        switch (status) {
            case NEW_AMOUNT:
                hint = "Yeni Miqdar";
                break;
            case NEW_PRICE:
                hint = "Yeni Qiymət";
                break;
            case PERCENT:
                hint = "Faiz";
                break;
        }

        input.setHint(hint);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                DatabaseHelper db = new DatabaseHelper(activity,
                        CreateSellActivity.isNewSell ?
                                DatabaseHelper.DATABASE_SELLS : DatabaseHelper.DATABASE_SELLER_RETURN);
                Double value = Double.parseDouble(input.getText().toString());

                Product product = hashMap.get(name);
                if (product == null) return;

                if (status == CHANGE_VALUE_STATUS.NEW_PRICE || status == CHANGE_VALUE_STATUS.PERCENT) {
                    if (status == CHANGE_VALUE_STATUS.PERCENT)
                        value = product.sellPrice - product.sellPrice * value / 100;

                    textViewFirst.setText(String.valueOf(value));
                    textViewSecond.setText(String.valueOf(value * amount));

                    hashMap.put(name, new Product(name, product.name, product.buyPrice, value, amount));

                    db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_SELL_PRICE, value);
                } else {
                    textViewFirst.setText(String.valueOf(value));
                    textViewSecond.setText(String.valueOf(price * value));

                    hashMap.put(name, new Product(name, product.name, product.buyPrice, price, value));

                    db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT, value);
                }

                double result = 0.0;
                for (Product p : db.getProductList()) result += p.sellPrice * p.wantedAmount;
                result = SharedClass.twoDigitDecimal(result);
                textViewResult.setText(String.valueOf(result));

                notifyDataSetChanged();
            } catch (Exception e) {
                SharedClass.showSnackBar(activity, "Yanlış dəyər daxiletdiniz!");
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    private enum CHANGE_VALUE_STATUS {
        NEW_PRICE,
        NEW_AMOUNT,
        PERCENT
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView price;
        private final TextView amount;
        private final TextView commonPrice;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            price = itemView.findViewById(R.id.cardTextViewProductPrice);
            amount = itemView.findViewById(R.id.cardTextViewProductAmount);
            commonPrice = itemView.findViewById(R.id.cardTextViewProductCommonPrice);
        }
    }
}