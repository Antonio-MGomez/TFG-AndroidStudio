package medac.lynca.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import medac.lynca.R;
import medac.lynca.modelo.ReservationModel;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private List<ReservationModel> list;
    private OnCancelClickListener listener;

    public interface OnCancelClickListener {
        void onCancelClick(int position);
    }

    public ReservationAdapter(List<ReservationModel> list, OnCancelClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationModel res = list.get(position);
        holder.tvTitle.setText(res.getTitle());
        holder.tvTime.setText(res.getTime());
        holder.tvStatus.setText("● " + res.getStatus());

        // OCULTAR BOTÓN CANCELAR SI ES PISTA PASADA
        if (res.getStatus().equalsIgnoreCase("Finalizada")) {
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        // Cargar imagen de drawable
        int imageId = holder.itemView.getContext().getResources().getIdentifier(
                res.getImageResource(), "drawable", holder.itemView.getContext().getPackageName());

        if (imageId != 0) {
            holder.img.setImageResource(imageId);
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnCancel.setOnClickListener(v -> listener.onCancelClick(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvStatus;
        ImageView img;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvResTitle);
            tvTime = itemView.findViewById(R.id.tvResTime);
            tvStatus = itemView.findViewById(R.id.tvResStatus);
            img = itemView.findViewById(R.id.imgReserva);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}