package grupo2.api.iface;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.ElectionStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdministrationService extends Remote {

    void openElection() throws RemoteException, ElectionStateException;
    ElectionStatus consultElectionState() throws RemoteException;
    void closeElection() throws RemoteException, ElectionStateException;

}
