package medac.lynca.modelo;

public class DateModel {
    private String dayName;
    private int dayNumber;
    private boolean isSelected;
    // NUEVO: Multiplicador de precio para este día
    private double priceMultiplier;

    public DateModel(String dayName, int dayNumber, boolean isSelected, double priceMultiplier) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.isSelected = isSelected;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDayName() { return dayName; }
    public int getDayNumber() { return dayNumber; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    // NUEVO: Getter para el multiplicador
    public double getPriceMultiplier() { return priceMultiplier; }
}