package fr.ulco.contentproviderdesir;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class VisualiserActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_CALENDAR = 100;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> eventTitles;

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

        ListView lv = (ListView)findViewById(R.id.listAgenda);

        eventTitles = new ArrayList<>();



        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventTitles);
        lv.setAdapter(adapter);

        if (checkPermission()) {
            loadCalendarEvents();
        } else {
            // Demande la permission
            requestPermission();
        }
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
                Toast.makeText(this, "Permission refusée, impossible de lire l'agenda", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadCalendarEvents() {
        ContentResolver cr = getContentResolver();

        // On recupere les colonnes
        String[] projection = { CalendarContract.Events.TITLE };
        Cursor cursor = cr.query(CalendarContract.Events.CONTENT_URI, projection, null, null, null);

        if (cursor != null) {
            eventTitles.clear(); // vider la liste pour eviter ddes doublons

            int titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE);

            while (cursor.moveToNext()) {
                if (titleIndex != -1) {
                    String title = cursor.getString(titleIndex);
                    eventTitles.add(title);
                }
            }
            cursor.close();

            adapter.notifyDataSetChanged();
        }
    }
}