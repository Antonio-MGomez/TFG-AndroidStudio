package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import medac.lynca.R;

public class ConfirmacionReservaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion_reserva);

        // Recoger datos
        String pistaNombre = getIntent().getStringExtra("pista_nombre");
        String tipoDeporte = getIntent().getStringExtra("tipo_deporte");
        String fecha       = getIntent().getStringExtra("fecha");
        String horaInicio  = getIntent().getStringExtra("hora_inicio");
        String horaFin     = getIntent().getStringExtra("hora_fin");
        String precio      = getIntent().getStringExtra("precio");
        String imagenUrl   = getIntent().getStringExtra("imagen_url");
        String direccion   = getIntent().getStringExtra("direccion");
        int    imagenRes   = getIntent().getIntExtra("imagen_res",
                R.drawable.pista_baloncesto_1);

        // Nombre pista
        TextView tvNombre = findViewById(R.id.tvConfNombrePista);
        if (tvNombre != null)
            tvNombre.setText(pistaNombre != null ? pistaNombre : "Pista");

        // Dirección
        TextView tvDir = findViewById(R.id.tvConfDireccion);
        if (tvDir != null) {
            if (direccion != null && !direccion.isEmpty()) {
                tvDir.setText("📍 " + direccion);
                tvDir.setVisibility(View.VISIBLE);
            } else {
                tvDir.setVisibility(View.GONE);
            }
        }

        // Imagen
        ImageView img = findViewById(R.id.imgConfirmacion);
        if (img != null) {
            if (imagenUrl != null && !imagenUrl.isEmpty()) {
                Glide.with(this)
                        .load(imagenUrl)
                        .placeholder(imagenRes)
                        .error(imagenRes)
                        .centerCrop()
                        .into(img);
            } else {
                img.setImageResource(imagenRes);
            }
        }

        // Fecha formateada
        TextView tvFecha = findViewById(R.id.tvConfFecha);
        if (tvFecha != null) {
            if (fecha != null && fecha.contains("-")) {
                try {
                    String[] p = fecha.split("-");
                    tvFecha.setText(p[2] + "/" + p[1] + "/" + p[0]);
                } catch (Exception e) {
                    tvFecha.setText(fecha);
                }
            } else {
                tvFecha.setText(fecha != null ? fecha : "--");
            }
        }

        // Hora
        TextView tvHora = findViewById(R.id.tvConfHora);
        if (tvHora != null) {
            String horaTexto = "";
            if (horaInicio != null) horaTexto += horaInicio;
            horaTexto += " - ";
            if (horaFin != null) horaTexto += horaFin;
            tvHora.setText(horaTexto);
        }

        // Deporte
        TextView tvDeporte = findViewById(R.id.tvConfDeporte);
        if (tvDeporte != null)
            tvDeporte.setText(tipoDeporte != null ? tipoDeporte : "--");

        // Precio
        TextView tvPrecio = findViewById(R.id.tvConfPrecio);
        if (tvPrecio != null)
            tvPrecio.setText("€" + (precio != null ? precio : "0"));

        // Botón ver reservas
        Button btnVerReservas = findViewById(R.id.btnVerReservas);
        if (btnVerReservas != null) {
            btnVerReservas.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }

        // Botón volver al inicio
        Button btnVolverInicio = findViewById(R.id.btnVolverInicio);
        if (btnVolverInicio != null) {
            btnVolverInicio.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
}