package medac.lynca.modelo;

public class ReservationModel {

    private long   id;
    private String facilityName;
    private String time;
    private String imageResource;
    private String status;

    // Nuevos campos para detalles
    private String pistaId;
    private String imagenUrl;
    private String direccion;
    private String tipoDeporte;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String precio;

    // Constructor original
    public ReservationModel(long id, String facilityName,
                            String time, String imageResource, String status) {
        this.id            = id;
        this.facilityName  = facilityName;
        this.time          = time;
        this.imageResource = imageResource;
        this.status        = status;
    }

    // Constructor completo
    public ReservationModel(long id, String facilityName,
                            String time, String imageResource,
                            String status, String pistaId,
                            String imagenUrl, String direccion,
                            String tipoDeporte, String fecha,
                            String horaInicio, String horaFin,
                            String precio) {
        this.id            = id;
        this.facilityName  = facilityName;
        this.time          = time;
        this.imageResource = imageResource;
        this.status        = status;
        this.pistaId       = pistaId;
        this.imagenUrl     = imagenUrl;
        this.direccion     = direccion;
        this.tipoDeporte   = tipoDeporte;
        this.fecha         = fecha;
        this.horaInicio    = horaInicio;
        this.horaFin       = horaFin;
        this.precio        = precio;
    }

    public long   getId()            { return id; }
    public String getFacilityName()  { return facilityName; }
    public String getTime()          { return time; }
    public String getImageResource() { return imageResource; }
    public String getStatus()        { return status; }
    public String getPistaId()       { return pistaId; }
    public String getImagenUrl()     { return imagenUrl; }
    public String getDireccion()     { return direccion; }
    public String getTipoDeporte()   { return tipoDeporte; }
    public String getFecha()         { return fecha; }
    public String getHoraInicio()    { return horaInicio; }
    public String getHoraFin()       { return horaFin; }
    public String getPrecio()        { return precio; }
}