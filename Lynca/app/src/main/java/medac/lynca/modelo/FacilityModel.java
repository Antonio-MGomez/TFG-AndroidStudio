package medac.lynca.modelo;

public class FacilityModel {
    public String name;
    public String city;
    public String sport;
    public String distance;
    public float  rating;
    public int    price;
    public int    imageRes;
    public String pistaId;  // ← ID real de Supabase

    public FacilityModel(String name, String city, String sport,
                         String distance, float rating, int price,
                         int imageRes, String pistaId) {
        this.name     = name;
        this.city     = city;
        this.sport    = sport;
        this.distance = distance;
        this.rating   = rating;
        this.price    = price;
        this.imageRes = imageRes;
        this.pistaId  = pistaId;
    }
}