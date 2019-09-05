package grupo2.api;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdministrationService extends Remote {
    enum ElectionState {
        NOT_STARTED,
        STARTED,
        FINISHED
    }

    void openElection() throws RemoteException, IllegalStateException;
    ElectionState consultElectionState() throws RemoteException;
    void closeElection() throws RemoteException, IllegalStateException;
}
