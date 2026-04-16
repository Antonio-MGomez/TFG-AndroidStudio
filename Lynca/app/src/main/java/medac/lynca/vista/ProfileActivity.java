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

    private String getNombrePista(String pistaId) {
        if (pistaId == null) return "Instalación";
        switch (pistaId) {
            case "09b0e24c-79db-482a-8cf2-2c33a3e1dddf": return "Pista Tenis";
            case "99a95eae-e5cb-49f5-8475-43a659a1fd4a": return "Pista Pádel 1";
            case "d3304f3d-c511-41fd-a65f-027566151951": return "Pista Pádel 2";
            case "eb1707df-023f-4353-ad4c-3a6ebb27f0de": return "Pista Fútbol Sala";
            default: return "Instalación deportiva";
        }
    }

    private String getImagenPorPista(String pistaId) {
        if (pistaId == null) return "pista_baloncesto_1";
        switch (pistaId) {
            case "09b0e24c-79db-482a-8cf2-2c33a3e1dddf": return "pista_tenis";
            case "99a95eae-e5cb-49f5-8475-43a659a1fd4a":
            case "d3304f3d-c511-41fd-a65f-027566151951": return "pista_padel";
            default: return "pista_baloncesto_1";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // ── Bookings activo en la barra ──────────────────────────
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
                JSONObject obj    = arr.getJSONObject(i);
                String estado     = obj.optString("estado_reserva", "Confirmada");
                String fecha      = obj.optString("fecha_reserva", "");
                String horaInicio = obj.optString("hora_inicio", "");
                String horaFin    = obj.optString("hora_fin", "");
                String pistaId    = obj.optString("pista_id", "");

                String pistaNombre = getNombrePista(pistaId);
                String imgNombre   = getImagenPorPista(pistaId);
                String timeStr     = fecha + "  " + horaInicio + " - " + horaFin;

                ReservationModel res = new ReservationModel(
                        0L, pistaNombre, timeStr, imgNombre, estado);

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
        adapter = new ReservationAdapter(data, position -> {
            if (active) cancelarReserva(data.get(position));
        });
        rv.setAdapter(adapter);
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
                                    "Error al cancelar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProfileActivity.this,
                                "No se pudo cancelar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarReserva(String reservaId) {
        SupabaseClient.getInstance().deleteReserva(reservaId,
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        Toast.makeText(ProfileActivity.this,
                                "Reserva cancelada ✅", Toast.LENGTH_SHORT).show();
                        cargarReservas();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProfileActivity.this,
                                "No se pudo cancelar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}