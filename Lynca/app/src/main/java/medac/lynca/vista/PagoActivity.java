package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONObject;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;

public class PagoActivity extends AppCompatActivity {

    private static final int METODO_NINGUNO      = 0;
    private static final int METODO_GOOGLE_PAY   = 1;
    private static final int METODO_PAYPAL       = 2;
    private static final int METODO_TARJETA      = 3;

    private int metodoSeleccionado = METODO_NINGUNO;

    private CardView layoutTarjeta;
    private EditText etNumeroTarjeta, etCaducidad, etCvv, etNombreTarjeta;
    private Button   btnPagar;

    // Datos de la reserva
    private String pistaNombre, tipoDeporte, fecha;
    private String horaInicio, horaFin, precio;
    private String imagenUrl, direccion, pistaId;
    private int    imagenRes;
    private int    precioFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        // Recoger datos del intent
        pistaNombre = getIntent().getStringExtra("pista_nombre");
        tipoDeporte = getIntent().getStringExtra("tipo_deporte");
        fecha       = getIntent().getStringExtra("fecha");
        horaInicio  = getIntent().getStringExtra("hora_inicio");
        horaFin     = getIntent().getStringExtra("hora_fin");
        precio      = getIntent().getStringExtra("precio");
        imagenUrl   = getIntent().getStringExtra("imagen_url");
        direccion   = getIntent().getStringExtra("direccion");
        pistaId     = getIntent().getStringExtra("pista_id");
        imagenRes   = getIntent().getIntExtra("imagen_res",
                R.drawable.pista_baloncesto_1);

        try {
            precioFinal = Integer.parseInt(precio);
        } catch (Exception e) { precioFinal = 0; }

        // Vistas
        layoutTarjeta   = findViewById(R.id.layoutTarjeta);
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etCaducidad     = findViewById(R.id.etCaducidad);
        etCvv           = findViewById(R.id.etCvv);
        etNombreTarjeta = findViewById(R.id.etNombreTarjeta);
        btnPagar        = findViewById(R.id.btnPagar);

        // Rellenar resumen
        TextView tvNombre = findViewById(R.id.tvPagoNombrePista);
        TextView tvFecha  = findViewById(R.id.tvPagoFecha);
        TextView tvHora   = findViewById(R.id.tvPagoHora);
        TextView tvPrecio = findViewById(R.id.tvPagoPrecio);

        if (tvNombre != null) tvNombre.setText(pistaNombre);
        if (tvPrecio != null) tvPrecio.setText("€" + precio);
        if (tvHora   != null) tvHora.setText(horaInicio + " - " + horaFin);
        if (tvFecha  != null) {
            if (fecha != null && fecha.contains("-")) {
                try {
                    String[] p = fecha.split("-");
                    tvFecha.setText(p[2] + "/" + p[1] + "/" + p[0]);
                } catch (Exception e) { tvFecha.setText(fecha); }
            } else {
                tvFecha.setText(fecha != null ? fecha : "--");
            }
        }

        // Botón volver
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Selección método de pago
        CardView cardGoogle  = findViewById(R.id.cardGooglePay);
        CardView cardPaypal  = findViewById(R.id.cardPaypal);
        CardView cardTarjeta = findViewById(R.id.cardTarjeta);

        if (cardGoogle  != null) cardGoogle.setOnClickListener(v ->
                seleccionarMetodo(METODO_GOOGLE_PAY));
        if (cardPaypal  != null) cardPaypal.setOnClickListener(v ->
                seleccionarMetodo(METODO_PAYPAL));
        if (cardTarjeta != null) cardTarjeta.setOnClickListener(v ->
                seleccionarMetodo(METODO_TARJETA));

