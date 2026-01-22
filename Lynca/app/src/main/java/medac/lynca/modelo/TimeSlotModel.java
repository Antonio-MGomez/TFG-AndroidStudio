package medac.lynca.modelo;

/**
 * Modelo para las franjas horarias.
 * Incluye estados para reserva y selección del usuario.
 */
public class TimeSlotModel {
    private String time;
    private String price;
    private boolean isReserved;
    private boolean isSelected; // Controla el estado visual de selección

    public TimeSlotModel(String time, String price, boolean isReserved) {
        this.time = time;
        this.price = price;
        this.isReserved = isReserved;
        this.isSelected = false;
    }

    public String getTime() { return time; }
    public String getPrice() { return price; }
    public boolean isReserved() { return isReserved; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}