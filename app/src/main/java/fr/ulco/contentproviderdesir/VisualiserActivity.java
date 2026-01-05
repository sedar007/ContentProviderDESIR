package fr.ulco.contentproviderdesir;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VisualiserActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_CALENDAR = 100;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> eventTitles;
    private boolean isSorted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visualiser);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lv = findViewById(R.id.listAgenda);


        eventTitles = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventTitles);
        lv.setAdapter(adapter);

        loadDatas();
        switchHandler();

    }


    public void onClickBackButton(View view) {
        finish();
    }

    // Permet de charger les données en fonction de la permission
    private void loadDatas(){
        if (checkPermission()) {
            loadCalendarEvents();
        } else {
            // Demande la permission
            requestPermission();
        }
    }

    private void switchHandler() {
        isSorted = false;
        Switch switchSort = findViewById(R.id.switch1);
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isSorted = isChecked;
                    loadDatas();
            }
        });

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALENDAR},
                PERMISSION_REQUEST_READ_CALENDAR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CALENDAR) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCalendarEvents();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadCalendarEvents() {
        ContentResolver cr = getContentResolver();

        // On recupere les colonnes
        String[] projection = {
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };
        String sortOrder = CalendarContract.Events.TITLE + (isSorted ? " ASC" : " DESC" );


        Cursor cursor = cr.query(CalendarContract.Events.CONTENT_URI, projection, null, null, sortOrder);

        if (cursor != null) {
            eventTitles.clear(); // vider la liste pour eviter ddes doublons

            int titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE);
            int startIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART);
            int endIndex = cursor.getColumnIndex(CalendarContract.Events.DTEND);

            while (cursor.moveToNext()) {
                if (titleIndex != -1 && startIndex != -1 && endIndex != -1) {
                    String title = cursor.getString(titleIndex);

                    final String displayString = title + "\n" +
                            "Début : " + formatDate(cursor.getLong(startIndex)) + "\n" +
                            "Fin : " + formatDate(cursor.getLong(endIndex));

                    eventTitles.add(displayString);
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }
    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}