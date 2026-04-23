package medac.lynca.vista;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.SupabaseClient;

public class SearchActivity extends AppCompatActivity {

    static class PistaItem {
        String id, nombre, tipoDeporte, imagenUrl, descripcion, direccion;
        double precioHora;

        PistaItem(String id, String nombre, String tipoDeporte,
                  double precioHora, String imagenUrl,
                  String descripcion, String direccion) {
            this.id          = id;
            this.nombre      = nombre;
            this.tipoDeporte = tipoDeporte;
            this.precioHora  = precioHora;
            this.imagenUrl   = imagenUrl;
            this.descripcion = descripcion;
            this.direccion   = direccion;
        }
    }

    private final List<PistaItem> allPistas = new ArrayList<>();
    private final List<PistaItem> filtered  = new ArrayList<>();
    private PistaAdapter adapter;
    private String activeChip = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupRecyclerView();
        setupSearch();
        setupChips();
        BottomNavHelper.setup(this, "search");
        cargarPistas();
    }

    private void cargarPistas() {
        SupabaseClient.getInstance().getPistasConImagenes(
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) { parsearPistas(body); }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(SearchActivity.this,
                                "Error cargando pistas", Toast.LENGTH_SHORT).show();
                        cargarPistasLocal();
                    }
                });
    }

    private void parsearPistas(String body) {
        allPistas.clear();
        try {
            JSONArray arr = new JSONArray(body);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject pista = arr.getJSONObject(i);
                String id      = pista.getString("id");
                String nombre  = pista.optString("nombre", "Pista");
                String deporte = pista.optString("tipo_deporte", "");
                double precio  = pista.optDouble("precio_hora", 0);
                String desc    = pista.optString("descripcion", "");

                // Dirección desde instalaciones
                String direccion = "";
                if (pista.has("instalaciones")
                        && !pista.isNull("instalaciones")) {
                    Object instObj = pista.get("instalaciones");
                    if (instObj instanceof JSONObject) {
                        direccion = ((JSONObject) instObj)
                                .optString("direccion", "");
                    }
                }

                // Imagen principal
                String imagenUrl = "";
                if (pista.has("imagenes_pista")) {
                    JSONArray imgs = pista.getJSONArray("imagenes_pista");
                    for (int j = 0; j < imgs.length(); j++) {
                        JSONObject img = imgs.getJSONObject(j);
                        if (img.optBoolean("es_principal", false)) {
                            imagenUrl = img.optString("url_imagen", "");
                            break;
                        }
                    }
                    if (imagenUrl.isEmpty() && imgs.length() > 0) {
                        imagenUrl = imgs.getJSONObject(0)
                                .optString("url_imagen", "");
                    }
                }

                allPistas.add(new PistaItem(
                        id, nombre, deporte, precio,
                        imagenUrl, desc, direccion));
            }
        } catch (Exception e) {
            cargarPistasLocal();
            return;
        }
        filtered.clear();
        filtered.addAll(allPistas);
        adapter.notifyDataSetChanged();
    }

    private void cargarPistasLocal() {
        allPistas.clear();
        allPistas.add(new PistaItem(
                "09b0e24c-79db-482a-8cf2-2c33a3e1dddf",
                "Pista Tenis", "Tenis", 12, "", "", ""));
        allPistas.add(new PistaItem(
                "99a95eae-e5cb-49f5-8475-43a659a1fd4a",
                "Pista Pádel 1", "Pádel", 10, "", "", ""));
        allPistas.add(new PistaItem(
                "d3304f3d-c511-41fd-a65f-027566151951",
                "Pista Pádel 2", "Pádel", 8, "", "", ""));
        allPistas.add(new PistaItem(
                "eb1707df-023f-4353-ad4c-3a6ebb27f0de",
                "Pista Fútbol Sala", "Fútbol Sala", 10, "", "", ""));
        filtered.clear();
        filtered.addAll(allPistas);
        adapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvFacilities);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PistaAdapter(filtered);
        rv.setAdapter(adapter);
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){
                applyFilters(s.toString());
            }
            @Override public void afterTextChanged(Editable s){}
        });
    }

    private void setupChips() {
        int[] chipIds = {
                R.id.chipTodos, R.id.chipTenis, R.id.chipPadel,
                R.id.chipFutbol, R.id.chipBaloncesto
        };
        String[] deportes = {
                "Todos","Tenis","Pádel","Fútbol Sala","Baloncesto"
        };

        for (int i = 0; i < chipIds.length; i++) {
            TextView chip = findViewById(chipIds[i]);
            if (chip == null) continue;
            final String deporte    = deportes[i];
            final int    selectedId = chipIds[i];
            chip.setOnClickListener(v -> {
                activeChip = deporte;
                updateChipStyles(chipIds, selectedId);
                applyFilters(((EditText) findViewById(R.id.etSearch))
                        .getText().toString());
            });
        }
    }

    private void updateChipStyles(int[] chipIds, int selectedId) {
        for (int id : chipIds) {
            TextView chip = findViewById(id);
            if (chip == null) continue;
            if (id == selectedId) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getColor(android.R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_normal);
                chip.setTextColor(getColor(android.R.color.black));
            }
        }
    }

    private void applyFilters(String query) {
        filtered.clear();
        for (PistaItem p : allPistas) {
            boolean matchSport = activeChip.equals("Todos")
                    || p.tipoDeporte.equalsIgnoreCase(activeChip);
            boolean matchQuery = query.isEmpty()
                    || p.nombre.toLowerCase().contains(query.toLowerCase())
                    || p.tipoDeporte.toLowerCase().contains(query.toLowerCase())
                    || p.direccion.toLowerCase().contains(query.toLowerCase());
            if (matchSport && matchQuery) filtered.add(p);
        }
        adapter.notifyDataSetChanged();
    }

    private void abrirPista(PistaItem p) {
        Intent intent = new Intent(this, PistaInfoActivity.class);
        intent.putExtra("pista_id",     p.id);
        intent.putExtra("pista_nombre", p.nombre);
        intent.putExtra("tipo_deporte", p.tipoDeporte);
        intent.putExtra("precio_hora",  String.valueOf((int) p.precioHora));
        intent.putExtra("imagen_url",   p.imagenUrl);
        intent.putExtra("descripcion",  p.descripcion);
        intent.putExtra("direccion",    p.direccion);
        intent.putExtra("imagen_res",   getImagenLocal(p.tipoDeporte));
        startActivity(intent);
    }

    private int getImagenLocal(String deporte) {
        if (deporte == null) return R.drawable.pista_baloncesto_1;
        switch (deporte) {
            case "Tenis":  return R.drawable.pista_tenis;
            case "Pádel":
            case "Padel":  return R.drawable.pista_padel;
            default:       return R.drawable.pista_baloncesto_1;
        }
    }

    class PistaAdapter extends RecyclerView.Adapter<PistaAdapter.VH> {

        private final List<PistaItem> list;
        PistaAdapter(List<PistaItem> list) { this.list = list; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_facility, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            PistaItem p = list.get(position);
            holder.tvName.setText(p.nombre);
            holder.tvCity.setText(p.direccion != null && !p.direccion.isEmpty()
                    ? p.direccion : p.tipoDeporte);
            holder.tvPrice.setText((int) p.precioHora + "€");

            if (p.imagenUrl != null && !p.imagenUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(p.imagenUrl)
                        .placeholder(getImagenLocal(p.tipoDeporte))
                        .error(getImagenLocal(p.tipoDeporte))
                        .centerCrop()
                        .into(holder.img);
            } else {
                holder.img.setImageResource(getImagenLocal(p.tipoDeporte));
            }

            holder.itemView.setOnClickListener(v -> abrirPista(p));
            holder.btnReservar.setOnClickListener(v -> abrirPista(p));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView  tvName, tvCity, tvPrice;
            android.widget.Button btnReservar;

            VH(View v) {
                super(v);
                img         = v.findViewById(R.id.imgFacility);
                tvName      = v.findViewById(R.id.tvFacilityName);
                tvCity      = v.findViewById(R.id.tvFacilityCity);
                tvPrice     = v.findViewById(R.id.tvFacilityPrice);
                btnReservar = v.findViewById(R.id.btnReservarItem);
            }
        }
    }
}