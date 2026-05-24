import java.io.Serializable;

public class BookingResult implements Serializable{
    private static final long serialVersionUID= 1L;

    public boolean success;
    public boolean partialAvailable; //αν υπάρχουν διαθέσιμα λιγότερα από τα ζητούμενα δωμάτια+
    public int available;
    public int booked;
    public int totalCost;
    public String message; //έτοιμο μήνυμα που μπορεί να τυπώσει ο client

    public BookingResult(boolean success, boolean partialAvailable, int available, int booked, int totalCost, String message){
        this.success= success;
        this.partialAvailable= partialAvailable;
        this.available= available;
        this.booked= booked;
        this.totalCost= totalCost;
        this.message= message;
    }
}