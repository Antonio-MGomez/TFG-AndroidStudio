package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseConfig;

public class EditarPerfilActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etCodigoPostal;
    private EditText etNuevaPass, etConfirmarPass;
    private String   deporteFavorito = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        etNombre        = findViewById(R.id.etNombreEdit);
        etEmail         = findViewById(R.id.etEmailEdit);
        etCodigoPostal  = findViewById(R.id.etCodigoPostalEdit);
        etNuevaPass     = findViewById(R.id.etNuevaPass);
        etConfirmarPass = findViewById(R.id.etConfirmarPass);

        // Rellenar con datos actuales
        SessionManager session = SessionManager.getInstance(this);
        etNombre.setText(session.getNombre());
        etEmail.setText(session.getEmail());

        // Deporte favorito guardado
        String deporteGuardado = getSharedPreferences("LyncaPrefs", MODE_PRIVATE)
                .getString("deporte_favorito", "Tenis");
        deporteFavorito = deporteGuardado;

        // Botón volver
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Botón cambiar foto
        LinearLayout btnFoto = findViewById(R.id.btnCambiarFoto);
        if (btnFoto != null) {
            btnFoto.setOnClickListener(v ->
                    Toast.makeText(this,
                            "Función de cambio de foto próximamente",
                            Toast.LENGTH_SHORT).show());
        }

        // Selector deporte favorito
        setupDeportes(deporteGuardado);

        // Botón Guardar
        Button btnGuardar = findViewById(R.id.btnGuardar);
        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> guardarCambios());
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Selector deporte favorito
    // ════════════════════════════════════════════════════════════
    private void setupDeportes(String deporteActual) {
        int[] ids = {
                R.id.btnDeporteTenis,
                R.id.btnDeportePadel,
                R.id.btnDeporteFutbol,
                R.id.btnDeporteBalon
        };
        String[] deportes = {"Tenis", "Pádel", "Fútbol", "Baloncesto"};

        for (int i = 0; i < ids.length; i++) {
            TextView btn = findViewById(ids[i]);
            if (btn == null) continue;
            final String deporte    = deportes[i];
            final int    selectedId = ids[i];

            // Marcar el deporte actual
            if (deporte.equals(deporteActual)) {
                btn.setBackgroundResource(R.drawable.bg_chip_selected);
                btn.setTextColor(getColor(android.R.color.white));
            }

            btn.setOnClickListener(v -> {
                deporteFavorito = deporte;
                updateDeporteStyles(ids, selectedId);
            });
        }
    }

    private void updateDeporteStyles(int[] ids, int selectedId) {
        for (int id : ids) {
            TextView btn = findViewById(id);
            if (btn == null) continue;
            if (id == selectedId) {
                btn.setBackgroundResource(R.drawable.bg_chip_selected);
                btn.setTextColor(getColor(android.R.color.white));
            } else {
                btn.setBackgroundResource(R.drawable.bg_chip_normal);
                btn.setTextColor(getColor(android.R.color.black));
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Guardar cambios en Supabase
    // ════════════════════════════════════════════════════════════
    private void guardarCambios() {
        String nombre   = etNombre.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String cp       = etCodigoPostal.getText().toString().trim();
        String newPass  = etNuevaPass.getText().toString().trim();
        String confPass = etConfirmarPass.getText().toString().trim();

        if (nombre.isEmpty()) { etNombre.setError("Campo obligatorio"); return; }
        if (email.isEmpty())  { etEmail.setError("Campo obligatorio");  return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido"); return;
        }
        if (!newPass.isEmpty()) {
            if (newPass.length() < 6) {
                etNuevaPass.setError("Mínimo 6 caracteres"); return;
            }
            if (!newPass.equals(confPass)) {
                etConfirmarPass.setError("Las contraseñas no coinciden"); return;
            }
        }

        SessionManager session = SessionManager.getInstance(this);
        String perfilId = session.getPerfilId();
        String emailAnterior = session.getEmail();

        if (perfilId == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject update = new JSONObject();
            update.put("nombre_completo", nombre);
            update.put("email",           email);
            if (!cp.isEmpty())       update.put("codigo_postal", cp);
            if (!newPass.isEmpty())  update.put("password", newPass);

            String body = update.toString();

            new Thread(() -> {
                try {
                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                    okhttp3.MediaType JSON = okhttp3.MediaType.get(
                            "application/json");
                    okhttp3.Request req = new okhttp3.Request.Builder()
                            .url(SupabaseConfig.REST_URL
                                    + "/perfiles?id=eq." + perfilId)
                            .addHeader("apikey", SupabaseConfig.ANON_KEY)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Prefer", "return=representation")
                            .patch(okhttp3.RequestBody.create(body, JSON))
                            .build();

                    okhttp3.Response response = client.newCall(req).execute();

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            // Guardar deporte favorito
                            if (!deporteFavorito.isEmpty()) {
                                getSharedPreferences("LyncaPrefs", MODE_PRIVATE)
                                        .edit()
                                        .putString("deporte_favorito",
                                                deporteFavorito)
                                        .apply();
                            }

                            // Actualizar sesión local
                            session.saveSession(perfilId, email, nombre);

                            Toast.makeText(EditarPerfilActivity.this,
                                    "✅ Perfil actualizado correctamente",
                                    Toast.LENGTH_SHORT).show();

                            // Si cambió email o contraseña → cerrar sesión
                            if (!newPass.isEmpty()
                                    || !email.equals(emailAnterior)) {
                                session.clearSession();
                                Intent intent = new Intent(
                                        EditarPerfilActivity.this,
                                        LoginActivity.class);
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                finish();
                            }
                        } else {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Error al guardar cambios",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Error de conexión",
                                    Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show();
        }
    }
}