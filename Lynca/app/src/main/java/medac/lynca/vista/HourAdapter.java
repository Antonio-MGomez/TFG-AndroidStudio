package medac.lynca.vista;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import medac.lynca.R;
import medac.lynca.modelo.TimeSlotModel;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    private List<TimeSlotModel> hourList;
    private OnHourClickListener listener;

    public interface OnHourClickListener {
        void onHourClick(int position);
    }

    public HourAdapter(List<TimeSlotModel> hourList, OnHourClickListener listener) {
        this.hourList = hourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hour_victor, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        TimeSlotModel slot = hourList.get(position);
        holder.tvHour.setText(slot.getTime());

        if (slot.isReserved()) {
            holder.tvPrice.setText("Reservada");
            holder.tvPrice.setTextColor(Color.parseColor("#F44336"));
            holder.cardHour.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.cardHour.setStrokeColor(Color.TRANSPARENT);
            holder.itemView.setClickable(false);
        } else {
            holder.tvPrice.setText(slot.getPrice());
            holder.tvPrice.setTextColor(Color.parseColor("#757575"));

            if (slot.isSelected()) {
                holder.cardHour.setCardBackgroundColor(Color.parseColor("#E3E5FC"));
                holder.cardHour.setStrokeColor(Color.parseColor("#1A237E"));
                holder.tvHour.setTextColor(Color.parseColor("#1A237E"));
            } else {
                holder.cardHour.setCardBackgroundColor(Color.WHITE);
                holder.cardHour.setStrokeColor(Color.parseColor("#DDDDDD"));
                holder.tvHour.setTextColor(Color.BLACK);
            }
            holder.itemView.setOnClickListener(v -> listener.onHourClick(position));
        }
    }

    @Override
    public int getItemCount() { return hourList.size(); }

    static class HourViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour, tvPrice;
        MaterialCardView cardHour;
        public HourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            cardHour = itemView.findViewById(R.id.cardHour);
        }
    }
}