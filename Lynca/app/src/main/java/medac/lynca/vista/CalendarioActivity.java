package medac.lynca.vista;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import medac.lynca.R;

public class CalendarioActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva);

        calendarView = findViewById(R.id.calendarView);
        tvInfo = findViewById(R.id.tvInfo);

        // Listener para detectar cambios de fecha
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvInfo.setText("HORARIOS PARA " + date);
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Toast.makeText(this, "Procesando reserva...", Toast.LENGTH_SHORT).show();
            // Aquí conectarías con el Backend (Spring Boot)
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}