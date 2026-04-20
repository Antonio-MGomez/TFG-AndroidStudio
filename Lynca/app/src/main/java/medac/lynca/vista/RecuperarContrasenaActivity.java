package medac.lynca.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import medac.lynca.R;

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button   btnSend;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperarcontrasena);

        etEmail      = findViewById(R.id.etEmailForgot);
        btnSend      = findViewById(R.id.btnSendLink);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError("Por favor, introduce tu correo");
                } else {
                    Toast.makeText(RecuperarContrasenaActivity.this,
                            "Enlace enviado a " + email,
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }
}