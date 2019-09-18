package grupo2.server.service;

import grupo2.api.iface.FiscalizationService;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.Party;
import grupo2.api.model.Vote;
import grupo2.api.iface.VoteListener;
import grupo2.server.election.ElectionManager;
import grupo2.server.election.VoteObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FiscalizationServiceImpl implements FiscalizationService, VoteObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiscalizationServiceImpl.class);
    private Map<Party, Map<Integer, List<VoteListener>>> listeners;
    private ElectionManager em;
    private ExecutorService executorService;

    public FiscalizationServiceImpl(ElectionManager manager) {
        this.em = manager;
        // Usamos un thread pool para invocar los callbacks
        // Porque no queremos bloquear trabajo util esperando el retorno de los callbacks
        // que podria durar tiempo indefinido (en efecto evitamos denial of service)
        this.executorService = Executors.newCachedThreadPool();
        this.listeners = new EnumMap<>(Party.class);
        Arrays.stream(Party.values()).forEach(p -> this.listeners.put(p, new HashMap<>()));
        manager.register(this);
    }

    @Override
    public boolean register(VoteListener listener, Party party, int id) throws ElectionStateException {
        switch (em.getElectionStatus()){
            case NOT_STARTED:
                LOGGER.debug("Register listener for Party {}, table {}", party, id);
                Map<Integer, List<VoteListener>> partyFiscals = listeners.get(party);
                partyFiscals.putIfAbsent(id, new ArrayList<>());
                partyFiscals.get(id).add(listener);
                return true;
            case FINISHED:
                throw new ElectionStateException("Elections have already finished.");
            case STARTED:
                throw new ElectionStateException("Elections have already begun.");
        }
        return false;
    }

    private void notifyListenersOf(Party p, Vote vote) {
        Map<Integer, List<VoteListener>> partyFiscals = listeners.get(p);
        Optional.ofNullable(partyFiscals.get(vote.getBallotBox()))
                        .ifPresent(ls -> ls.forEach(l -> {
                            executorService.submit(() -> {
                                try {
                                    LOGGER.debug("Notifying listener of vote for {} - {}", p, vote.getBallotBox());
                                    l.reportVote(vote);
                                } catch (RemoteException e) {
                                    LOGGER.error("Remote exception while trying to notify vote {}: {}", vote, e.getMessage());
                                } catch (Exception e){
                                    LOGGER.error("Unexpected exception trying to notify vote {}: {}", vote, e.getMessage());
                                }
                            });
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
