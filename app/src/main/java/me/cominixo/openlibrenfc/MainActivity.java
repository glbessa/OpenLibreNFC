package me.cominixo.openlibrenfc;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static androidx.core.content.ContextCompat.getSystemService;
import static me.cominixo.openlibrenfc.LibreNfcUtils.bytesToHexStr;
import static me.cominixo.openlibrenfc.LibreNfcUtils.hexStrToBytes;
import static me.cominixo.openlibrenfc.LibreNfcUtils.sendCmd;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.openlibrenfc.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    enum SelectedAction {
        SCAN, RESET_AGE, ACTIVATE, LOAD_DUMP
    }

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;

    private SelectedAction selectedAction = SelectedAction.SCAN;

    private TextView idView;
    private TextView uidView;
    private TextView typeView;
    private TextView ageView;
    private TextView tempView;
    private TextView regionView;
    private TextView statusView;

    private TextView selectedActionView;

    private final OpenLibre libre = new OpenLibre();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );
        this.intentFilters = new IntentFilter[] {
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        };
        try {
            this.intentFilters[1].addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException(e);
        }

        idView = findViewById(R.id.libreid);
        uidView = findViewById(R.id.uid);
        typeView = findViewById(R.id.type);
        ageView = findViewById(R.id.age);
        tempView = findViewById(R.id.temp);
        regionView = findViewById(R.id.region);
        statusView = findViewById(R.id.status);
        selectedActionView = findViewById(R.id.selected_action);

        selectedActionView.setText(getString(R.string.selected_action, getString(R.string.scan)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.nfcAdapter.enableForegroundDispatch(this, this.pendingIntent, this.intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
        if (nfcTag == null)
            return;

        handleTag(nfcTag);
    }

    private void handleTag(Tag nfcTag) {
        NfcV handler = NfcV.get(nfcTag);
        try {
            this.libre.update(handler);
        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            this.showTagInfo();
        }
    }

    public void showTagInfo() {
        idView.setText(getString(R.string.libreid, this.libre.id));
        uidView.setText(getString(R.string.uid, this.libre.uid));
        typeView.setText(getString(R.string.type, this.libre.type));
        ageView.setText(getString(R.string.age, this.libre.age));
        tempView.setText(getString(R.string.temp, this.libre.temperature));
        regionView.setText(getString(R.string.region, this.libre.region));
        statusView.setText(getString(R.string.status, this.libre.status));
    }

    public void onScanClick(View view) {
        selectedAction = SelectedAction.SCAN;
        selectedActionView.setText(getString(R.string.selected_action, getString(R.string.scan)));
    }

    public void onResetAgeClick(View view) {
        selectedAction = SelectedAction.RESET_AGE;
        selectedActionView.setText(getString(R.string.selected_action, getString(R.string.reset_age)));
    }

    public void onActivateClick(View view) {
        selectedAction = SelectedAction.ACTIVATE;
        selectedActionView.setText(getString(R.string.selected_action, getString(R.string.start)));
    }

    public void dumpMemory(View view) {
        /*
        File file = getFile();

        FileOutputStream os;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            os = new FileOutputStream(file);
            os.write(bytesToHexStr(memory).getBytes());
            os.close();

            Toast.makeText(this, "Memory dumped to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {

            Toast.makeText(this, "Couldn't dump memory", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        */
    }

    public void loadMemory(View view) {

        new AlertDialog.Builder(this)
                .setTitle("Load memory")
                .setMessage("This will overwrite the current memory on the sensor with the most recent memory dump. If you edited the memory dump, make sure the checksums are correct.")
                .setPositiveButton(android.R.string.ok, null)
                .show();

        selectedAction = SelectedAction.LOAD_DUMP;
        selectedActionView.setText(getString(R.string.selected_action, getString(R.string.load_memory)));
    }

    private File getFile() {
        File externalCacheDir = ContextCompat.getExternalCacheDirs(this)[0];

        File dir = new File(externalCacheDir, "openlibrenfc");
        dir.mkdirs();

        File file = new File(dir, "memory_dump.txt");

        return file;

    }
}
