package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;

public class HomeActivity extends AppCompatActivity {

    private final List<PopularItem> popularItems = new ArrayList<>();
    private PopularAdapter popularAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String nombre = SessionManager.getInstance(this).getNombre();
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(nombre + "!");

        findViewById(R.id.btnBookNow).setOnClickListener(v -> {
            if (!popularItems.isEmpty()) abrirPista(popularItems.get(0));
        });
        findViewById(R.id.cardFeatured).setOnClickListener(v -> {
            if (!popularItems.isEmpty()) abrirPista(popularItems.get(0));
        });

        findViewById(R.id.btnSearch).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        RecyclerView rv = findViewById(R.id.rvPopular);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
        popularAdapter = new PopularAdapter(popularItems);
        rv.setAdapter(popularAdapter);

        BottomNavHelper.setup(this, "home");
        cargarPistas();
    }

    private void cargarPistas() {
        SupabaseClient.getInstance().getPistasConImagenes(
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        try {
                            JSONArray arr = new JSONArray(body);
                            popularItems.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject p   = arr.getJSONObject(i);
                                String id      = p.getString("id");
                                String nombre  = p.optString("nombre", "Pista");
                                String deporte = p.optString("tipo_deporte", "");
                                double precio  = p.optDouble("precio_hora", 0);
                                String desc    = p.optString("descripcion", "");

                                String imgUrl = "";
                                if (p.has("imagenes_pista")) {
                                    JSONArray imgs = p.getJSONArray("imagenes_pista");
                                    for (int j = 0; j < imgs.length(); j++) {
                                        JSONObject img = imgs.getJSONObject(j);
                                        if (img.optBoolean("es_principal", false)) {
                                            imgUrl = img.optString("url_imagen","");
                                            break;
                                        }
                                    }
                                    if (imgUrl.isEmpty() && imgs.length() > 0) {
                                        imgUrl = imgs.getJSONObject(0)
                                                .optString("url_imagen","");
                                    }
                                }
                                popularItems.add(new PopularItem(
                                        id, nombre, deporte,
                                        (int) precio, imgUrl, desc));
                            }
                            popularAdapter.notifyDataSetChanged();

                            if (!popularItems.isEmpty()) {
                                ImageView imgFeatured = findViewById(R.id.imgFeatured);
                                if (imgFeatured != null) {
                                    String url = popularItems.get(0).imagenUrl;
                                    if (url != null && !url.isEmpty()) {
                                        Glide.with(HomeActivity.this)
                                                .load(url)
                                                .placeholder(R.drawable.pista_baloncesto_1)
                                                .centerCrop()
                                                .into(imgFeatured);
                                    }
                                }
                            }
                        } catch (Exception e) { cargarLocal(); }
                    }

                    @Override
                    public void onError(String error) { cargarLocal(); }
                });
    }

    private void cargarLocal() {
        popularItems.clear();
        popularItems.add(new PopularItem(
                "09b0e24c-79db-482a-8cf2-2c33a3e1dddf",
                "Pista Tenis", "Tenis", 12, "", ""));
        popularItems.add(new PopularItem(
                "99a95eae-e5cb-49f5-8475-43a659a1fd4a",
                "Pista Padel 1", "Padel", 10, "", ""));
        popularItems.add(new PopularItem(
                "eb1707df-023f-4353-ad4c-3a6ebb27f0de",
                "Pista Futbol Sala", "Futbol Sala", 10, "", ""));
        popularAdapter.notifyDataSetChanged();
    }

    private void abrirPista(PopularItem item) {
        Intent intent = new Intent(this, PistaInfoActivity.class);
        intent.putExtra("pista_id",     item.id);
        intent.putExtra("pista_nombre", item.nombre);
        intent.putExtra("tipo_deporte", item.deporte);
        intent.putExtra("precio_hora",  String.valueOf(item.precio));
        intent.putExtra("imagen_res",   getImagenLocal(item.deporte));
        intent.putExtra("imagen_url",   item.imagenUrl);
        intent.putExtra("descripcion",  item.descripcion);
        startActivity(intent);
    }

    private int getImagenLocal(String deporte) {
        if (deporte == null) return R.drawable.pista_baloncesto_1;
        switch (deporte) {
            case "Tenis":  return R.drawable.pista_tenis;
            case "Padel":
            case "Pádel":  return R.drawable.pista_padel;
            default:       return R.drawable.pista_baloncesto_1;
        }
    }

    static class PopularItem {
        String id, nombre, deporte, imagenUrl, descripcion;
        int    precio;
        float  rating = 4.8f;

        PopularItem(String id, String nombre, String deporte,
                    int precio, String imagenUrl, String descripcion) {
            this.id          = id;
            this.nombre      = nombre;
            this.deporte     = deporte;
            this.precio      = precio;
            this.imagenUrl   = imagenUrl;
            this.descripcion = descripcion;
        }
    }

    class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.VH> {
        private final List<PopularItem> list;
        PopularAdapter(List<PopularItem> list) { this.list = list; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_popular, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            PopularItem item = list.get(position);
            holder.tvName.setText(item.nombre);
            holder.tvDistance.setText(item.deporte);
            holder.tvRating.setText(String.valueOf(item.rating));

            if (item.imagenUrl != null && !item.imagenUrl.isEmpty()) {
                Glide.with(holder.img.getContext())
                        .load(item.imagenUrl)
                        .placeholder(getImagenLocal(item.deporte))
                        .error(getImagenLocal(item.deporte))
                        .centerCrop()
                        .into(holder.img);
            } else {
                holder.img.setImageResource(getImagenLocal(item.deporte));
            }
            holder.itemView.setOnClickListener(v -> abrirPista(item));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView  tvName, tvDistance, tvRating;
            VH(View v) {
                super(v);
                img        = v.findViewById(R.id.imgPopular);
                tvName     = v.findViewById(R.id.tvPopularName);
                tvDistance = v.findViewById(R.id.tvPopularDistance);
                tvRating   = v.findViewById(R.id.tvPopularRating);
            }
        }
    }
}