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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.DateModel;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;
import medac.lynca.modelo.SupabaseConfig;
import medac.lynca.modelo.TimeSlotModel;

public class PistaReservaActivity extends AppCompatActivity implements
        DateAdapter.OnDateClickListener, HourAdapter.OnHourClickListener {

    private RecyclerView        rvDates, rvHours;
    private DateAdapter         dateAdapter;
    private HourAdapter         hourAdapter;
    private TextView            tvPriceMain;
    private ViewPager2          viewPagerHeader;
    private Button              btnReservar;
    private List<DateModel>     dateList;
    private List<TimeSlotModel> hourList = new ArrayList<>();

    private String pistaId, pistaNombre, imagenUrl;
    private int    imagenRes;
    private int    precioBase = 10;

    private List<String> horasDisponibles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_victor);

        pistaId     = getIntent().getStringExtra("pista_id");
        pistaNombre = getIntent().getStringExtra("pista_nombre");
        imagenRes   = getIntent().getIntExtra("imagen_res",
                R.drawable.pista_baloncesto_1);
        imagenUrl   = getIntent().getStringExtra("imagen_url");

        try {
            precioBase = Integer.parseInt(
                    getIntent().getStringExtra("precio_hora")
                            .replace("€","").trim());
        } catch (Exception e) { precioBase = 10; }

        rvDates         = findViewById(R.id.rvDates);
        rvHours         = findViewById(R.id.rvHours);
        tvPriceMain     = findViewById(R.id.tvPriceMain);
        viewPagerHeader = findViewById(R.id.viewPagerHeader);
        btnReservar     = findViewById(R.id.btnReservar);

        cargarImagenes();
        setupDates();
        cargarHorarios();

        findViewById(R.id.btnCalendarShortcut).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarioActivity.class)));

        btnReservar.setOnClickListener(v -> intentarReservar());
        BottomNavHelper.setup(this, "search");
    }

    // ════════════════════════════════════════════════════════════
    //  Imágenes
    // ════════════════════════════════════════════════════════════
    private void cargarImagenes() {
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            List<String> urls = new ArrayList<>();
            urls.add(imagenUrl);
            viewPagerHeader.setAdapter(
                    new ImageSliderAdapterUrl(urls, imagenRes));
        } else {
            new Thread(() -> {
                try {
                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                    okhttp3.Request req = new okhttp3.Request.Builder()
                            .url(SupabaseConfig.REST_URL
                                    + "/imagenes_pista?pista_id=eq." + pistaId)
                            .addHeader("apikey", SupabaseConfig.ANON_KEY)
                            .get().build();

                    okhttp3.Response response = client.newCall(req).execute();
                    String body = response.body() != null
                            ? response.body().string() : "[]";

                    runOnUiThread(() -> {
                        try {
                            JSONArray arr = new JSONArray(body);
                            List<String> urls = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                String u = arr.getJSONObject(i)
                                        .optString("url_imagen", "");
                                if (!u.isEmpty()) urls.add(u);
                            }
                            if (!urls.isEmpty()) {
                                viewPagerHeader.setAdapter(
                                        new ImageSliderAdapterUrl(
                                                urls, imagenRes));
                            } else {
                                setImagenLocal();
                            }
                        } catch (Exception e) { setImagenLocal(); }
                    });
                } catch (Exception e) {
                    runOnUiThread(this::setImagenLocal);
                }
            }).start();
        }
    }

    private void setImagenLocal() {
        List<Integer> imgs = new ArrayList<>();
        imgs.add(imagenRes);
        viewPagerHeader.setAdapter(new ImageSliderAdapter(imgs));
    }

    // ════════════════════════════════════════════════════════════
    //  Horarios desde Supabase
    // ════════════════════════════════════════════════════════════
    private void cargarHorarios() {
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(SupabaseConfig.REST_URL
                                + "/pistas?id=eq." + pistaId
                                + "&select=horario,precio_hora")
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .get().build();

                okhttp3.Response response = client.newCall(req).execute();
                String body = response.body() != null
                        ? response.body().string() : "[]";

                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() > 0) {
                            JSONObject pista = arr.getJSONObject(0);

                            if (pista.has("precio_hora")
                                    && !pista.isNull("precio_hora")) {
                                precioBase = pista.getInt("precio_hora");
                            }

                            if (pista.has("horario")
                                    && !pista.isNull("horario")) {
                                Object horarioObj = pista.get("horario");
                                horasDisponibles.clear();

                                if (horarioObj instanceof JSONArray) {
                                    JSONArray slots = (JSONArray) horarioObj;
                                    for (int i = 0; i < slots.length(); i++) {
                                        Object slot = slots.get(i);
                                        if (slot instanceof JSONObject) {
                                            JSONObject s = (JSONObject) slot;
                                            String estado = s.optString(
                                                    "estado","Libre");
                                            String inicio = s.optString(
                                                    "inicio","");
                                            if (!inicio.isEmpty()
                                                    && estado.equalsIgnoreCase(
                                                    "Libre")) {
                                                horasDisponibles.add(inicio);
                                            }
                                        } else {
                                            horasDisponibles.add(
                                                    slots.getString(i));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { /* usar horas por defecto */ }
                    updateHoursAndPrice(15, 1.0);
                });
            } catch (Exception e) {
                runOnUiThread(() -> updateHoursAndPrice(15, 1.0));
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    //  Reservar
    // ════════════════════════════════════════════════════════════
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
            Toast.makeText(this, "Debes iniciar sesión primero",
                    Toast.LENGTH_SHORT).show();
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
            reserva.put("pista_id",       pistaId);
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
                            Toast.makeText(PistaReservaActivity.this,
                                    "✅ ¡Reserva confirmada!",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PistaReservaActivity.this,
                                    ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                        @Override
                        public void onError(String error) {
                            btnReservar.setEnabled(true);
                            btnReservar.setText(getString(R.string.reservar));
                            Toast.makeText(PistaReservaActivity.this,
                                    "Error al reservar: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            btnReservar.setEnabled(true);
            btnReservar.setText(getString(R.string.reservar));
            Toast.makeText(this, "Error inesperado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String calcularHoraFin(String horaInicio) {
        try {
            String[] parts = horaInicio.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int totalMin = h * 60 + m + 90;
            return String.format("%02d:%02d", totalMin / 60, totalMin % 60);
        } catch (Exception e) { return "00:00"; }
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
        int base = (int) (precioBase * mult);
        tvPriceMain.setText("€" + base);
        hourList = new ArrayList<>();

        String[] horas;
        if (!horasDisponibles.isEmpty()) {
            horas = horasDisponibles.toArray(new String[0]);
        } else {
            horas = new String[]{
                    "09:00","10:00","11:00","12:00","13:00",
                    "16:00","17:00","18:00","19:00","20:00","21:00","22:00"
            };
        }

        boolean esFinDeSemana = (day == 20 || day == 21);
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