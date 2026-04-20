package medac.lynca.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button   btnLogin, btnGoogle, btnRegister;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);

        // Aplicar modo nocturno guardado
        SharedPreferences prefs = getSharedPreferences("LyncaPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SessionManager.getInstance(this).isLoggedIn()) {
            goToHome();
            return;
        }

        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        btnGoogle        = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnRegister      = findViewById(R.id.btnRegisterAction);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (!validarDatos(email, pass)) return;

            btnLogin.setEnabled(false);
            btnLogin.setText("Entrando...");

            SupabaseClient.getInstance().login(email, pass,
                    new SupabaseClient.Callback() {
                        @Override
                        public void onSuccess(String body) {
                            try {
                                JSONArray arr = new JSONArray(body);
                                if (arr.length() == 0) {
                                    btnLogin.setEnabled(true);
                                    btnLogin.setText(
                                            getString(R.string.inicio_sesion));
                                    Toast.makeText(LoginActivity.this,
                                            "Email o contraseña incorrectos",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                JSONObject perfil = arr.getJSONObject(0);
                                String perfilId   = perfil.getString("id");
                                String userEmail  = perfil.getString("email");
                                String nombre     = perfil.optString(
                                        "nombre_completo", "Usuario");

                                SessionManager.getInstance(LoginActivity.this)
                                        .saveSession(perfilId, userEmail, nombre);
                                goToHome();

                            } catch (Exception e) {
                                btnLogin.setEnabled(true);
                                btnLogin.setText(getString(R.string.inicio_sesion));
                                Toast.makeText(LoginActivity.this,
                                        "Error al procesar respuesta",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText(getString(R.string.inicio_sesion));
                            Toast.makeText(LoginActivity.this,
                                    "Error de conexión. Inténtalo de nuevo.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this,
                        getString(R.string.proximamente),
                        Toast.LENGTH_SHORT).show());

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this,
                        RecuperarContrasenaActivity.class)));
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private boolean validarDatos(String email, String pass) {
        if (email.isEmpty()) {
            etEmail.setError("Introduce tu email"); return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido"); return false;
        }
        if (pass.isEmpty()) {
            etPassword.setError("Introduce tu contraseña"); return false;
        }
        return true;
    }
}