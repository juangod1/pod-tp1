package grupo2.server.service;

import grupo2.api.ElectionStatus;
import grupo2.api.Vote;
import grupo2.api.VotingService;
import grupo2.server.election.ElectionManager;
import grupo2.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VotingServiceImpl implements VotingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private ElectionManager em;

    public VotingServiceImpl(ElectionManager em) {
        this.em = em;
    }

    @Override
    public void addVote(Vote vote) throws IllegalStateException{
        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Poll has not opened yet.");
            case STARTED:
                em.addVote(vote);
                break;
            case FINISHED:
                throw new IllegalStateException("Poll has already closed.");
        }
    }
}
