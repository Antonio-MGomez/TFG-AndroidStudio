package medac.lynca.vista;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import medac.lynca.R;

public class LoginActivity extends AppCompatActivity {

    // Declarar variables
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle, btnRegister;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Vincular vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnRegister = findViewById(R.id.btnRegisterAction);

        // 2. Acción botón Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String pass = etPassword.getText().toString();

                if (validarDatos(email, pass)) {
                    // AQUÍ IRÍA TU LÓGICA DE BACKEND (Firebase, API, etc.)
                    Toast.makeText(LoginActivity.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 3. Acción botón Google
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Login con Google", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Acción Registrarse (Texto coloreado)
        // Opcional: Para poner "Regístrate" en verde mediante código o XML,
        // aquí simplemente detectamos el clic.
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // El "Intent" es la intención de cambiar de pantalla
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RecuperarContrasenaActivity.class);
                startActivity(intent);
            }
        });
    }

    // Método simple de validación
    private boolean validarDatos(String email, String pass) {
        if (email.isEmpty()) {
            etEmail.setError("Introduce tu email");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido");
            return false;
        }
        if (pass.isEmpty()) {
            etPassword.setError("Introduce tu contraseña");
            return false;
        }
        return true;
    }
}