package grupo2.api.iface;

import grupo2.api.model.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VotingService extends Remote {
    void addVote(Vote vote) throws RemoteException;
}
