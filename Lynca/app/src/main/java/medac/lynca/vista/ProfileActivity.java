package medac.lynca.vista;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.ReservationModel;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView       rv;
    private ReservationAdapter adapter;
    private Button             btnReservadas, btnPasadas;

    private final List<ReservationModel> activeList = new ArrayList<>();
    private final List<ReservationModel> pastList   = new ArrayList<>();
    private boolean showingActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rv            = findViewById(R.id.rvReservations);
        btnReservadas = findViewById(R.id.tabReservadas);
        btnPasadas    = findViewById(R.id.tabPasadas);

        rv.setLayoutManager(new LinearLayoutManager(this));

        SessionManager session = SessionManager.getInstance(this);
        TextView tvName  = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        if (tvName  != null) tvName.setText(session.getNombre());
        if (tvEmail != null) tvEmail.setText(session.getEmail());

        btnReservadas.setOnClickListener(v -> mostrarLista(true));
        btnPasadas.setOnClickListener(v    -> mostrarLista(false));

        BottomNavHelper.setup(this, "bookings");
        cargarReservas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReservas();
    }

    private void cargarReservas() {
        SessionManager session = SessionManager.getInstance(this);
        String perfilId        = session.getPerfilId();

        if (perfilId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        SupabaseClient.getInstance().getReservas(perfilId,
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        procesarReservas(body);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProfileActivity.this,
                                "No se pudieron cargar las reservas",
                                Toast.LENGTH_SHORT).show();
                        mostrarLista(showingActive);
                    }
                });
    }

    private void procesarReservas(String body) {
        activeList.clear();
        pastList.clear();

        try {
            JSONArray arr = new JSONArray(body);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj      = arr.getJSONObject(i);
                String reservaId   = obj.optString("id", "");
                String estado      = obj.optString("estado_reserva", "Confirmada");
                String fecha       = obj.optString("fecha_reserva", "");
                String horaInicio  = obj.optString("hora_inicio", "");
                String horaFin     = obj.optString("hora_fin", "");
                String pistaId     = obj.optString("pista_id", "");
                String timeStr     = fecha + "  " + horaInicio + " - " + horaFin;

                // Datos de la pista via join
                String pistaNombre = "Instalación deportiva";
                String tipoDeporte = "";
                String imagenUrl   = "";
                String direccion   = "";
                String precio      = "";
                String imgNombre   = "pista_baloncesto_1";

                if (obj.has("pistas") && !obj.isNull("pistas")) {
                    JSONObject pista = obj.getJSONObject("pistas");
                    pistaNombre = pista.optString("nombre", pistaNombre);
                    tipoDeporte = pista.optString("tipo_deporte", "");
                    precio      = pista.optString("precio_hora", "");

                    // Imagen desde imagenes_pista
                    if (pista.has("imagenes_pista")) {
                        JSONArray imgs = pista.getJSONArray("imagenes_pista");
                        for (int j = 0; j < imgs.length(); j++) {
                            JSONObject img = imgs.getJSONObject(j);
                            if (img.optBoolean("es_principal", false)) {
                                imagenUrl = img.optString("url_imagen","");
                                break;
                            }
                        }
                        if (imagenUrl.isEmpty() && imgs.length() > 0) {
                            imagenUrl = imgs.getJSONObject(0)
                                    .optString("url_imagen","");
                        }
                    }

                    // Dirección desde instalaciones
                    if (pista.has("instalaciones")
                            && !pista.isNull("instalaciones")) {
                        Object instObj = pista.get("instalaciones");
                        if (instObj instanceof JSONObject) {
                            direccion = ((JSONObject) instObj)
                                    .optString("direccion","");
                        }
                    }

                    // Imagen local fallback
                    switch (tipoDeporte) {
                        case "Tenis":  imgNombre = "pista_tenis";  break;
                        case "Pádel":
                        case "Padel":  imgNombre = "pista_padel";  break;
                        default:       imgNombre = "pista_baloncesto_1"; break;
                    }
                }

                ReservationModel res = new ReservationModel(
                        0L, pistaNombre, timeStr, imgNombre, estado,
                        pistaId, imagenUrl, direccion, tipoDeporte,
                        fecha, horaInicio, horaFin, precio);

                if ("Confirmada".equalsIgnoreCase(estado)
                        || "Pendiente".equalsIgnoreCase(estado)) {
                    activeList.add(res);
                } else {
                    pastList.add(res);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error procesando datos",
                    Toast.LENGTH_SHORT).show();
        }

        mostrarLista(showingActive);
    }

    private void mostrarLista(boolean active) {
        showingActive = active;

        if (active) {
            btnReservadas.setBackgroundTintList(
                    ColorStateList.valueOf(Color.WHITE));
            btnPasadas.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
        } else {
            btnReservadas.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
            btnPasadas.setBackgroundTintList(
                    ColorStateList.valueOf(Color.WHITE));
        }
        btnReservadas.setTextColor(Color.BLACK);
        btnPasadas.setTextColor(Color.BLACK);

        List<ReservationModel> data = active ? activeList : pastList;
        adapter = new ReservationAdapter(data,
                position -> {
                    if (active) cancelarReserva(data.get(position));
                },
                position -> abrirDetalles(data.get(position))
        );
        rv.setAdapter(adapter);
    }

    private void abrirDetalles(ReservationModel reserva) {
        Intent intent = new Intent(this, ConfirmacionReservaActivity.class);
        intent.putExtra("pista_nombre", reserva.getFacilityName());
        intent.putExtra("tipo_deporte", reserva.getTipoDeporte());
        intent.putExtra("fecha",        reserva.getFecha());
        intent.putExtra("hora_inicio",  reserva.getHoraInicio());
        intent.putExtra("hora_fin",     reserva.getHoraFin());
        intent.putExtra("precio",       reserva.getPrecio());
        intent.putExtra("imagen_url",   reserva.getImagenUrl());
        intent.putExtra("direccion",    reserva.getDireccion());
        intent.putExtra("imagen_res",   getImagenRes(reserva.getTipoDeporte()));
        startActivity(intent);
    }

    private int getImagenRes(String deporte) {
        if (deporte == null) return R.drawable.pista_baloncesto_1;
        switch (deporte) {
            case "Tenis":  return R.drawable.pista_tenis;
            case "Pádel":
            case "Padel":  return R.drawable.pista_padel;
            default:       return R.drawable.pista_baloncesto_1;
        }
    }

    private void cancelarReserva(ReservationModel reserva) {
        SessionManager session = SessionManager.getInstance(this);
        String perfilId        = session.getPerfilId();

        SupabaseClient.getInstance().getReservas(perfilId,
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        try {
                            JSONArray arr = new JSONArray(body);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                String fecha   = obj.optString("fecha_reserva","");
                                String hora    = obj.optString("hora_inicio","");

                                if (reserva.getTime().contains(fecha)
                                        && reserva.getTime().contains(hora)) {
                                    String reservaId = obj.getString("id");
                                    eliminarReserva(reservaId);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(ProfileActivity.this,
                                    "Error al cancelar",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProfileActivity.this,
                                "No se pudo cancelar",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarReserva(String reservaId) {
        SupabaseClient.getInstance().deleteReserva(reservaId,
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        Toast.makeText(ProfileActivity.this,
                                "Reserva cancelada ✅",
                                Toast.LENGTH_SHORT).show();
                        cargarReservas();
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProfileActivity.this,
                                "No se pudo cancelar",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}