package texel.com.depoproject.SqlLiteDatabase;

import android.provider.BaseColumns;

public class TableInfo {
    public static final class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "basket";

        public static final String COLUMN_ID_NAME = "id_name";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BUY_PRICE = "buy_price";
        public static final String COLUMN_SELL_PRICE = "sell_price";
        public static final String COLUMN_WANTED_AMOUNT = "wanted_amount";
        public static final String COLUMN_WANTED_AMOUNT_ROTTEN = "wanted_amount_rotten";
    }
}
