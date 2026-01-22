package medac.lynca.vista;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.ReservationModel;
import medac.lynca.modelo.ReservationRepository;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ReservationAdapter adapter;
    private Button btnReservadas, btnPasadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rv = findViewById(R.id.rvReservations);
        btnReservadas = findViewById(R.id.tabReservadas);
        btnPasadas = findViewById(R.id.tabPasadas);

        rv.setLayoutManager(new LinearLayoutManager(this));

        // Carga inicial (Reservadas)
        loadReservations(true);

        btnReservadas.setOnClickListener(v -> loadReservations(true));
        btnPasadas.setOnClickListener(v -> loadReservations(false));
    }

    private void loadReservations(boolean active) {
        // Colores fijos
        int colorBlanco = Color.WHITE;
        int colorGrisFondo = Color.parseColor("#DDDDDD"); // El color del contenedor
        int colorNegro = Color.BLACK;

        // Lógica de intercambio de botones (Letras siempre negras)
        if (active) {
            // RESERVADAS seleccionado
            btnReservadas.setBackgroundTintList(ColorStateList.valueOf(colorBlanco));
            btnReservadas.setTextColor(colorNegro);

            // PASADAS apagado (se vuelve gris)
            btnPasadas.setBackgroundTintList(ColorStateList.valueOf(colorGrisFondo));
            btnPasadas.setTextColor(colorNegro);
        } else {
            // RESERVADAS apagado (se vuelve gris)
            btnReservadas.setBackgroundTintList(ColorStateList.valueOf(colorGrisFondo));
            btnReservadas.setTextColor(colorNegro);

            // PASADAS seleccionado
            btnPasadas.setBackgroundTintList(ColorStateList.valueOf(colorBlanco));
            btnPasadas.setTextColor(colorNegro);
        }

        // Cargar lista (Pasadas o Reservadas)
        List<ReservationModel> data = active ?
                ReservationRepository.getInstance().getActive() :
                ReservationRepository.getInstance().getPast();

        // El adaptador ocultará el botón cancelar si es una pista pasada
        adapter = new ReservationAdapter(data, position -> {
            if (active) {
                ReservationRepository.getInstance().removeReservation(position);
                loadReservations(true);
            }
        });
        rv.setAdapter(adapter);
    }
}