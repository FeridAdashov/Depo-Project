package texel.com.depoproject.ExternalStorage;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.SignActivities.SignIn;


public class StorageFunctions {

    public static void store(Activity activity, String parentName, String childName, String text) {
        String app_name = SignIn.app_name;
//        String app_name = "Pictures"; //For LD app opener in PC

        if (Environment.getExternalStorageState().equalsIgnoreCase("mounted"))//Check if Device Storage is present
        {
            try {
                File root = new File(Environment.getExternalStorageDirectory(), app_name);
                if (!root.exists()) {
                    root.mkdirs();
                }
                File parent = new File(root, parentName);
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                if (childName.contains(":")) childName = childName.replaceAll(":", "ː");

                File myTxt = new File(parent, childName + ".txt");
                FileWriter writer = new FileWriter(myTxt);
                writer.append(text);//Writing the text
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Log.d("AAAAA", e.toString());
            }
        } else SharedClass.showSnackBar(activity, "Yaddaş problemi");
    }
}
