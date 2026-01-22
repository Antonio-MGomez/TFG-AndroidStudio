package medac.lynca.modelo;

import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {
    private static ReservationRepository instance;
    private List<ReservationModel> activeReservations;
    private List<ReservationModel> pastReservations;

    private ReservationRepository() {
        activeReservations = new ArrayList<>();
        pastReservations = new ArrayList<>();

        // RESERVADAS (Activas)
        activeReservations.add(new ReservationModel("Pista de Tenis Exterior", "Mañana, 17:00-18:00", "pista_tenis", "Confirmada"));
        activeReservations.add(new ReservationModel("Piscina Ruben Plaza", "Hoy, 21:00-22:00", "piscina_ejemplo", "Confirmada"));

        // PASADAS (Se oculta cancelar)
        pastReservations.add(new ReservationModel("Pista Pádel 2", "Ayer, 20:00-21:00", "pista_padel", "Finalizada"));
        pastReservations.add(new ReservationModel("Pabellón número cinco", "Lunes, 19:00-20:00", "pista_baloncesto_1", "Finalizada"));
    }

    public static ReservationRepository getInstance() {
        if (instance == null) instance = new ReservationRepository();
        return instance;
    }

    public List<ReservationModel> getActive() { return activeReservations; }
    public List<ReservationModel> getPast() { return pastReservations; }
    public void addReservation(ReservationModel res) { activeReservations.add(0, res); }
    public void removeReservation(int position) { activeReservations.remove(position); }
}