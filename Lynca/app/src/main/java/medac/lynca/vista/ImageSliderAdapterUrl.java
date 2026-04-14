package medac.lynca.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import medac.lynca.R;

public class ImageSliderAdapterUrl
        extends RecyclerView.Adapter<ImageSliderAdapterUrl.SliderVH> {

    private final List<String> urls;
    private final int fallbackRes;

    public ImageSliderAdapterUrl(List<String> urls, int fallbackRes) {
        this.urls        = urls;
        this.fallbackRes = fallbackRes;
    }

    @NonNull
    @Override
    public SliderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider_image, parent, false);
        return new SliderVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderVH holder, int position) {
        String url = urls.get(position);
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(url)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(fallbackRes);
        }
    }

    @Override
    public int getItemCount() { return urls.size(); }

    static class SliderVH extends RecyclerView.ViewHolder {
        ImageView imageView;
        SliderVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSliderItem);
        }
    }
}