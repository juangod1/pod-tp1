package grupo2.server;

import grupo2.api.FiscalizationService;
import grupo2.api.Party;
import grupo2.api.Vote;
import grupo2.api.VoteListener;
import grupo2.server.service.VoteInformer;

import java.rmi.RemoteException;

public class FiscalizationServiceImpl implements FiscalizationService, VoteInformer {

    VoteListener listener;

    @Override
    public boolean register(VoteListener listener, Party party, int id) {
        this.listener = listener;
        return false;
    }

    public void newVote(Vote vote) throws RemoteException {
        listener.reportVote(vote);
    }
}
