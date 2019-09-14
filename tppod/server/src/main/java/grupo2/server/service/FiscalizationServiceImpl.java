package grupo2.server.service;

import grupo2.api.FiscalizationService;
import grupo2.api.Party;
import grupo2.api.Vote;
import grupo2.api.VoteListener;
import grupo2.server.election.ElectionManager;
import grupo2.server.election.VoteObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;

public class FiscalizationServiceImpl implements FiscalizationService, VoteObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiscalizationServiceImpl.class);
    private Map<Party, Map<Integer, List<VoteListener>>> listeners;


    public FiscalizationServiceImpl(ElectionManager manager) {
        this.listeners = new EnumMap<>(Party.class);
        Arrays.stream(Party.values()).forEach(p -> this.listeners.put(p, new HashMap<>()));
        manager.register(this);
    }

    @Override
    public boolean register(VoteListener listener, Party party, int id) {
        Map<Integer, List<VoteListener>> partyFiscals = listeners.get(party);
        partyFiscals.putIfAbsent(id, new ArrayList<>());
        partyFiscals.get(id).add(listener);
        return true;
    }

    private void notifyListenersOf(Party p, Vote vote) {
        Map<Integer, List<VoteListener>> partyFiscals = listeners.get(p);
        Optional.ofNullable(partyFiscals.get(vote.getBallotBox()))
                        .ifPresent(ls -> ls.forEach(l -> {
                        try {
                            l.reportVote(vote);
                        } catch (RemoteException e) {
                            LOGGER.error("Remote exception while trying to notify vote {}: {}", vote, e.getMessage());
                        }
                    }));
    }

    @Override
    public void newVotes(List<Vote> votes) {
        votes.forEach(this::newVote);
    }

    public void newVote(Vote vote) {
        vote.getRanking().forEach(p -> notifyListenersOf(p, vote));
    }
}