        // Botón pagar
        btnPagar.setOnClickListener(v -> procesarPago());
    }

    // ════════════════════════════════════════════════════════════
    //  Seleccionar método de pago
    // ════════════════════════════════════════════════════════════
    private void seleccionarMetodo(int metodo) {
        metodoSeleccionado = metodo;

        // Resetear estilos
        resetCards();

        // Marcar seleccionado
        switch (metodo) {
            case METODO_GOOGLE_PAY:
                destacarCard(R.id.cardGooglePay, R.id.radioGooglePay);
                layoutTarjeta.setVisibility(View.GONE);
                break;
            case METODO_PAYPAL:
                destacarCard(R.id.cardPaypal, R.id.radioPaypal);
                layoutTarjeta.setVisibility(View.GONE);
                break;
            case METODO_TARJETA:
                destacarCard(R.id.cardTarjeta, R.id.radioTarjeta);
                layoutTarjeta.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void resetCards() {
        int[] cardIds  = {R.id.cardGooglePay, R.id.cardPaypal, R.id.cardTarjeta};
        int[] radioIds = {R.id.radioGooglePay, R.id.radioPaypal, R.id.radioTarjeta};
        for (int i = 0; i < cardIds.length; i++) {
            CardView card = findViewById(cardIds[i]);
            if (card != null) {
                card.setCardBackgroundColor(
                        getColor(android.R.color.white));
            }
        }
    }

    private void destacarCard(int cardId, int radioId) {
        CardView card = findViewById(cardId);
        if (card != null) {
            card.setCardBackgroundColor(
                    getColor(android.R.color.holo_blue_light));
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Procesar pago
    // ════════════════════════════════════════════════════════════
    private void procesarPago() {
        if (metodoSeleccionado == METODO_NINGUNO) {
            Toast.makeText(this, "Selecciona un método de pago",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (metodoSeleccionado == METODO_TARJETA) {
            if (!validarTarjeta()) return;
        }

        btnPagar.setEnabled(false);
        btnPagar.setText("Procesando...");

        // Simular proceso de pago
        btnPagar.postDelayed(() -> guardarReserva(), 1500);
    }

    private boolean validarTarjeta() {
        String numero = etNumeroTarjeta.getText().toString().trim();
        String cad    = etCaducidad.getText().toString().trim();
        String cvv    = etCvv.getText().toString().trim();
        String nombre = etNombreTarjeta.getText().toString().trim();

        if (numero.length() < 16) {
            etNumeroTarjeta.setError("Número de tarjeta inválido");
            return false;
        }
        if (cad.length() < 4) {
            etCaducidad.setError("Fecha inválida");
            return false;
        }
        if (cvv.length() < 3) {
            etCvv.setError("CVV inválido");
            return false;
        }
        if (nombre.isEmpty()) {
            etNombreTarjeta.setError("Introduce el nombre");
            return false;
        }
        return true;
    }

    // ════════════════════════════════════════════════════════════
    //  Guardar reserva en Supabase
    // ════════════════════════════════════════════════════════════
    private void guardarReserva() {
        SessionManager session = SessionManager.getInstance(this);
        String perfilId = session.getPerfilId();

        if (perfilId == null) {
            btnPagar.setEnabled(true);
            btnPagar.setText("Pagar ahora");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        try {
            JSONObject reserva = new JSONObject();
            reserva.put("perfil_id",      perfilId);
            reserva.put("pista_id",       pistaId);
            reserva.put("fecha_reserva",  fecha);
            reserva.put("hora_inicio",    horaInicio + ":00");
            reserva.put("hora_fin",       horaFin + ":00");
            reserva.put("estado_reserva", "Confirmada");
            reserva.put("precio_total",   precioFinal);

            SupabaseClient.getInstance().insertReserva(reserva,
                    new SupabaseClient.Callback() {
                        @Override
                        public void onSuccess(String body) {
                            btnPagar.setEnabled(true);
                            btnPagar.setText("Pagar ahora");

                            // Ir a confirmación
                            Intent intent = new Intent(PagoActivity.this,
                                    ConfirmacionReservaActivity.class);
                            intent.putExtra("pista_nombre", pistaNombre);
                            intent.putExtra("tipo_deporte", tipoDeporte);
                            intent.putExtra("fecha",        fecha);
                            intent.putExtra("hora_inicio",  horaInicio);
                            intent.putExtra("hora_fin",     horaFin);
                            intent.putExtra("precio",       precio);
                            intent.putExtra("imagen_url",   imagenUrl);
                            intent.putExtra("imagen_res",   imagenRes);
                            intent.putExtra("direccion",    direccion);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            btnPagar.setEnabled(true);
                            btnPagar.setText("Pagar ahora");
                            Toast.makeText(PagoActivity.this,
                                    "Error al procesar el pago: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            btnPagar.setEnabled(true);
            btnPagar.setText("Pagar ahora");
            Toast.makeText(this, "Error inesperado",
                    Toast.LENGTH_SHORT).show();
        }
    }
}