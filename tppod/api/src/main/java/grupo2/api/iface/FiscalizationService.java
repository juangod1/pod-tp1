package grupo2.api.iface;

import grupo2.api.model.ElectionStateException;
import grupo2.api.model.Party;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FiscalizationService extends Remote {
    boolean register(VoteListener listener, Party party, int id) throws RemoteException, ElectionStateException;
}
