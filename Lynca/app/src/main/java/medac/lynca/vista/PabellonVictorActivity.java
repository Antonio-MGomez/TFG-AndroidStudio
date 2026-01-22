package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.DateModel;
import medac.lynca.modelo.ReservationModel;
import medac.lynca.modelo.ReservationRepository;
import medac.lynca.modelo.TimeSlotModel;

public class PabellonVictorActivity extends AppCompatActivity implements
        DateAdapter.OnDateClickListener, HourAdapter.OnHourClickListener {

    private RecyclerView rvDates, rvHours;
    private DateAdapter dateAdapter;
    private HourAdapter hourAdapter;
    private TextView tvPriceMain;
    private List<DateModel> dateList;
    private List<TimeSlotModel> hourList;
    private ViewPager2 viewPagerHeader;
    private String selectedDayLabel = "Lunes 15";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_victor);

        rvDates = findViewById(R.id.rvDates);
        rvHours = findViewById(R.id.rvHours);
        tvPriceMain = findViewById(R.id.tvPriceMain);
        viewPagerHeader = findViewById(R.id.viewPagerHeader);
        Button btnReservar = findViewById(R.id.btnReservar);

        setupImageSlider();
        setupDates();

        // --- ENLACE CON EL CALENDARIO DE TU AMIGO ---
        findViewById(R.id.btnCalendarShortcut).setOnClickListener(v -> {
            // Usamos un Intent para viajar de esta pantalla a CalendarioActivity
            Intent intent = new Intent(PabellonVictorActivity.this, CalendarioActivity.class);
            startActivity(intent);
        });

        btnReservar.setOnClickListener(v -> {
            String hora = "";
            for (TimeSlotModel slot : hourList) {
                if (slot.isSelected()) { hora = slot.getTime(); break; }
            }

            if (hora.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona una hora", Toast.LENGTH_SHORT).show();
            } else {
                ReservationModel nueva = new ReservationModel(
                        "Pabellón número cinco",
                        selectedDayLabel + ", " + hora,
                        "pista_baloncesto_1",
                        "Confirmada"
                );
                ReservationRepository.getInstance().addReservation(nueva);
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });

        updateHoursAndPrice(15, 1.0);
    }

    private void setupImageSlider() {
        List<Integer> imgs = new ArrayList<>();
        imgs.add(R.drawable.pista_baloncesto_1);
        imgs.add(R.drawable.pista_baloncesto_2);
        viewPagerHeader.setAdapter(new ImageSliderAdapter(imgs));
    }

    private void setupDates() {
        dateList = new ArrayList<>();
        dateList.add(new DateModel("Lunes", 15, true, 1.0));
        dateList.add(new DateModel("Martes", 16, false, 1.0));
        dateList.add(new DateModel("Miérc", 17, false, 1.2));
        dateList.add(new DateModel("Jueves", 18, false, 1.0));
        dateList.add(new DateModel("Viernes", 19, false, 1.3));
        dateList.add(new DateModel("Sábado", 20, false, 1.5));
        dateList.add(new DateModel("Domingo", 21, false, 1.5));

        dateAdapter = new DateAdapter(dateList, this);
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);
    }

    @Override
    public void onDateClick(int pos) {
        for(int i=0; i<dateList.size(); i++) dateList.get(i).setSelected(i==pos);
        dateAdapter.notifyDataSetChanged();
        selectedDayLabel = dateList.get(pos).getDayName() + " " + dateList.get(pos).getDayNumber();
        updateHoursAndPrice(dateList.get(pos).getDayNumber(), dateList.get(pos).getPriceMultiplier());
    }

    @Override
    public void onHourClick(int pos) {
        if(!hourList.get(pos).isReserved()){
            for(int i=0; i<hourList.size(); i++) hourList.get(i).setSelected(i==pos);
            hourAdapter.notifyDataSetChanged();
            tvPriceMain.setText(hourList.get(pos).getPrice());
        } else {
            Toast.makeText(this, "Esta hora no está disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHoursAndPrice(int day, double mult) {
        int base = (int)(48 * mult);
        tvPriceMain.setText("€" + base);
        hourList = new ArrayList<>();

        boolean esFinDeSemana = (day == 20 || day == 21);
        String[] horas = {"09:00", "10:00", "11:00", "12:00", "13:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};

        for (String h : horas) {
            boolean reservada = esFinDeSemana ||
                    (day == 15 && h.equals("21:00")) ||
                    (day == 16 && h.equals("18:00")) ||
                    (day == 17 && h.equals("19:00"));
            hourList.add(new TimeSlotModel(h, "€" + base, reservada));
        }

        hourAdapter = new HourAdapter(hourList, this);
        rvHours.setLayoutManager(new GridLayoutManager(this, 3));
        rvHours.setAdapter(hourAdapter);
        rvHours.setNestedScrollingEnabled(false);
    }
}