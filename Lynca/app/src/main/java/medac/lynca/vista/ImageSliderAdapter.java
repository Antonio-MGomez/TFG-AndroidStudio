package medac.lynca.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import medac.lynca.R;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private List<Integer> imagesList;

    // Constructor: recibe la lista de IDs de las imágenes (R.drawable.foto1, etc.)
    public ImageSliderAdapter(List<Integer> imagesList) {
        this.imagesList = imagesList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Inflamos" el diseño de una imagen individual que creamos en el Paso 1
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider_image, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        // Ponemos la imagen correspondiente en el ImageView
        holder.imageView.setImageResource(imagesList.get(position));
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    // Clase interna para manejar la vista de cada item
    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSliderItem);
        }
    }
}