package grupo2.server.election;

import grupo2.api.Party;

import java.util.Map;

public interface VoteCalculator {
    Map<Party, Double> calculate();
}
