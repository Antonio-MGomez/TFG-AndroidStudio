package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;
import medac.lynca.R;

public class PabellonInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_info);

        // 1. Configurar el carrusel de imágenes (ViewPager2)
        ViewPager2 viewPagerHeader = findViewById(R.id.viewPagerHeader);

        // Creamos la lista con tus nuevas fotos de baloncesto
        // ASEGÚRATE DE QUE TUS FOTOS SE LLAMEN ASÍ EN LA CARPETA DRAWABLE
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.pista_baloncesto_1);
        images.add(R.drawable.pista_baloncesto_2);

        // Asignamos el adaptador que creamos
        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPagerHeader.setAdapter(adapter);

        // 2. Configurar el botón de reservar
        Button btnReservar = findViewById(R.id.btnReservarInfo);
        btnReservar.setOnClickListener(v -> {
            // Navegar a la pantalla de selección de fecha y hora
            Intent intent = new Intent(PabellonInfoActivity.this, PabellonVictorActivity.class);
            startActivity(intent);
        });
    }
}