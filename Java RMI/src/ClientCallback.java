import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote{
    void messageClient(String message) throws RemoteException;
}
