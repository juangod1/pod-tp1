package grupo2.server.election;

import grupo2.api.model.Party;
import grupo2.api.model.Vote;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlternativeVoteCalculator implements VoteCalculator {

    private final List<Vote> votes;

    public AlternativeVoteCalculator(List<Vote> votes) {
        this.votes = votes;
    }


    private Optional<Party> getChoice(Vote v, Predicate<Party> filter) {
        return v.getRanking().stream().filter(filter).findFirst();
    }


    @Override
    public Map<Party, Double> calculate() {
        return calculate(votes, t -> true);
    }

    private Map<Party, Double>  calculate(List<Vote> validVotes, Predicate<Party> filterPredicate) {
        if(validVotes.isEmpty()) {
            return new HashMap<>();
        }
        // Alternative voting system
        double voteCount = this.votes.size();   // Los porcentajes son siempre sobre el total (aun filtrando blancos)

        List<Vote> filteredVotes = // Sacamos los votos "en blanco" que pueden aparecer
                validVotes.stream()
                        .filter(v -> getChoice(v, filterPredicate).isPresent())
                        .collect(Collectors.toList());

        List<Map.Entry<Party, Long>> partySortedCount =
                filteredVotes.stream()
                        .map(v -> getChoice(v, filterPredicate).get()) // Ya estan filtrados, el get no falla
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingLong(Map.Entry::getValue))
                        .collect(Collectors.toList());

        // partySortedCount has the most voted last
        double maxPercentage = partySortedCount.get(partySortedCount.size() - 1).getValue()/voteCount;

        if (maxPercentage > 0.5 || partySortedCount.size() == 1) {
            Map<Party, Double> results = new HashMap<>();
            results.put(partySortedCount.get(partySortedCount.size() - 1).getKey(), maxPercentage);
            return results;
        } else {
            Party worst = partySortedCount.get(0).getKey(); // El primero es el peor
            Predicate<Party> newFilter = filterPredicate.and(p -> p != worst);  // La proxima iteracion no considera este partido
            return calculate(filteredVotes, newFilter);
        }
    }
}
