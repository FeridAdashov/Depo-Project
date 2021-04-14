package texel.com.depoproject.Printer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.xml.sax.XMLReader;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class PrinterActivity extends AppCompatActivity {

    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    private OutputStream outputStream;

    private TextView lblPrinterName;
    private EditText textBox;

    private Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        activity = this;
        String message_1 = getIntent().getStringExtra("message_1");
        String message_2 = getIntent().getStringExtra("message_2");

        message_2 = message_1 + "    "
                + CustomDateTime.getDate(new Date()).replace("_", "/")
                + "  "
                + CustomDateTime.getTime(new Date()).replace("_", ":")
                + "\n\n\n"
                + message_2;

        // Create object of controls
        Button btnConnect = findViewById(R.id.btnPair);
        Button btnDisconnect = findViewById(R.id.btnUnpair);
        TextView btnPrint = findViewById(R.id.btnPrint);

        textBox = findViewById(R.id.txtText);
        textBox.setText(message_2);

        lblPrinterName = findViewById(R.id.lblPrinterName);

        btnConnect.setOnClickListener(view -> {
            try {
                findBluetoothDevice();
//                openBluetoothPrinter();
            } catch (Exception ex) {
                ex.printStackTrace();
//                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        });
        btnDisconnect.setOnClickListener(view -> {
            try {
                disconnectBT();
            } catch (Exception ex) {
                ex.printStackTrace();
//                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        });
        btnPrint.setOnClickListener(view -> {
            try {
                printData();
            } catch (Exception ex) {
                ex.printStackTrace();
//                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void findBluetoothDevice() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(activity, "Bluetooth dəstəklənmir", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 0);
                return;
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if (pairedDevice.size() > 0) {
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_singlechoice);
                final ArrayList<BluetoothDevice> devices = new ArrayList<>();
                for (BluetoothDevice pairedDev : pairedDevice) {
                    arrayAdapter.add(pairedDev.getName());
                    devices.add(pairedDev);
                }
                showDevicesDialog(arrayAdapter, devices);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDevicesDialog(final ArrayAdapter<String> arrayAdapter, final ArrayList<BluetoothDevice> devices) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
        builderSingle.setIcon(R.drawable.ic_print_white);
        builderSingle.setTitle("Qurğunu seçin:");

        builderSingle.setNegativeButton("Ləğv et", (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            bluetoothDevice = devices.get(which);
            lblPrinterName.setText("Qurğu seçildi");
            openBluetoothPrinter();
        });
        builderSingle.show();
    }

    // Open Bluetooth Printer
    void openBluetoothPrinter() {
        try {
            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (Exception ex) {
//            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Printing Text to Bluetooth Printer //
    void printData() {
        try {
            String msg = textBox.getText().toString();
            msg = SharedClass.convertAzToEn(msg) + "\n\n\n";

            outputStream.write(msg.getBytes());
            lblPrinterName.setText("Çap olunur...");
        } catch (Exception ex) {
            ex.printStackTrace();
//            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Disconnect Printer //
    void disconnectBT() {
        try {
            outputStream.close();
            bluetoothSocket.close();
            lblPrinterName.setText("Printer Ayrıldı.");
        } catch (Exception ex) {
            ex.printStackTrace();
//            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) findBluetoothDevice();
        else SharedClass.showSnackBar(activity, "Bluetooth açılarkən xəta baş verdi");
    }

    public static class MyTagHandler implements Html.TagHandler {

        boolean first = true;
        String parent = null;
        int index = 1;

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            if (tag.equals("ul")) {
                parent = "ul";
            } else if (tag.equals("ol")) {
                parent = "ol";
            }

            if (tag.equals("li")) {
                if (parent.equals("ul")) {
                    if (first) {
                        output.append("\n\t•");
                        first = false;
                    } else {
                        first = true;
                    }
                } else {
                    if (first) {
                        output.append("\n\t" + index + ". ");
                        first = false;
                        index++;
                    } else {
                        first = true;
                    }
                }
            }
        }
    }
}