package grupo2.server.service;

import grupo2.api.*;
import grupo2.api.ElectionStatus;
import grupo2.server.ElectionManager;
import grupo2.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdministrationServiceImpl implements AdministrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private ElectionManager em;

    public AdministrationServiceImpl(ElectionManager em) {
        this.em = em;
    }

    @Override
    public void openElection() throws IllegalStateException {
        ElectionStatus state = em.getElectionStatus();
        switch(state){
            case NOT_STARTED:
                em.setElectionStatus(ElectionStatus.STARTED);
                LOGGER.info("Started election.");
                break;
            case STARTED:
                // All is fine
                break;
            case FINISHED:
                throw new IllegalStateException("Tried to open a finished election.");
        }
    }

    @Override
    public ElectionStatus consultElectionState() {
        return em.getElectionStatus();
    }

    @Override
    public void closeElection() throws IllegalStateException {
        ElectionStatus state = em.getElectionStatus();

        switch(state){
            case NOT_STARTED:
                throw new IllegalStateException("Tried to close an election that has not started.");
            case STARTED:
                em.setElectionStatus(ElectionStatus.FINISHED);
                LOGGER.info("Closed election.");
                break;
            case FINISHED:
                break;
        }
    }
}
