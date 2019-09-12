package grupo2.api;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdministrationService extends Remote {

    void openElection() throws RemoteException, IllegalStateException;
    ElectionStatus consultElectionState() throws RemoteException;
    void closeElection() throws RemoteException, IllegalStateException;

}
