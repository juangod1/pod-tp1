package grupo2.server.service;

import grupo2.api.*;
import grupo2.server.ElectionManager;

public class ConsultingServiceImpl implements ConsultingService {
    private ElectionManager em;

    public ConsultingServiceImpl(ElectionManager em){
        this.em = em;
    }

    @Override
    public ElectionResults consultElection(ElectionLevel level, Object dimension) throws IllegalStateException {
        ElectionStatus status = em.getElectionStatus();
        switch(status){
            case NOT_STARTED:
                throw new IllegalStateException("Elections have not yet started.");
            case STARTED:
                return em.getNationalResults();
            case FINISHED:
                switch(level){
                    case TABLE:
                        return em.getTableResults((int)dimension);
                    case NATIONAL:
                        return em.getNationalResults();
                    case PROVINCIAL:
                        return em.getProvincialResults((Province)dimension);
                }
        }
        return null;
    }
}
