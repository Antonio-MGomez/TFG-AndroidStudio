package medac.lynca.vista;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_victor);

        // Vinculación
        rvDates = findViewById(R.id.rvDates);
        rvHours = findViewById(R.id.rvHours);
        tvPriceMain = findViewById(R.id.tvPriceMain);

        // Acción del botón calendario
        findViewById(R.id.btnCalendarShortcut).setOnClickListener(v ->
                Toast.makeText(this, "Redirigiendo al calendario...", Toast.LENGTH_SHORT).show());

        setupDates();
        // Carga inicial con el multiplicador del primer día (Lunes = 1.0)
        updateHoursAndPrice(dateList.get(0).getPriceMultiplier());
    }

    private void setupDates() {
        dateList = new ArrayList<>();
        dateList.add(new DateModel("Lunes", 15, true, 1.0));
        dateList.add(new DateModel("Martes", 16, false, 1.0));
        dateList.add(new DateModel("Miérc", 17, false, 1.25)); // 25% más caro
        dateList.add(new DateModel("Jueves", 18, false, 1.0));

        dateAdapter = new DateAdapter(dateList, this);
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);
    }

    @Override
    public void onDateClick(int position) {
        // Cambiar selección visual de días
        for (int i = 0; i < dateList.size(); i++) dateList.get(i).setSelected(i == position);
        dateAdapter.notifyDataSetChanged();

        // Actualizar horas con los nuevos precios del día
        updateHoursAndPrice(dateList.get(position).getPriceMultiplier());
    }

    @Override
    public void onHourClick(int position) {
        // Cambiar selección visual de horas (Poner en azul)
        for (int i = 0; i < hourList.size(); i++) hourList.get(i).setSelected(i == position);
        hourAdapter.notifyDataSetChanged();

        // Actualizar el precio grande de la barra inferior
        String nuevoPrecio = hourList.get(position).getPrice();
        tvPriceMain.setText(nuevoPrecio.contains("€") ? nuevoPrecio : "€" + nuevoPrecio);
    }

    private void updateHoursAndPrice(double multiplier) {
        int precioBase = 48;
        int precioCalculado = (int) (precioBase * multiplier);

        // El precio inferior por defecto es el de la primera hora
        tvPriceMain.setText("€" + precioCalculado);

        hourList = new ArrayList<>();
        hourList.add(new TimeSlotModel("18:00", "€" + precioCalculado, false));
        hourList.add(new TimeSlotModel("19:00", "€" + precioCalculado, false));
        hourList.add(new TimeSlotModel("20:00", "€" + precioCalculado, false));
        hourList.add(new TimeSlotModel("21:00", "Reservada", true));
        hourList.add(new TimeSlotModel("22:00", "€" + (int)(55 * multiplier), false));

        hourAdapter = new HourAdapter(hourList, this);
        rvHours.setLayoutManager(new GridLayoutManager(this, 3));
        rvHours.setAdapter(hourAdapter);
        rvHours.setNestedScrollingEnabled(false);
    }
}