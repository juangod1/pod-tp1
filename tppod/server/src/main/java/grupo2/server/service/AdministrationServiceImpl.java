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
        em.setElectionStatus(ElectionStatus.STARTED);
    }

    @Override
    public ElectionStatus consultElectionState() {
        return em.getElectionStatus();
    }

    @Override
    public void closeElection() throws ElectionStateException {
        em.setElectionStatus(ElectionStatus.FINISHED);
    }

}
