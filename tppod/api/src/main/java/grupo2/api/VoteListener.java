package grupo2.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VoteListener extends Remote {
    void reportVote(Vote vote) throws RemoteException;
}
