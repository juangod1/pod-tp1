package grupo2.api;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HelloServerService extends Remote {
    String hello(String name) throws RemoteException;
}
