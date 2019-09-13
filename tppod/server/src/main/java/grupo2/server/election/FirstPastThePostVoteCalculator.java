package grupo2.server.election;

import grupo2.api.Party;
import grupo2.api.Vote;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPastThePostVoteCalculator implements VoteCalculator {

    private final List<Vote> votes;

    public FirstPastThePostVoteCalculator(List<Vote> votes) {
        this.votes = votes;
    }

    @Override
    public Map<Party, Double> calculate() {
        return calculate(x -> true);
    }

    private Map<Party, Double> calculate(Predicate<Vote> filterPredicate) {
        double size = votes.stream()
                .filter(filterPredicate)
                .count();

        return votes.stream()
                .filter(filterPredicate)
                .map(Vote::getTopVote)
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.collectingAndThen(Collectors.counting(), x -> x/size)));
    }
}
