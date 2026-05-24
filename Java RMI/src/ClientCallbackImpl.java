import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback{
    public ClientCallbackImpl() throws RemoteException{
        super();
    }

    @Override
    public void messageClient(String message) throws RemoteException{
        System.out.println("\n+++ RESERVATION NOTIFICATION +++");
        System.out.println(message);
        System.out.println("Exit and try again.");
    }
}