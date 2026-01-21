package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import medac.lynca.R;

public class PabellonInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_info);

        Button btn = findViewById(R.id.btnReservarInfo);

        btn.setOnClickListener(v -> {
            // NAVEGACIÓN: De Info a Selección de Fecha
            Intent intent = new Intent(this, PabellonVictorActivity.class);
            startActivity(intent);
        });
    }
}