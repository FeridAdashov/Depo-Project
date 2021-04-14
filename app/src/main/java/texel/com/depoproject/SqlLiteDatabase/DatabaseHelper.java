package texel.com.depoproject.SqlLiteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_TEXTURES = "textures.db";
    public static final String DATABASE_SELLS = "sells.db";
    public static final String DATABASE_SELLER_RETURN = "seller_return.db";
    public static final String DATABASE_BUYS = "buys.db";
    public static final String DATABASE_BUYER_RETURN = "buyer_return.db";
    public static final String DATABASE_SELLER_PURE_ROTTEN = "seller_pure_rotten.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_BASKET_CREATE =
            "CREATE TABLE " + TableInfo.ProductEntry.TABLE_NAME + " (" +
                    TableInfo.ProductEntry.COLUMN_ID_NAME + " TEXT, " +
                    TableInfo.ProductEntry.COLUMN_NAME + " TEXT, " +
                    TableInfo.ProductEntry.COLUMN_BUY_PRICE + " INTEGER, " +
                    TableInfo.ProductEntry.COLUMN_SELL_PRICE + " INTEGER, " +
                    TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT + " INTEGER," +
                    TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT_ROTTEN + " INTEGER" +
                    ")";

    public DatabaseHelper(Context context, String tableName) {
        super(context, tableName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_BASKET_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TableInfo.ProductEntry.TABLE_NAME);

        onCreate(db);
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TableInfo.ProductEntry.TABLE_NAME, null, null);
    }

    public void addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableInfo.ProductEntry.COLUMN_ID_NAME, product.id_name);
        cv.put(TableInfo.ProductEntry.COLUMN_NAME, product.name);
        cv.put(TableInfo.ProductEntry.COLUMN_BUY_PRICE, product.buyPrice);
        cv.put(TableInfo.ProductEntry.COLUMN_SELL_PRICE, product.sellPrice);
        cv.put(TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT, product.wantedAmount);
        cv.put(TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT_ROTTEN, product.wantedAmountRotten);

        long result = db.insert(TableInfo.ProductEntry.TABLE_NAME, null, cv);

        if (result > -1)
            Log.i("DatabaseHelper", "Not başarıyla kaydedildi");
        else
            Log.i("DatabaseHelper", "Not kaydedilemedi");

        db.close();
    }

    public void changeColumnValue(String name_id, String column, Double value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, value);

        long result = db.update(TableInfo.ProductEntry.TABLE_NAME, cv,
                TableInfo.ProductEntry.COLUMN_ID_NAME + "=?", new String[]{name_id});

        if (result > -1)
            Log.i("DatabaseHelper", "Not başarıyla kaydedildi");
        else
            Log.i("DatabaseHelper", "Not kaydedilemedi");

        db.close();
    }

    public void deleteProduct(String name_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TableInfo.ProductEntry.TABLE_NAME, TableInfo.ProductEntry.COLUMN_ID_NAME + "=?", new String[]{name_id});
        db.close();
    }

    public boolean checkValueExist(String value) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " +
                TableInfo.ProductEntry.TABLE_NAME + " WHERE " +
                TableInfo.ProductEntry.COLUMN_ID_NAME + " =?";

        Cursor cursor = db.rawQuery(selectString, new String[]{value});

        boolean hasObject = false;
        if (cursor.moveToFirst()) hasObject = true;

        cursor.close();
        db.close();
        return hasObject;
    }

    public ArrayList<Product> getProductList() {
        ArrayList<Product> data = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                TableInfo.ProductEntry.COLUMN_ID_NAME,
                TableInfo.ProductEntry.COLUMN_NAME,
                TableInfo.ProductEntry.COLUMN_BUY_PRICE,
                TableInfo.ProductEntry.COLUMN_SELL_PRICE,
                TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT,
                TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT_ROTTEN,
        };

        Cursor c = db.query(TableInfo.ProductEntry.TABLE_NAME, projection, null, null, null, null, null);
        while (c.moveToNext()) {
            Product product = new Product(
                    c.getString(c.getColumnIndex(TableInfo.ProductEntry.COLUMN_ID_NAME)),
                    c.getString(c.getColumnIndex(TableInfo.ProductEntry.COLUMN_NAME)),
                    c.getDouble(c.getColumnIndex(TableInfo.ProductEntry.COLUMN_BUY_PRICE)),
                    c.getDouble(c.getColumnIndex(TableInfo.ProductEntry.COLUMN_SELL_PRICE)),
                    c.getDouble(c.getColumnIndex(TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT))
            );
            product.wantedAmountRotten = c.getDouble(c.getColumnIndex(TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT_ROTTEN));
            data.add(product);
        }

        c.close();
        db.close();

        return data;
    }


}