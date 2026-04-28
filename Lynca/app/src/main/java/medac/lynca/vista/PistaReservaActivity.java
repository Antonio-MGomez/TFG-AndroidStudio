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
    private int    precioBase  = 10;
    private int    precioFinal = 10;

    private String fechaSeleccionada  = "";
    private String horaInicioSelected = "";
    private String horaFinSelected    = "";

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

        precioFinal = precioBase;

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
                                precioBase  = pista.getInt("precio_hora");
                                precioFinal = precioBase;
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
                    updateHoursAndPrice(
                            dateList.get(0).getDayNumber(), 1.0);
                });
            } catch (Exception e) {
                runOnUiThread(() -> updateHoursAndPrice(
                        dateList.get(0).getDayNumber(), 1.0));
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    //  Ir a pantalla de pago
    // ════════════════════════════════════════════════════════════
    private void intentarReservar() {
        horaInicioSelected = "";
        for (TimeSlotModel slot : hourList) {
            if (slot.isSelected()) {
                horaInicioSelected = slot.getTime();
                break;
            }
        }
        if (horaInicioSelected.isEmpty()) {
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

        horaFinSelected = calcularHoraFin(horaInicioSelected);

        // Ir a pantalla de pago
        Intent intent = new Intent(this, PagoActivity.class);
        intent.putExtra("pista_nombre", pistaNombre);
        intent.putExtra("tipo_deporte",
                getIntent().getStringExtra("tipo_deporte"));
        intent.putExtra("fecha",       fechaSeleccionada);
        intent.putExtra("hora_inicio", horaInicioSelected);
        intent.putExtra("hora_fin",    horaFinSelected);
        intent.putExtra("precio",      String.valueOf(precioFinal));
        intent.putExtra("imagen_url",  imagenUrl);
        intent.putExtra("imagen_res",  imagenRes);
        intent.putExtra("direccion",
                getIntent().getStringExtra("direccion"));
        intent.putExtra("pista_id",    pistaId);
        startActivity(intent);
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

    // ════════════════════════════════════════════════════════════
    //  Fechas dinámicas desde hoy
    // ════════════════════════════════════════════════════════════
    private void setupDates() {
        dateList = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        String[] dias = {"Dom","Lun","Mar","Mié","Jue","Vie","Sáb"};
        double[] multiplicadores = {1.5, 1.0, 1.0, 1.2, 1.0, 1.3, 1.5};

        for (int i = 0; i < 7; i++) {
            Calendar dia = (Calendar) cal.clone();
            dia.add(Calendar.DAY_OF_MONTH, i);
            int numeroDia    = dia.get(Calendar.DAY_OF_MONTH);
            int diaSemana    = dia.get(Calendar.DAY_OF_WEEK) - 1;
            String nombreDia = dias[diaSemana];
            double mult      = multiplicadores[diaSemana];
            dateList.add(new DateModel(nombreDia, numeroDia, i == 0, mult));
        }

        dateAdapter = new DateAdapter(dateList, this);
        rvDates.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);

        // Fecha inicial = hoy
        Calendar hoy = Calendar.getInstance();
        fechaSeleccionada = hoy.get(Calendar.YEAR) + "-"
                + String.format("%02d", hoy.get(Calendar.MONTH) + 1) + "-"
                + String.format("%02d", hoy.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onDateClick(int pos) {
        for (int i = 0; i < dateList.size(); i++)
            dateList.get(i).setSelected(i == pos);
        dateAdapter.notifyDataSetChanged();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, pos);
        fechaSeleccionada = cal.get(Calendar.YEAR) + "-"
                + String.format("%02d", cal.get(Calendar.MONTH) + 1) + "-"
                + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));

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
        precioFinal = base;
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

        for (String h : horas) {
            hourList.add(new TimeSlotModel(h, "€" + base, false));
        }

        hourAdapter = new HourAdapter(hourList, this);
        rvHours.setLayoutManager(new GridLayoutManager(this, 3));
        rvHours.setAdapter(hourAdapter);
        rvHours.setNestedScrollingEnabled(false);
    }
}