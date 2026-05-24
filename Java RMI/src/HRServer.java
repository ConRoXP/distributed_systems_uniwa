import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HRServer{
    public static void main(String[] args){
        try{
            HRImpl hotel= new HRImpl();
            Registry registry= LocateRegistry.createRegistry(3338);
            registry.rebind("HotelReservation", hotel);

            System.out.println("HR RMI server initialized.");
        }
        catch(Exception e){
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}