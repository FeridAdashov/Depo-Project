package texel.com.depoproject.Pages.BUYER.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class RecyclerBuysProductsBuyer extends RecyclerView.Adapter<RecyclerBuysProductsBuyer.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> childNameList;
    private final ArrayList<String> searchChildNameList;
    private final ArrayList<Double> priceList;
    private final Activity activity;

    public RecyclerBuysProductsBuyer(Activity activity,
                                     ArrayList<String> nameList,
                                     ArrayList<String> childNameList,
                                     ArrayList<String> searchChildNameList,
                                     ArrayList<Double> priceList) {
        this.activity = activity;
        this.nameList = nameList;
        this.childNameList = childNameList;
        this.searchChildNameList = searchChildNameList;
        this.priceList = priceList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_products_for_texture_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = childNameList.indexOf(searchChildNameList.get(pos));

        String name = nameList.get(position);
        Double price = priceList.get(position);

        holder.name.setText(name);
        holder.price.setText(String.format("%.2f", price));

        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final EditText input = new EditText(activity);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint("Miqdar daxil edin");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                try {
                    double amount = SharedClass.twoDigitDecimal(Double.parseDouble(input.getText().toString()));
                    if (amount == 0) throw new Exception();

                    addToBasket(name, new Product(name, name, price, 0.0, amount));
                } catch (Exception e) {
                    SharedClass.showSnackBar(activity, "Yanlış dəyər daxiletdiniz!");
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void addToBasket(String name, Product product) {
        DatabaseHelper db = new DatabaseHelper(activity,
                BuyerProductsFragment.isNewBuy ? DatabaseHelper.DATABASE_BUYS : DatabaseHelper.DATABASE_BUYER_RETURN);
        if (db.checkValueExist(name))
            SharedClass.showSnackBar(activity, "Bu Məhsul Artıq Səbətdə Var!");
        else {
            db.addProduct(product);
            db.close();
        }
    }

    @Override
    public int getItemCount() {
        return searchChildNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView price;
        private final CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            price = itemView.findViewById(R.id.cardTextViewProductPrice);
            cardView = itemView.findViewById(R.id.cardViewTextureProduct);
        }
    }
}