package medac.lynca.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import medac.lynca.R; // Si te da error la R, importa tu paquete R

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperarcontrasena);

        // 1. Vincular vistas
        etEmail = findViewById(R.id.etEmailForgot);
        btnSend = findViewById(R.id.btnSendLink);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // 2. Acción botón Enviar
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    etEmail.setError("Por favor, introduce tu correo");
                } else {
                    // Aquí conectaríamos con Firebase más adelante
                    Toast.makeText(RecuperarContrasenaActivity.this, "Enlace de recuperación enviado a " + email, Toast.LENGTH_LONG).show();
                    finish(); // Cierra esta pantalla y vuelve al login
                }
            }
        });

        // 3. Acción Volver al Login
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Simplemente cerramos esta actividad para volver a la anterior (Login)
            }
        });
    }
}

