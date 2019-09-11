package grupo2.server.service;

import grupo2.api.Vote;

import java.rmi.RemoteException;

public interface VoteInformer {
    void newVote(Vote vote) throws RemoteException;
}
