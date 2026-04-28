package medac.lynca.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import medac.lynca.R;
import medac.lynca.modelo.ReservationModel;

public class ReservationAdapter
        extends RecyclerView.Adapter<ReservationAdapter.VH> {

    public interface OnCancelListener  { void onCancel(int position); }
    public interface OnDetailListener  { void onDetail(int position); }

    private final List<ReservationModel> list;
    private final OnCancelListener       cancelListener;
    private final OnDetailListener       detailListener;

    public ReservationAdapter(List<ReservationModel> list,
                              OnCancelListener cancelListener,
                              OnDetailListener detailListener) {
        this.list           = list;
        this.cancelListener = cancelListener;
        this.detailListener = detailListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ReservationModel res = list.get(position);

        holder.tvName.setText(res.getFacilityName());
        holder.tvTime.setText(res.getTime());
        holder.tvStatus.setText(res.getStatus());

        // Cargar imagen desde URL o fallback local
        if (res.getImagenUrl() != null && !res.getImagenUrl().isEmpty()) {
            Glide.with(holder.img.getContext())
                    .load(res.getImagenUrl())
                    .placeholder(getImagenRes(holder, res))
                    .error(getImagenRes(holder, res))
                    .centerCrop()
                    .into(holder.img);
        } else {
            holder.img.setImageResource(getImagenRes(holder, res));
        }

        // Botón cancelar
        if (holder.btnCancel != null) {
            holder.btnCancel.setOnClickListener(v -> {
                if (cancelListener != null)
                    cancelListener.onCancel(holder.getAdapterPosition());
            });
        }

        // Botón detalles
        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> {
                if (detailListener != null)
                    detailListener.onDetail(holder.getAdapterPosition());
            });
        }
    }

    private int getImagenRes(VH holder, ReservationModel res) {
        String deporte = res.getTipoDeporte();
        if (deporte == null) return R.drawable.pista_baloncesto_1;
        switch (deporte) {
            case "Tenis":  return R.drawable.pista_tenis;
            case "Pádel":
            case "Padel":  return R.drawable.pista_padel;
            default:       return R.drawable.pista_baloncesto_1;
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView  tvName, tvTime, tvStatus;
        Button    btnCancel, btnDetail;

        VH(@NonNull View v) {
            super(v);
            img      = v.findViewById(R.id.imgReservation);
            tvName   = v.findViewById(R.id.tvReservationName);
            tvTime   = v.findViewById(R.id.tvReservationTime);
            tvStatus = v.findViewById(R.id.tvReservationStatus);
            btnCancel = v.findViewById(R.id.btnCancelReservation);
            btnDetail = v.findViewById(R.id.btnDetailReservation);
        }
    }
}