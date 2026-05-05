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

    private EditText etSearch;
    private EditText etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch   = findViewById(R.id.etSearch);
        etLocation = findViewById(R.id.etLocation);

        setupRecyclerView();
        setupSearch();
        setupLocationSearch();
        setupChips();
        BottomNavHelper.setup(this, "search");
        cargarPistas();
    }

    // ════════════════════════════════════════════════════════════
    //  Cargar pistas desde Supabase
    // ════════════════════════════════════════════════════════════
    private void cargarPistas() {
        SupabaseClient.getInstance().getPistasConImagenes(
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) { parsearPistas(body); }
                    @Override
                    public void onError(String error) {
                        allPistas.clear();
                        filtered.clear();
                        adapter.notifyDataSetChanged();
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

                String direccion = "";
                if (pista.has("instalaciones")
                        && !pista.isNull("instalaciones")) {
                    Object instObj = pista.get("instalaciones");
                    if (instObj instanceof JSONObject) {
                        direccion = ((JSONObject) instObj)
                                .optString("direccion", "");
                    }
                }

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
            allPistas.clear();
        }
        filtered.clear();
        filtered.addAll(allPistas);
        adapter.notifyDataSetChanged();
    }

    // ════════════════════════════════════════════════════════════
    //  RecyclerView
    // ════════════════════════════════════════════════════════════
    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvFacilities);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PistaAdapter(filtered);
        rv.setAdapter(adapter);
    }

    // ════════════════════════════════════════════════════════════
    //  Buscador por nombre
    // ════════════════════════════════════════════════════════════
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s){}
        });
    }

    // ════════════════════════════════════════════════════════════
    //  Buscador por ubicación
    // ════════════════════════════════════════════════════════════
    private void setupLocationSearch() {
        etLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s){}
        });
    }

    // ════════════════════════════════════════════════════════════
    //  Chips de deporte
    // ════════════════════════════════════════════════════════════
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
                applyFilters();
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

    // ════════════════════════════════════════════════════════════
    //  Aplicar todos los filtros juntos
    // ════════════════════════════════════════════════════════════
    private void applyFilters() {
        String queryNombre   = etSearch.getText().toString().trim().toLowerCase();
        String queryLocation = etLocation.getText().toString().trim().toLowerCase();

        filtered.clear();
        for (PistaItem p : allPistas) {

            // Filtro deporte
            boolean matchSport = activeChip.equals("Todos")
                    || p.tipoDeporte.equalsIgnoreCase(activeChip);

            // Filtro nombre
            boolean matchNombre = queryNombre.isEmpty()
                    || p.nombre.toLowerCase().contains(queryNombre)
                    || p.tipoDeporte.toLowerCase().contains(queryNombre);

            // Filtro ubicación
            boolean matchLocation = queryLocation.isEmpty()
                    || (p.direccion != null
                    && p.direccion.toLowerCase().contains(queryLocation));

            if (matchSport && matchNombre && matchLocation) filtered.add(p);
        }
        adapter.notifyDataSetChanged();
    }

    // ════════════════════════════════════════════════════════════
    //  Abrir pista
    // ════════════════════════════════════════════════════════════
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

    // ════════════════════════════════════════════════════════════
    //  Adapter
    // ════════════════════════════════════════════════════════════
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