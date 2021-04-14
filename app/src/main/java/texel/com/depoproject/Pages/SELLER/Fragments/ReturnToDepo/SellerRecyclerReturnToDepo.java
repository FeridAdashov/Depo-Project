package texel.com.depoproject.Pages.SELLER.Fragments.ReturnToDepo;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.DataClasses.ReturnToDepoProductInfo;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class SellerRecyclerReturnToDepo extends RecyclerView.Adapter<SellerRecyclerReturnToDepo.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, ReturnToDepoProductInfo> hashMap;

    public SellerRecyclerReturnToDepo(Activity activity,
                                      ArrayList<String> nameList,
                                      ArrayList<String> searchNameList,
                                      HashMap<String, ReturnToDepoProductInfo> hashMap) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_return_to_depo_seller, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        ReturnToDepoProductInfo info = hashMap.get(name);

        if (info == null) return;

        holder.name.setText(info.name);
        holder.price.setText(String.valueOf(info.sellPrice));
        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            final EditText inputPure = new EditText(activity);
            inputPure.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputPure.setHint("Saf məhsul miqdarı");

            final EditText inputRotten = new EditText(activity);
            inputRotten.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputRotten.setHint("Çürük məhsul miqdarı");

            linearLayout.addView(inputPure);
            linearLayout.addView(inputRotten);

            builder.setView(linearLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                try {
                    String textPure = inputPure.getText().toString();
                    String textRotten = inputRotten.getText().toString();

                    if (TextUtils.isEmpty(textPure) && TextUtils.isEmpty(textRotten)) {
                        SharedClass.showSnackBar(activity, "Miqdar daxil etməlisiniz!!!");
                        return;
                    }
                    if (TextUtils.isEmpty(textPure)) textPure = "0";
                    if (TextUtils.isEmpty(textRotten)) textRotten = "0";

                    double amountPure = SharedClass.twoDigitDecimal(Double.parseDouble(textPure));
                    double amountRotten = SharedClass.twoDigitDecimal(Double.parseDouble(textRotten));

                    if (amountPure == 0.0 && amountRotten == 0.0) {
                        SharedClass.showSnackBar(activity, "Miqdar daxil etməlisiniz!!!");
                        return;
                    }

                    Product product = new Product(name, name, 0.0, info.sellPrice, amountPure);
                    product.wantedAmountRotten = amountRotten;

                    addToBasket(name, product);
                } catch (Exception e) {
                    SharedClass.showSnackBar(activity, "Yanlış dəyər daxiletdiniz!");
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void addToBasket(String name, Product product) {
        DatabaseHelper db = new DatabaseHelper(activity, DatabaseHelper.DATABASE_SELLER_PURE_ROTTEN);
        if (db.checkValueExist(name))
            SharedClass.showSnackBar(activity, "Bu Məhsul Artıq Səbətdə Var!");
        else {
            db.addProduct(product);
            db.close();
        }
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView price;
        private final CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewReturnToDepo);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            price = itemView.findViewById(R.id.cardTextViewProductPrice);
        }
    }
}