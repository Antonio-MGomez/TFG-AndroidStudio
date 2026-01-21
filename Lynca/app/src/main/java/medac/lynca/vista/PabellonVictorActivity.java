package medac.lynca.vista;

import android.os.Bundle;
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
import medac.lynca.vista.model.DateModel;
import medac.lynca.vista.model.TimeSlotModel;

public class PabellonVictorActivity extends AppCompatActivity implements
        DateAdapter.OnDateClickListener, HourAdapter.OnHourClickListener {

    private RecyclerView rvDates, rvHours;
    private DateAdapter dateAdapter;
    private HourAdapter hourAdapter;
    private TextView tvPriceMain;
    private List<DateModel> dateList;
    private List<TimeSlotModel> hourList;
    private ViewPager2 viewPagerHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_victor);

        rvDates = findViewById(R.id.rvDates);
        rvHours = findViewById(R.id.rvHours);
        tvPriceMain = findViewById(R.id.tvPriceMain);
        viewPagerHeader = findViewById(R.id.viewPagerHeader);

        // 1. Configurar carrusel con las imágenes de baloncesto
        setupImageSlider();

        findViewById(R.id.btnCalendarShortcut).setOnClickListener(v ->
                Toast.makeText(this, "Redirigiendo al calendario...", Toast.LENGTH_SHORT).show());

        setupDates();

        // Carga inicial: Lunes (posición 0)
        updateHoursAndPrice(dateList.get(0).getDayNumber(), dateList.get(0).getPriceMultiplier());
    }

    private void setupImageSlider() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.pista_baloncesto_1);
        images.add(R.drawable.pista_baloncesto_2);

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPagerHeader.setAdapter(adapter);
    }

    private void setupDates() {
        dateList = new ArrayList<>();
        dateList.add(new DateModel("Lunes", 15, true, 1.0));
        dateList.add(new DateModel("Martes", 16, false, 1.0));
        dateList.add(new DateModel("Miérc", 17, false, 1.25));
        dateList.add(new DateModel("Jueves", 18, false, 1.0));

        dateAdapter = new DateAdapter(dateList, this);
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);
    }

    @Override
    public void onDateClick(int position) {
        for (int i = 0; i < dateList.size(); i++) dateList.get(i).setSelected(i == position);
        dateAdapter.notifyDataSetChanged();

        // Pasamos el número de día para variar las reservas
        updateHoursAndPrice(dateList.get(position).getDayNumber(), dateList.get(position).getPriceMultiplier());
    }

    @Override
    public void onHourClick(int position) {
        for (int i = 0; i < hourList.size(); i++) hourList.get(i).setSelected(i == position);
        hourAdapter.notifyDataSetChanged();

        String nuevoPrecio = hourList.get(position).getPrice();
        tvPriceMain.setText(nuevoPrecio.contains("€") ? nuevoPrecio : "€" + nuevoPrecio);
    }

    private void updateHoursAndPrice(int dayNumber, double multiplier) {
        int precioBase = 48;
        int precioCalculado = (int) (precioBase * multiplier);
        tvPriceMain.setText("€" + precioCalculado);

        hourList = new ArrayList<>();

        // Lógica para variar qué hora está reservada según el día (Lunes=15, Martes=16...)
        // Lunes: 21:00 Reservada
        // Martes: 18:00 Reservada
        // Miércoles: 19:00 Reservada
        // Jueves: 20:00 Reservada

        hourList.add(new TimeSlotModel("18:00", "€" + precioCalculado, dayNumber == 16));
        hourList.add(new TimeSlotModel("19:00", "€" + precioCalculado, dayNumber == 17));
        hourList.add(new TimeSlotModel("20:00", "€" + precioCalculado, dayNumber == 18));
        hourList.add(new TimeSlotModel("21:00", "€" + precioCalculado, dayNumber == 15));
        hourList.add(new TimeSlotModel("22:00", "€" + (int)(55 * multiplier), false));

        hourAdapter = new HourAdapter(hourList, this);
        rvHours.setLayoutManager(new GridLayoutManager(this, 3));
        rvHours.setAdapter(hourAdapter);
        rvHours.setNestedScrollingEnabled(false);
    }
}