package medac.lynca.modelo;

public class ReservationModel {
    private String title, time, imageResource, status;

    public ReservationModel(String title, String time, String imageResource, String status) {
        this.title = title;
        this.time = time;
        this.imageResource = imageResource; // Nombre de la foto en drawable
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getImageResource() { return imageResource; }
    public String getStatus() { return status; }
}