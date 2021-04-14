package texel.com.depoproject.HelperClasses;


import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseFunctions {

    private static final ArrayList<String> databaseIds = new ArrayList<>(Arrays.asList(
            "your database id"                          //Database 1
    ));
    private static final ArrayList<String> databaseApiKeys = new ArrayList<>(Arrays.asList(
            "your database key"                                 //Database 1
    ));
    private static final ArrayList<String> databaseURLs = new ArrayList<>(Arrays.asList(
            "your Database url"                                   //Database 1
    ));
    public static int database_count = 1;

    private static FirebaseApp getMyFirebaseApp(Context context, int index) {
        --index;

        FirebaseApp app;
        boolean b = false;

        for (FirebaseApp a : FirebaseApp.getApps(context)) {
            if (a.getName().equals(String.valueOf(index)))
                b = true;
        }
        if (!b) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(databaseIds.get(index)) // Required for Analytics.
                    .setApiKey(databaseApiKeys.get(index))    // Required for Auth.
                    .setDatabaseUrl(databaseURLs.get(index))  // Required for RTDB.
                    .build();
            app = FirebaseApp.initializeApp(context, options, String.valueOf(index));
        } else app = FirebaseApp.getInstance(String.valueOf(index));

        return app;
    }

    private static ArrayList<FirebaseApp> getAllFirebaseApps(Context context) {
        ArrayList<FirebaseApp> apps = new ArrayList<>();
        for (int i = 1; i <= databaseIds.size(); ++i)
            apps.add(getMyFirebaseApp(context, i));
        return apps;
    }

    public static ArrayList<DatabaseReference> getDatabases(Context context) {
        ArrayList<DatabaseReference> databases = new ArrayList<>();
        for (FirebaseApp app : DatabaseFunctions.getAllFirebaseApps(context))
            databases.add(FirebaseDatabase.getInstance(app).getReference());

        return databases;
    }
}
