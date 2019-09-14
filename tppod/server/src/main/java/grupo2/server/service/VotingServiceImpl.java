package grupo2.server.service;

import grupo2.api.model.ElectionStateException;
import grupo2.api.model.ElectionStatus;
import grupo2.api.model.Vote;
import grupo2.api.iface.VotingService;
import grupo2.server.election.ElectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VotingServiceImpl implements VotingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VotingServiceImpl.class);
    private ElectionManager em;

    public VotingServiceImpl(ElectionManager em) {
        this.em = em;
    }

    @Override
    public void addVote(Vote vote) throws ElectionStateException{
        LOGGER.debug("Adding a vote for Province {} Table {}: {}", vote.getProvince(), vote.getBallotBox(), vote.getRanking());

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new ElectionStateException("Poll has not opened yet.");
            case STARTED:
                em.addVote(vote);
                break;
            case FINISHED:
                throw new ElectionStateException("Poll has already closed.");
        }
    }
}
