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
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pabellon_info);

        ViewPager2 viewPagerHeader = findViewById(R.id.viewPagerHeader);
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.pista_baloncesto_1);
        images.add(R.drawable.pista_baloncesto_2);
        viewPagerHeader.setAdapter(new ImageSliderAdapter(images));

        Button btnReservar = findViewById(R.id.btnReservarInfo);
        btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(PabellonInfoActivity.this,
                    PabellonVictorActivity.class);
            startActivity(intent);
        });
    }
}