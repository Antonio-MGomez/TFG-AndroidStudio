package medac.lynca.modelo;

public class ReservationModel {
    private long   id;
    private String title;
    private String time;
    private String imageResource;
    private String status;

    // Constructor SIN id (para compatibilidad con el código anterior)
    public ReservationModel(String title, String time, String imageResource, String status) {
        this.id            = 0;
        this.title         = title;
        this.time          = time;
        this.imageResource = imageResource;
        this.status        = status;
    }

    // Constructor CON id (para datos que vienen de Supabase)
    public ReservationModel(long id, String title, String time,
                            String imageResource, String status) {
        this.id            = id;
        this.title         = title;
        this.time          = time;
        this.imageResource = imageResource;
        this.status        = status;
    }

    public long   getId()            { return id; }
    public String getTitle()         { return title; }
    public String getTime()          { return time; }
    public String getImageResource() { return imageResource; }
    public String getStatus()        { return status; }
    public void   setStatus(String s){ this.status = s; }
}