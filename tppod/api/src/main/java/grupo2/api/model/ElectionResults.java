package grupo2.api.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class ElectionResults implements Serializable {
    private Map<Party, Double> counts;
    private ElectionStatus electionStatus;

    public ElectionResults(Map<Party, Double> counts, ElectionStatus es) {
        this.counts = counts;
        this.electionStatus = es;
    }

    public double getResults(Party party) {
        return Optional.ofNullable(counts.get(party)).orElse(0.0);
    }

    public Map<Party,Double> getResults() {
        return counts;
    }



    public ElectionStatus getElectionStatus() {
        return electionStatus;
    }
}
