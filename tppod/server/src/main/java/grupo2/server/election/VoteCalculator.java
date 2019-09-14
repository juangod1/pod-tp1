package grupo2.server.election;

import grupo2.api.model.Party;

import java.util.Map;

public interface VoteCalculator {
    Map<Party, Double> calculate();
}
