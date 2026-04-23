package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseConfig;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etSurname, etEmail, etZip, etPass;
    private Button   btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName      = findViewById(R.id.etName);
        etSurname   = findViewById(R.id.etSurname);
        etEmail     = findViewById(R.id.etEmailReg);
        etZip       = findViewById(R.id.etZipCode);
        etPass      = findViewById(R.id.etPassReg);
        btnRegister = findViewById(R.id.btnRegisterAction);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> {
            String nombre    = etName.getText().toString().trim();
            String apellidos = etSurname.getText().toString().trim();
            String email     = etEmail.getText().toString().trim();
            String pass      = etPass.getText().toString().trim();

            if (nombre.isEmpty())    { etName.setError("Campo obligatorio");    return; }
            if (apellidos.isEmpty()) { etSurname.setError("Campo obligatorio"); return; }
            if (email.isEmpty())     { etEmail.setError("Campo obligatorio");   return; }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email no válido"); return;
            }
            if (pass.isEmpty())    { etPass.setError("Campo obligatorio");   return; }
            if (pass.length() < 6) { etPass.setError("Mínimo 6 caracteres"); return; }

            btnRegister.setEnabled(false);
            btnRegister.setText("Registrando...");

            String nombreCompleto = nombre + " " + apellidos;
            String nuevoId        = UUID.randomUUID().toString();

            try {
                org.json.JSONObject perfil = new org.json.JSONObject();
                perfil.put("id",              nuevoId);
                perfil.put("email",           email);
                perfil.put("password",        pass);
                perfil.put("nombre_completo", nombreCompleto);
                perfil.put("rol",             "USER");
                perfil.put("esta_baneado",    false);

                new Thread(() -> {
                    try {
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.MediaType JSON = okhttp3.MediaType.get(
                                "application/json");
                        okhttp3.Request req = new okhttp3.Request.Builder()
                                .url(SupabaseConfig.REST_URL + "/perfiles")
                                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Prefer", "return=representation")
                                .post(okhttp3.RequestBody.create(
                                        perfil.toString(), JSON))
                                .build();

                        okhttp3.Response response = client.newCall(req).execute();
                        String body = response.body() != null
                                ? response.body().string() : "[]";

                        runOnUiThread(() -> {
                            btnRegister.setEnabled(true);
                            btnRegister.setText(
                                    getString(R.string.registrarse));

                            if (response.isSuccessful()) {
                                try {
                                    JSONArray arr = new JSONArray(body);
                                    if (arr.length() > 0) {
                                        JSONObject p  = arr.getJSONObject(0);
                                        String pid    = p.getString("id");
                                        String pemail = p.getString("email");
                                        String pnom   = p.optString(
                                                "nombre_completo","Usuario");
                                        SessionManager.getInstance(
                                                        RegisterActivity.this)
                                                .saveSession(pid, pemail, pnom);
                                        startActivity(new Intent(
                                                RegisterActivity.this,
                                                HomeActivity.class));
                                        finish();
                                    }
                                } catch (Exception ex) {
                                    irAlLogin();
                                }
                            } else {
                                if (body.contains("duplicate")
                                        || body.contains("unique")) {
                                    Toast.makeText(RegisterActivity.this,
                                            "Este email ya está registrado",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                            "Error: " + body,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } catch (Exception ex) {
                        runOnUiThread(() -> {
                            btnRegister.setEnabled(true);
                            btnRegister.setText(
                                    getString(R.string.registrarse));
                            Toast.makeText(RegisterActivity.this,
                                    "Error de conexión",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();

            } catch (Exception e) {
                btnRegister.setEnabled(true);
                btnRegister.setText(getString(R.string.registrarse));
                Toast.makeText(this, "Error inesperado",
                        Toast.LENGTH_SHORT).show();
            }
        });

        tvGoToLogin.setOnClickListener(v -> irAlLogin());
    }

    private void irAlLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}