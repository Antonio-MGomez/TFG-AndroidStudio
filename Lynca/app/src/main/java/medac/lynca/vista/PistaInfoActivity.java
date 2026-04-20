package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.SupabaseClient;
import medac.lynca.modelo.SupabaseConfig;

public class PistaInfoActivity extends AppCompatActivity {

    private String pistaId, pistaNombre, tipoDeporte, precioHora;
    private int    imagenRes;
    private String imagenUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pista_info);

        pistaId     = getIntent().getStringExtra("pista_id");
        pistaNombre = getIntent().getStringExtra("pista_nombre");
        tipoDeporte = getIntent().getStringExtra("tipo_deporte");
        precioHora  = getIntent().getStringExtra("precio_hora");
        imagenRes   = getIntent().getIntExtra("imagen_res",
                R.drawable.pista_baloncesto_1);
        imagenUrl   = getIntent().getStringExtra("imagen_url");

        // Nombre y precio
        TextView tvNombre = findViewById(R.id.tvPistaNombre);
        TextView tvPrecio = findViewById(R.id.tvPrecioInfo);
        if (tvNombre != null) tvNombre.setText(pistaNombre);
        if (tvPrecio != null) tvPrecio.setText("€" + precioHora + " / hora");

        // Descripción
        String descripcion = getIntent().getStringExtra("descripcion");
        TextView tvDesc = findViewById(R.id.tvDescripcion);
        if (tvDesc != null) {
            if (descripcion != null && !descripcion.isEmpty()) {
                tvDesc.setText(descripcion);
            } else {
                tvDesc.setText(getDescripcion(tipoDeporte));
            }
        }

        // Tags
        setTags(tipoDeporte,
                findViewById(R.id.tvTag1),
                findViewById(R.id.tvTag2),
                findViewById(R.id.tvTag3));

        // Reseñas
        setReviews(tipoDeporte);

        // Cargar imágenes
        cargarImagenes();

        // Botón Reservar
        Button btnReservar = findViewById(R.id.btnReservarInfo);
        btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(this, PistaReservaActivity.class);
            intent.putExtra("pista_id",     pistaId);
            intent.putExtra("pista_nombre", pistaNombre);
            intent.putExtra("tipo_deporte", tipoDeporte);
            intent.putExtra("precio_hora",  precioHora);
            intent.putExtra("imagen_res",   imagenRes);
            intent.putExtra("imagen_url",   imagenUrl);
            startActivity(intent);
        });

        BottomNavHelper.setup(this, "search");
    }

    // ════════════════════════════════════════════════════════════
    //  Imágenes desde Supabase
    // ════════════════════════════════════════════════════════════
    private void cargarImagenes() {
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(SupabaseConfig.REST_URL
                                + "/imagenes_pista?pista_id=eq." + pistaId)
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .get().build();

                okhttp3.Response response = client.newCall(req).execute();
                String body = response.body() != null
                        ? response.body().string() : "[]";

                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(body);
                        List<String> urls = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            String u = arr.getJSONObject(i)
                                    .optString("url_imagen", "");
                            if (!u.isEmpty()) urls.add(u);
                        }
                        ViewPager2 vp = findViewById(R.id.viewPagerHeader);
                        if (!urls.isEmpty()) {
                            vp.setAdapter(new ImageSliderAdapterUrl(
                                    urls, imagenRes));
                        } else {
                            List<Integer> imgs = new ArrayList<>();
                            imgs.add(imagenRes);
                            vp.setAdapter(new ImageSliderAdapter(imgs));
                        }
                    } catch (Exception e) { setImagenLocal(); }
                });
            } catch (Exception e) {
                runOnUiThread(this::setImagenLocal);
            }
        }).start();
    }

    private void setImagenLocal() {
        List<Integer> imgs = new ArrayList<>();
        imgs.add(imagenRes);
        ViewPager2 vp = findViewById(R.id.viewPagerHeader);
        if (vp != null) vp.setAdapter(new ImageSliderAdapter(imgs));
    }

    // ════════════════════════════════════════════════════════════
    //  Reseñas por deporte
    // ════════════════════════════════════════════════════════════
    private void setReviews(String deporte) {
        ImageView img1  = findViewById(R.id.imgUser1);
        TextView  user1 = findViewById(R.id.tvUser1);
        TextView  text1 = findViewById(R.id.tvReview1Text);
        ImageView img2  = findViewById(R.id.imgUser2);
        TextView  user2 = findViewById(R.id.tvUser2);

        if (deporte == null) return;
        switch (deporte) {
            case "Tenis":
                if (img1  != null) img1.setImageResource(R.drawable.foto_review_1);
                if (user1 != null) user1.setText("TenisPro_Alicia");
                if (text1 != null) text1.setText(
                        "Pista perfecta para entrenar. " +
                                "La arcilla está en un estado impecable.");
                if (img2  != null) img2.setImageResource(R.drawable.foto_review_2);
                if (user2 != null) user2.setText("RafaSmash88");
                break;
            case "Padel":
            case "Pádel":
                if (img1  != null) img1.setImageResource(R.drawable.foto_review_3);
                if (user1 != null) user1.setText("PadelAddicted");
                if (text1 != null) text1.setText(
                        "Cristales limpísimos y muy buena iluminación. " +
                                "La mejor pista de pádel de la zona.");
                if (img2  != null) img2.setImageResource(R.drawable.foto_review_4);
                if (user2 != null) user2.setText("LauraSmash");
                break;
            case "Futbol Sala":
            case "Fútbol Sala":
                if (img1  != null) img1.setImageResource(R.drawable.foto_review_2);
                if (user1 != null) user1.setText("GoalMachine7");
                if (text1 != null) text1.setText(
                        "El parquet agarra genial. " +
                                "Vestuarios amplios y muy limpios.");
                if (img2  != null) img2.setImageResource(R.drawable.foto_review_1);
                if (user2 != null) user2.setText("FutbolSala_Pro");
                break;
            default:
                if (img1  != null) img1.setImageResource(R.drawable.foto_review_3);
                if (user1 != null) user1.setText("SportLover");
                if (text1 != null) text1.setText(
                        "Muy buena instalación. Totalmente recomendable.");
                if (img2  != null) img2.setImageResource(R.drawable.foto_review_4);
                if (user2 != null) user2.setText("ActiveUser22");
                break;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Tags por deporte
    // ════════════════════════════════════════════════════════════
    private void setTags(String deporte, TextView t1, TextView t2, TextView t3) {
        if (deporte == null) return;
        switch (deporte) {
            case "Tenis":
                if (t1 != null) t1.setText("🎾\nTenis");
                if (t2 != null) t2.setText("💡\nIluminación");
                if (t3 != null) t3.setText("🚿\nVestuarios");
                break;
            case "Padel":
            case "Pádel":
                if (t1 != null) t1.setText("🏓\nPádel");
                if (t2 != null) t2.setText("🏗️\nCubierta");
                if (t3 != null) t3.setText("🚿\nVestuarios");
                break;
            case "Futbol Sala":
            case "Fútbol Sala":
                if (t1 != null) t1.setText("⚽\nFútbol Sala");
                if (t2 != null) t2.setText("🌤️\nAbierta");
                if (t3 != null) t3.setText("🚿\nVestuarios");
                break;
            default:
                if (t1 != null) t1.setText("🏟️\n" + deporte);
                if (t2 != null) t2.setText("💡\nIluminación");
                if (t3 != null) t3.setText("🚿\nVestuarios");
                break;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Descripción por deporte
    // ════════════════════════════════════════════════════════════
    private String getDescripcion(String deporte) {
        if (deporte == null) return "Instalación deportiva de alta calidad.";
        switch (deporte) {
            case "Tenis":
                return "Pista de tenis exterior con superficie de arcilla roja. " +
                        "Iluminación nocturna disponible. " +
                        "Vestuarios y duchas incluidos en el precio.";
            case "Padel":
            case "Pádel":
                return "Pista de pádel cubierta de última generación con cristal " +
                        "panorámico. Alquiler de material disponible. " +
                        "Climatizada para jugar todo el año.";
            case "Futbol Sala":
            case "Fútbol Sala":
                return "Campo de fútbol sala con suelo de parquet flotante. " +
                        "Porterías homologadas y marcador electrónico. " +
                        "Ideal para partidos y entrenamientos.";
            default:
                return "Instalación deportiva de alta calidad disponible " +
                        "para reserva. Vestuarios y duchas incluidos.";
        }
    }
}