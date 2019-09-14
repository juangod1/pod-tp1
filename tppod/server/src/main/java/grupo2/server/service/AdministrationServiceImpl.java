package grupo2.server.service;

import grupo2.api.iface.AdministrationService;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.ElectionStatus;
import grupo2.server.election.ElectionManager;
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
    public void openElection() throws ElectionStateException {
        ElectionStatus state = em.getElectionStatus();
        switch(state){
            case NOT_STARTED:
                em.setElectionStatus(ElectionStatus.STARTED);
                LOGGER.info("Started election.");
                break;
            case STARTED:
                LOGGER.info("Attempted to start an already started election. No effects.");
                break;
            case FINISHED:
                throw new ElectionStateException("Tried to open a finished election.");
        }
    }

    @Override
    public ElectionStatus consultElectionState() {
        return em.getElectionStatus();
    }

    @Override
    public void closeElection() throws ElectionStateException {
        ElectionStatus state = em.getElectionStatus();

        switch(state){
            case NOT_STARTED:
                throw new ElectionStateException("Tried to close an election that has not started.");
            case STARTED:
                em.setElectionStatus(ElectionStatus.FINISHED);
                LOGGER.info("Closed election.");
                break;
            case FINISHED:
                LOGGER.info("Attempted to end an already stopped election. No effects.");
                break;
        }
    }

}
