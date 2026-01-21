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

public class RegisterActivity extends AppCompatActivity {

    // Variables para controlar tus campos
    private EditText etName, etSurname, etEmail, etZip, etPass;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ESTA LÍNEA ES LA CLAVE: Conecta este cerebro con tu diseño visual
        setContentView(R.layout.activity_register);

        // 1. Vincular las variables con los IDs de tu XML
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmailReg);
        etZip = findViewById(R.id.etZipCode);
        etPass = findViewById(R.id.etPassReg);
        btnRegister = findViewById(R.id.btnRegisterAction);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // 2. Programar el botón de Registrarse
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí validamos si los campos están vacíos
                if(etName.getText().toString().isEmpty() || etPass.getText().toString().isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Faltan datos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show();
                    // Aquí en el futuro guardarás los datos en la base de datos
                }
            }
        });

        // 3. Programar el botón "Ya tengo cuenta" (Volver al Login)
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Cierra el registro
            }
        });
    }
}