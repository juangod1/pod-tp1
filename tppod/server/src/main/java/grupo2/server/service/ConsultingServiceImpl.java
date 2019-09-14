package grupo2.server.service;

import grupo2.api.*;
import grupo2.server.Server;
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
    public ElectionResults consultTotal() {
        LOGGER.debug("Consult total");

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Elections have not yet started.");
            case STARTED:
            case FINISHED:
                return em.getNationalResults();
        }
        return null;
    }

    @Override
    public ElectionResults consultProvince(Province province) {
        LOGGER.debug("Consult province {}", province);

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Elections have not yet started.");
            case STARTED:
            case FINISHED:
                return em.getProvincialResults(province);
        }
        return null;
    }

    @Override
    public ElectionResults consultTable(int tableId) {
        LOGGER.debug("Consult table {}", tableId);

        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Elections have not yet started.");
            case STARTED:
            case FINISHED:
                return em.getTableResults(tableId);
        }
        return null;
    }
}
