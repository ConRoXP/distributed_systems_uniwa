import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HRInterface extends Remote{
    String listRooms() throws RemoteException;
    BookingResult book(String type, int number, String name, boolean allowPartial) throws RemoteException;
    String listGuests() throws RemoteException;
    String cancel(String type, int number, String name) throws RemoteException;
    void registerForNotification(String type, String name, ClientCallback callback) throws RemoteException;
}