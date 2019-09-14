package grupo2.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VotingService extends Remote {
    void addVote(Vote vote) throws RemoteException;
}
