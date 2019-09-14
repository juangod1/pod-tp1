package grupo2.server.service;

import grupo2.api.iface.ConsultService;
import grupo2.api.model.ElectionResults;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.ElectionStatus;
import grupo2.api.model.Province;
import grupo2.server.election.ElectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsultingServiceImpl implements ConsultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultingServiceImpl.class);

    private ElectionManager em;

    public ConsultingServiceImpl(ElectionManager em){
        this.em = em;
    }

    @Override
    public ElectionResults consultTotal() throws ElectionStateException{
        LOGGER.debug("Consult total");

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new ElectionStateException("Elections have not yet started.");
            case STARTED:
            case FINISHED:
                return em.getNationalResults();
        }
        return null;
    }

    @Override
    public ElectionResults consultProvince(Province province) throws ElectionStateException{
        LOGGER.debug("Consult province {}", province);

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new ElectionStateException("Elections have not yet started.");
            case STARTED:
            case FINISHED:
                return em.getProvincialResults(province);
        }
        return null;
    }

    @Override
    public ElectionResults consultTable(int tableId) throws ElectionStateException {
        LOGGER.debug("Consult table {}", tableId);

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new ElectionStateException("Elections have not yet started.");
            case STARTED:
            case FINISHED:
                return em.getTableResults(tableId);
        }
        return null;
    }
}
