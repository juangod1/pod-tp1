package grupo2.server.election;

import grupo2.api.model.Party;
import grupo2.api.model.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class SingleTransferableVoteCalculator implements VoteCalculator {
    private Logger LOGGER = LoggerFactory.getLogger(SingleTransferableVoteCalculator.class);
    private List<Vote> allVotes;
    private double threshold;
    private int numberOfWinners;

    private class TransferableVote {
        private List<Party> history;
        private double percentage;  // El porcentaje sobre el total que representa

        public TransferableVote(List<Party> history, double percentage) {
            this.history = history;
            this.percentage = percentage;
        }

        public List<Party> getHistory() {
            return history;
        }

        public double getPercentage() {
            return percentage;
        }
    }


    private class TransferableVoteSet {
        private List<TransferableVote> votes;
        private Party party;
        private double percentage;

        public TransferableVoteSet(Party party, List<TransferableVote> votes) {
            this.votes = new ArrayList<>(votes);
            this.party = party;
            recalculatePercentage();
        }

        private void recalculatePercentage() {
            this.percentage = votes.stream()
                    .map(TransferableVote::getPercentage)
                    .reduce(Double::sum)
                    .orElse(0.0);
        }

        public List<TransferableVote> getVotes() {
            return votes;
        }

        public Party getParty() {
            return party;
        }

        public double getPercentage() {
            return percentage;
        }

        public void addVote(TransferableVote newVote) {
            this.votes.add(newVote);
            recalculatePercentage();
        }
    }


    public SingleTransferableVoteCalculator(List<Vote> allVotes, int numberOfWinners) {
        this.allVotes = allVotes;
        this.threshold = 1.0/((double)numberOfWinners);
        this.numberOfWinners = numberOfWinners;
    }



    private double calculateProbabilities(List<Party> givenVotes) {
        // Calcula las probabilidades de que el i-esimo voto sea givenVotes.get(i)
        // con 0 <= i <= givenVotes.length()
        double survivingVotes = allVotes.stream()
                .filter(v -> Collections.indexOfSubList(v.getRanking(), givenVotes) == 0)
                .count();

        return survivingVotes / allVotes.size();
    }


    public Map<Party, Double> calculate() {
        List<TransferableVoteSet> initialVotes =
                Arrays.stream(Party.values())
                      .map(p -> new TransferableVoteSet(p, singletonList(new TransferableVote(singletonList(p),
                                                                        calculateProbabilities(singletonList(p))))))
                      .filter(x -> x.getPercentage() > 0)
                      .collect(toList());

        return singleTransferableVote(initialVotes, new HashMap<>());
    }


    private void transferVotes(TransferableVoteSet fromVoteSet, List<TransferableVoteSet> transferTo, double multiplier) {
        // Distribuir los porcentajes de los que se pasaron
        // Hacer esto con streams queda muy oscuro
        for (TransferableVote t: fromVoteSet.getVotes()) {
                // Transfiero cada voto proporcionalmente
                for (TransferableVoteSet beneficiary: transferTo) {
                    // Dada la historia del voto t que estoy transfiriendo,
                    // cuantos votaron como la siguiente opcion a beneficiary (un candidato que sigue en carrera)?
                    Party newParty = beneficiary.getParty();
                    List<Party> voteWithNewParty = new ArrayList<>(t.getHistory());
                    voteWithNewParty.add(newParty);

                    // El porcentaje que se transfiere es de "los que votaron ese ranking que venimos transfiriendo,
                    // que ahora caducó, más la Party que estamos considerando como siguiente opcion"
                    // Ademas lo multiplicamos por el multiplicador que es el porcentaje que tenemos que distribuir
                    double percentageOfTransfer = multiplier * calculateProbabilities(voteWithNewParty);
                    if(percentageOfTransfer > 0) {
                        LOGGER.debug("{}% of votes go from {} to {}",percentageOfTransfer*100, fromVoteSet.getParty(), newParty);
                        TransferableVote newVote = new TransferableVote(voteWithNewParty, percentageOfTransfer);
                        beneficiary.addVote(newVote);   // Se lo agrego como voto
                    }
                }
            }
    }


    private Map<Party, Double> singleTransferableVote(List<TransferableVoteSet> partyVotes, Map<Party,Double> winners) {
        List<TransferableVoteSet> votesConsidered = partyVotes;
        boolean potentialNewWinners = true;
        while (potentialNewWinners) {
            potentialNewWinners = false;
            List<TransferableVoteSet> stillRunning = new ArrayList<>();
            List<TransferableVoteSet> justWon = new ArrayList<>();

            for (TransferableVoteSet t : votesConsidered) {
                if (t.percentage >= threshold) {             // New winner!
                    LOGGER.debug("New winner {}", t.getParty());
                    winners.put(t.getParty(), threshold);   // Se paso de 20% asi que pongo 20% justo en el resultado
                    justWon.add(t);
                } else {
                    stillRunning.add(t);
                }
            }

            if (winners.keySet().size() >= numberOfWinners) {
                return winners;
            }

            // Distribuir los porcentajes de los que se pasaron
            for (TransferableVoteSet v : justWon) {
                double overTheThresholdProportion = (v.getPercentage() - threshold) / (v.getPercentage());
                if (overTheThresholdProportion == 0) {
                    continue; // Nada que distribuir
                }
                transferVotes(v, stillRunning, overTheThresholdProportion); // Transferir votos de v (solo la proporcion que queda por encima del threshold)
                potentialNewWinners = true; // Hay transferencia de votos, quizas alguien nuevo ganó
            }

            votesConsidered = stillRunning;
        }


        // Si quedan los candidatos justos (o menos porque no votaron por todos)
        if (votesConsidered.size() + winners.keySet().size() <= numberOfWinners) {
            // No me queda otra que devolver los que quedan
            LOGGER.debug("Remaining candidates are in");
            for(TransferableVoteSet tvs: votesConsidered) {
                winners.put(tvs.getParty(), tvs.getPercentage());
            }
            return winners;
        }

        // Buscamos el peor, elimininamos sus votos e iteramos
        // Ordenados decreciente por porcentaje
        List<TransferableVoteSet> sortedCandidates = votesConsidered.stream()
                .sorted((x,y) -> Double.compare(y.getPercentage(), x.getPercentage())).collect(toList());

        List<TransferableVoteSet> survivingCandidates = sortedCandidates.subList(0, sortedCandidates.size() - 1);
        TransferableVoteSet eliminated = sortedCandidates.get(sortedCandidates.size() - 1);
        transferVotes(eliminated, survivingCandidates, 1.0); // Transferir los votos del eliminado
        LOGGER.debug("{} gets eliminated", eliminated.getParty());

        // Sigo calculando ahora con uno menos
        return singleTransferableVote(survivingCandidates, winners);
    }

}
