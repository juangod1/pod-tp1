package grupo2.server.service;

import grupo2.api.*;
import grupo2.server.election.ElectionManager;

public class ConsultingServiceImpl implements ConsultService {
    private ElectionManager em;

    public ConsultingServiceImpl(ElectionManager em){
        this.em = em;
    }

    @Override
    public ElectionResults consultTotal() {
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
        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Elections have not yet started.");
            case STARTED:
                return em.getNationalResults();
            case FINISHED:
                return em.getProvincialResults(province);
        }
        return null;
    }

    @Override
    public ElectionResults consultTable(int tableId) {
        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Elections have not yet started.");
            case STARTED:
                return em.getNationalResults();
            case FINISHED:
                return em.getTableResults(tableId);
        }
        return null;
    }
}
