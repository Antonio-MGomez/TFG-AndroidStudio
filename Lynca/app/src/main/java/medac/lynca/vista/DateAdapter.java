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
import medac.lynca.modelo.DateModel;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<DateModel> dateList;
    // NUEVO: Listener para comunicar el clic
    private OnDateClickListener listener;

    // NUEVO: Interfaz que define la acción de clic
    public interface OnDateClickListener {
        void onDateClick(int position);
    }

    // Constructor actualizado para recibir el listener
    public DateAdapter(List<DateModel> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_victor, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateModel date = dateList.get(position);
        holder.tvDayName.setText(date.getDayName());
        holder.tvDayNumber.setText(String.valueOf(date.getDayNumber()));

        // Lógica visual de selección (Colores exactos del diseño)
        if (date.isSelected()) {
            // Fondo azul claro y texto azul oscuro
            holder.cardDate.setCardBackgroundColor(Color.parseColor("#E3E5FC"));
            holder.tvDayName.setTextColor(Color.parseColor("#1A237E"));
            holder.tvDayNumber.setTextColor(Color.parseColor("#1A237E"));
        } else {
            // Fondo blanco y texto negro
            holder.cardDate.setCardBackgroundColor(Color.WHITE);
            holder.tvDayName.setTextColor(Color.BLACK);
            holder.tvDayNumber.setTextColor(Color.BLACK);
        }

        // NUEVO: Detectar el clic en la tarjeta
        holder.itemView.setOnClickListener(v -> listener.onDateClick(position));
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;
        MaterialCardView cardDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            cardDate = itemView.findViewById(R.id.cardDate);
        }
    }
}