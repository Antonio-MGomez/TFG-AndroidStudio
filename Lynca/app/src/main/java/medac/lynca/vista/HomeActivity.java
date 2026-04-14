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

import java.util.ArrayList;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String nombre = SessionManager.getInstance(this).getNombre();
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(nombre + "!");

        findViewById(R.id.btnBookNow).setOnClickListener(v ->
                abrirPista("09b0e24c-79db-482a-8cf2-2c33a3e1dddf",
                        "Pista Tenis", "Tenis", "12", R.drawable.pista_tenis));

        findViewById(R.id.cardFeatured).setOnClickListener(v ->
                abrirPista("09b0e24c-79db-482a-8cf2-2c33a3e1dddf",
                        "Pista Tenis", "Tenis", "12", R.drawable.pista_tenis));

        findViewById(R.id.btnSearch).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        setupPopularList();
        BottomNavHelper.setup(this, "home");
    }

    private void abrirPista(String pistaId, String nombre,
                            String deporte, String precio, int imagenRes) {
        Intent intent = new Intent(this, PistaInfoActivity.class);
        intent.putExtra("pista_id",     pistaId);
        intent.putExtra("pista_nombre", nombre);
        intent.putExtra("tipo_deporte", deporte);
        intent.putExtra("precio_hora",  precio);
        intent.putExtra("imagen_res",   imagenRes);
        startActivity(intent);
    }

    private void setupPopularList() {
        List<PopularItem> items = new ArrayList<>();
        items.add(new PopularItem("Pista Tenis", "2.5 km", 4.7f,
                R.drawable.pista_tenis,
                "09b0e24c-79db-482a-8cf2-2c33a3e1dddf", "Tenis", "12"));
        items.add(new PopularItem("Pista Padel 1", "1.8 km", 4.9f,
                R.drawable.pista_padel,
                "99a95eae-e5cb-49f5-8475-43a659a1fd4a", "Padel", "10"));
        items.add(new PopularItem("Pista Futbol Sala", "3.1 km", 4.6f,
                R.drawable.pista_baloncesto_1,
                "eb1707df-023f-4353-ad4c-3a6ebb27f0de", "Futbol Sala", "10"));

        RecyclerView rv = findViewById(R.id.rvPopular);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(new PopularAdapter(items));
    }

    static class PopularItem {
        String name, distance, pistaId, deporte, precio;
        float  rating;
        int    imageRes;

        PopularItem(String name, String distance, float rating,
                    int imageRes, String pistaId, String deporte, String precio) {
            this.name     = name;
            this.distance = distance;
            this.rating   = rating;
            this.imageRes = imageRes;
            this.pistaId  = pistaId;
            this.deporte  = deporte;
            this.precio   = precio;
        }
    }

    class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.VH> {

        private final List<PopularItem> list;

        PopularAdapter(List<PopularItem> list) {
            this.list = list;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_popular, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            PopularItem item = list.get(position);
            holder.tvName.setText(item.name);
            holder.tvDistance.setText(item.distance);
            holder.tvRating.setText(String.valueOf(item.rating));
            holder.img.setImageResource(item.imageRes);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PistaInfoActivity.class);
                intent.putExtra("pista_id",     item.pistaId);
                intent.putExtra("pista_nombre", item.name);
                intent.putExtra("tipo_deporte", item.deporte);
                intent.putExtra("precio_hora",  item.precio);
                intent.putExtra("imagen_res",   item.imageRes);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

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