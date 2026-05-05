package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "PAGO_ERROR";

    private static final int METODO_NINGUNO    = 0;
    private static final int METODO_EFECTIVO   = 1;
    private static final int METODO_GOOGLE_PAY = 2;
    private static final int METODO_PAYPAL     = 3;
    private static final int METODO_TARJETA    = 4;

    private int metodoSeleccionado = METODO_NINGUNO;

    private CardView layoutTarjeta;
    private EditText etNumeroTarjeta, etCaducidad, etCvv, etNombreTarjeta;
    private Button   btnPagar;

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

        try { precioFinal = Integer.parseInt(precio); }
        catch (Exception e) { precioFinal = 0; }

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

        if (tvNombre != null) tvNombre.setText(
                pistaNombre != null ? pistaNombre : "--");
        if (tvPrecio != null) tvPrecio.setText(
                "€" + (precio != null ? precio : "0"));
        if (tvHora != null) tvHora.setText(
                (horaInicio != null ? horaInicio : "--")
                        + " - " + (horaFin != null ? horaFin : "--"));
        if (tvFecha != null) {
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

        // Métodos de pago
        CardView cardEfectivo = findViewById(R.id.cardEfectivo);
        CardView cardGoogle   = findViewById(R.id.cardGooglePay);
        CardView cardPaypal   = findViewById(R.id.cardPaypal);
        CardView cardTarjeta  = findViewById(R.id.cardTarjeta);

        if (cardEfectivo != null) cardEfectivo.setOnClickListener(v ->
                seleccionarMetodo(METODO_EFECTIVO));
        if (cardGoogle != null) cardGoogle.setOnClickListener(v ->
                seleccionarMetodo(METODO_GOOGLE_PAY));
        if (cardPaypal != null) cardPaypal.setOnClickListener(v ->
                seleccionarMetodo(METODO_PAYPAL));
        if (cardTarjeta != null) cardTarjeta.setOnClickListener(v ->
                seleccionarMetodo(METODO_TARJETA));

        btnPagar.setOnClickListener(v -> procesarPago());
    }

    // ════════════════════════════════════════════════════════════
    //  Seleccionar método
    // ════════════════════════════════════════════════════════════
    private void seleccionarMetodo(int metodo) {
        metodoSeleccionado = metodo;
        resetCards();

        switch (metodo) {
            case METODO_EFECTIVO:
                destacarCard(R.id.cardEfectivo);
                layoutTarjeta.setVisibility(View.GONE);
                btnPagar.setText("Reservar y pagar en efectivo");
                break;
            case METODO_GOOGLE_PAY:
                destacarCard(R.id.cardGooglePay);
                layoutTarjeta.setVisibility(View.GONE);
                btnPagar.setText("Pagar con Google Pay");
                break;
            case METODO_PAYPAL:
                destacarCard(R.id.cardPaypal);
                layoutTarjeta.setVisibility(View.GONE);
                btnPagar.setText("Pagar con PayPal");
                break;
            case METODO_TARJETA:
                destacarCard(R.id.cardTarjeta);
                layoutTarjeta.setVisibility(View.VISIBLE);
                btnPagar.setText("Pagar con tarjeta");
                break;
        }
    }

    private void resetCards() {
        int[] cardIds = {
                R.id.cardEfectivo, R.id.cardGooglePay,
                R.id.cardPaypal,   R.id.cardTarjeta
        };
        for (int id : cardIds) {
            CardView card = findViewById(id);
            if (card != null) card.setCardBackgroundColor(
                    getColor(android.R.color.white));
        }
    }

    private void destacarCard(int cardId) {
        CardView card = findViewById(cardId);
        if (card != null) card.setCardBackgroundColor(0xFFE8F0FE);
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

        if (metodoSeleccionado == METODO_EFECTIVO) {
            guardarReserva();
        } else {
            btnPagar.postDelayed(this::guardarReserva, 1500);
        }
    }

    private boolean validarTarjeta() {
        String numero = etNumeroTarjeta.getText().toString().trim();
        String cad    = etCaducidad.getText().toString().trim();
        String cvv    = etCvv.getText().toString().trim();
        String nombre = etNombreTarjeta.getText().toString().trim();

        if (numero.length() < 16) {
            etNumeroTarjeta.setError("Número de tarjeta inválido"); return false;
        }
        if (cad.length() < 4) {
            etCaducidad.setError("Fecha inválida"); return false;
        }
        if (cvv.length() < 3) {
            etCvv.setError("CVV inválido"); return false;
        }
        if (nombre.isEmpty()) {
            etNombreTarjeta.setError("Introduce el nombre"); return false;
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
            btnPagar.setText("Confirmar reserva");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (pistaId == null || fecha == null
                || horaInicio == null || horaFin == null) {
            btnPagar.setEnabled(true);
            btnPagar.setText("Confirmar reserva");
            Toast.makeText(this,
                    "Error: datos incompletos. Vuelve a intentarlo.",
                    Toast.LENGTH_LONG).show();
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

            Log.d(TAG, "JSON: " + reserva.toString());

            SupabaseClient.getInstance().insertReserva(reserva,
                    new SupabaseClient.Callback() {
                        @Override
                        public void onSuccess(String body) {
                            Log.d(TAG, "OK: " + body);
                            btnPagar.setEnabled(true);
                            btnPagar.setText("Confirmar reserva");

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
                            Log.e(TAG, "Error: " + error);
                            btnPagar.setEnabled(true);
                            btnPagar.setText("Confirmar reserva");
                            Toast.makeText(PagoActivity.this,
                                    "Error: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Excepcion: " + e.getMessage());
            btnPagar.setEnabled(true);
            btnPagar.setText("Confirmar reserva");
            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}