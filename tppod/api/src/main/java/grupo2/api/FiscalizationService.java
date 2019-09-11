package grupo2.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FiscalizationService extends Remote {
    boolean register(VoteListener listener, Party party, int id) throws RemoteException;
}
