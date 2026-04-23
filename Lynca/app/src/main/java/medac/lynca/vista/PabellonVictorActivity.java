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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.DateModel;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;
import medac.lynca.modelo.TimeSlotModel;

public class PabellonVictorActivity extends AppCompatActivity implements
        DateAdapter.OnDateClickListener, HourAdapter.OnHourClickListener {

    private RecyclerView        rvDates, rvHours;
    private DateAdapter         dateAdapter;
    private HourAdapter         hourAdapter;
    private TextView            tvPriceMain;
    private ViewPager2          viewPagerHeader;
    private Button              btnReservar;
    private List<DateModel>     dateList;
    private List<TimeSlotModel> hourList;

    private static final String PISTA_ID     = "09b0e24c-79db-482a-8cf2-2c33a3e1dddf";
    private static final String PISTA_NOMBRE = "Pabellón número cinco";
    private static final String PISTA_IMG    = "pista_baloncesto_1";
    private static final int    PRECIO_BASE  = 48;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_victor);

        rvDates         = findViewById(R.id.rvDates);
        rvHours         = findViewById(R.id.rvHours);
        tvPriceMain     = findViewById(R.id.tvPriceMain);
        viewPagerHeader = findViewById(R.id.viewPagerHeader);
        btnReservar     = findViewById(R.id.btnReservar);

        setupImageSlider();
        setupDates();
        updateHoursAndPrice(15, 1.0);

        findViewById(R.id.btnCalendarShortcut).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarioActivity.class)));

        btnReservar.setOnClickListener(v -> intentarReservar());
    }

    private void intentarReservar() {
        String horaInicio = "";
        for (TimeSlotModel slot : hourList) {
            if (slot.isSelected()) { horaInicio = slot.getTime(); break; }
        }
        if (horaInicio.isEmpty()) {
            Toast.makeText(this, "Selecciona una hora primero",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager session = SessionManager.getInstance(this);
        String perfilId = session.getPerfilId();
        if (perfilId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        Calendar cal = Calendar.getInstance();
        DateModel diaSeleccionado = null;
        for (DateModel d : dateList) {
            if (d.isSelected()) { diaSeleccionado = d; break; }
        }
        int numDia = diaSeleccionado != null
                ? diaSeleccionado.getDayNumber()
                : cal.get(Calendar.DAY_OF_MONTH);
        int mes  = cal.get(Calendar.MONTH) + 1;
        int anyo = cal.get(Calendar.YEAR);
        String fecha = anyo + "-"
                + String.format("%02d", mes) + "-"
                + String.format("%02d", numDia);

        String horaFin = calcularHoraFin(horaInicio);

        try {
            JSONObject reserva = new JSONObject();
            reserva.put("perfil_id",      perfilId);
            reserva.put("pista_id",       PISTA_ID);
            reserva.put("fecha_reserva",  fecha);
            reserva.put("hora_inicio",    horaInicio + ":00");
            reserva.put("hora_fin",       horaFin + ":00");
            reserva.put("estado_reserva", "Confirmada");

            btnReservar.setEnabled(false);
            btnReservar.setText("Reservando...");

            SupabaseClient.getInstance().insertReserva(reserva,
                    new SupabaseClient.Callback() {
                        @Override
                        public void onSuccess(String body) {
                            btnReservar.setEnabled(true);
                            btnReservar.setText(getString(R.string.reservar));
                            Toast.makeText(PabellonVictorActivity.this,
                                    "✅ ¡Reserva confirmada!",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PabellonVictorActivity.this,
                                    ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                        @Override
                        public void onError(String error) {
                            btnReservar.setEnabled(true);
                            btnReservar.setText(getString(R.string.reservar));
                            Toast.makeText(PabellonVictorActivity.this,
                                    "Error al reservar: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            btnReservar.setEnabled(true);
            btnReservar.setText(getString(R.string.reservar));
        }
    }

    private String calcularHoraFin(String horaInicio) {
        try {
            int h = Integer.parseInt(horaInicio.split(":")[0]);
            return String.format("%02d:00", (h + 1) % 24);
        } catch (Exception e) { return "00:00"; }
    }

    private void setupImageSlider() {
        List<Integer> imgs = new ArrayList<>();
        imgs.add(R.drawable.pista_baloncesto_1);
        imgs.add(R.drawable.pista_baloncesto_2);
        viewPagerHeader.setAdapter(new ImageSliderAdapter(imgs));
    }

    private void setupDates() {
        dateList = new ArrayList<>();
        dateList.add(new DateModel("Lunes",   15, true,  1.0));
        dateList.add(new DateModel("Martes",  16, false, 1.0));
        dateList.add(new DateModel("Miérc",   17, false, 1.2));
        dateList.add(new DateModel("Jueves",  18, false, 1.0));
        dateList.add(new DateModel("Viernes", 19, false, 1.3));
        dateList.add(new DateModel("Sábado",  20, false, 1.5));
        dateList.add(new DateModel("Domingo", 21, false, 1.5));

        dateAdapter = new DateAdapter(dateList, this);
        rvDates.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);
    }

    @Override
    public void onDateClick(int pos) {
        for (int i = 0; i < dateList.size(); i++)
            dateList.get(i).setSelected(i == pos);
        dateAdapter.notifyDataSetChanged();
        updateHoursAndPrice(
                dateList.get(pos).getDayNumber(),
                dateList.get(pos).getPriceMultiplier());
    }

    @Override
    public void onHourClick(int pos) {
        if (!hourList.get(pos).isReserved()) {
            for (int i = 0; i < hourList.size(); i++)
                hourList.get(i).setSelected(i == pos);
            hourAdapter.notifyDataSetChanged();
            tvPriceMain.setText(hourList.get(pos).getPrice());
        } else {
            Toast.makeText(this, "Esta hora no está disponible",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHoursAndPrice(int day, double mult) {
        int base = (int) (PRECIO_BASE * mult);
        tvPriceMain.setText("€" + base);
        hourList = new ArrayList<>();

        boolean esFinDeSemana = (day == 20 || day == 21);
        String[] horas = {"09:00","10:00","11:00","12:00","13:00",
                "16:00","17:00","18:00","19:00","20:00","21:00","22:00"};

        for (String h : horas) {
            boolean reservada = esFinDeSemana
                    || (day == 15 && h.equals("21:00"))
                    || (day == 16 && h.equals("18:00"))
                    || (day == 17 && h.equals("19:00"));
            hourList.add(new TimeSlotModel(h, "€" + base, reservada));
        }

        hourAdapter = new HourAdapter(hourList, this);
        rvHours.setLayoutManager(new GridLayoutManager(this, 3));
        rvHours.setAdapter(hourAdapter);
        rvHours.setNestedScrollingEnabled(false);
    }
}