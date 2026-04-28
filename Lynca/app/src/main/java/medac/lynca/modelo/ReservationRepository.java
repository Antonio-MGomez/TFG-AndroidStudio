package medac.lynca.modelo;

import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {

    private static ReservationRepository instance;
    private final List<ReservationModel> activeReservations = new ArrayList<>();
    private final List<ReservationModel> pastReservations   = new ArrayList<>();

    private ReservationRepository() {
        // Datos de ejemplo actualizados con el nuevo constructor
        activeReservations.add(new ReservationModel(
                1L, "Pista de Tenis Exterior",
                "Mañana, 17:00-18:00", "pista_tenis", "Confirmada"));
        activeReservations.add(new ReservationModel(
                2L, "Pista Pádel 1",
                "Pasado mañana, 19:00-20:00", "pista_padel", "Confirmada"));

        pastReservations.add(new ReservationModel(
                3L, "Pista Fútbol Sala",
                "Ayer, 18:00-19:00", "pista_baloncesto_1", "Cancelada"));
    }

    public static ReservationRepository getInstance() {
        if (instance == null) instance = new ReservationRepository();
        return instance;
    }

    public List<ReservationModel> getActiveReservations() {
        return activeReservations;
    }

    public List<ReservationModel> getPastReservations() {
        return pastReservations;
    }

    public void cancelReservation(ReservationModel reservation) {
        activeReservations.remove(reservation);
        reservation = new ReservationModel(
                reservation.getId(),
                reservation.getFacilityName(),
                reservation.getTime(),
                reservation.getImageResource(),
                "Cancelada");
        pastReservations.add(reservation);
    }
}