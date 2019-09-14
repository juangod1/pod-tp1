package grupo2.server;

import grupo2.api.model.*;
import grupo2.server.election.ElectionManager;
import org.junit.Before;
import org.junit.Test;


import java.util.*;

import static grupo2.api.model.Party.*;
import static junit.framework.Assert.assertEquals;

public class ElectionManagerTest {
    private ElectionManager em;
    private static final double EPS = 0.00001;

    @Before
    public void beforeTest() {
        em = new ElectionManager();
        em.setElectionStatus(ElectionStatus.STARTED);
    }


    public List<Vote> generateVotes(int amount, int ballotBox, Province province, Party... parties) {
        List<Vote> votes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            votes.add(new Vote(ballotBox, province, Arrays.asList(parties)));
        }

        return votes;
    }

    @Test
    public void tableElectionResultTests() {
        int TARGET_BOX = 0;
        int OTHER_BOX = 1;
        generateVotes(10, TARGET_BOX, Province.JUNGLE, Party.BUFFALO​).forEach(v -> em.notifyVote(v));
        generateVotes(30, TARGET_BOX, Province.JUNGLE, Party.MONKEY).forEach(v -> em.notifyVote(v));
        generateVotes(60, TARGET_BOX, Province.JUNGLE, Party.OWL).forEach(v -> em.notifyVote(v));

        generateVotes(60, OTHER_BOX, Province.JUNGLE, Party.SNAKE, null, null).forEach(v -> em.notifyVote(v));

        ElectionResults es = em.getTableResults(TARGET_BOX);
        assertEquals(0.1, es.getResults(Party.BUFFALO​), EPS);
        assertEquals(0.3, es.getResults(Party.MONKEY), EPS);
        assertEquals(0.6, es.getResults(Party.OWL), EPS);
        assertEquals(0.0, es.getResults(Party.SNAKE), EPS);
    }


    @Test
    public void alternativeVotingTest() {
        Province p = Province.JUNGLE;
        Party A = Party.BUFFALO​;
        Party B = Party.GORILLA;
        Party C = Party.JACKALOPE;
        Party D = Party.LEOPARD;
        Party E = Party.SNAKE;
        generateVotes(3, 0, p, B,C,A,D,E).forEach(v -> em.notifyVote(v));
        generateVotes(4, 0, p, C,A,D,B,E).forEach(v -> em.notifyVote(v));
        generateVotes(4, 0, p, B,D,C,A,E).forEach(v -> em.notifyVote(v));
        generateVotes(6, 0, p, D,C,A,E,B).forEach(v -> em.notifyVote(v));
        generateVotes(2, 0, p, B,E,A,C,D).forEach(v -> em.notifyVote(v));
        generateVotes(1, 0, p, E,A,D,B,C).forEach(v -> em.notifyVote(v));

        ElectionResults resultsBeforeClosing = em.getNationalResults(); // FPTP
        Map<Party, Double> rbc = resultsBeforeClosing.getResults();
        assertEquals(4, rbc.keySet().size());
        assertEquals(3.0/20 + 4.0/20 + 2.0/20, rbc.get(B), EPS);
        assertEquals(4.0/20, rbc.get(C), EPS);
        assertEquals(6.0/20, rbc.get(D), EPS);
        assertEquals(1.0/20, rbc.get(E), EPS);
        assertEquals(0.0, resultsBeforeClosing.getResults(A), EPS);

        em.setElectionStatus(ElectionStatus.FINISHED);
        ElectionResults nationalResults = em.getNationalResults();
        assertEquals(ElectionStatus.FINISHED, em.getElectionStatus());
        assertEquals(1, nationalResults.getResults().keySet().size());
        Map.Entry<Party, Double> winner = nationalResults.getResults().entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get();
        assertEquals(D, winner.getKey());
        assertEquals(11.0/20, winner.getValue(), EPS);
    }


    @Test
    public void stvTest() {
        // El ejemplo del video
        Province p = Province.JUNGLE;
        List<Vote> votes = new ArrayList<>();
        votes.addAll(generateVotes(15, 0, p, TARSIER, WHITE_GORILLA));
        votes.addAll(generateVotes(32, 0, p, GORILLA, TARSIER, WHITE_GORILLA));
        votes.addAll(generateVotes(64, 0, p, GORILLA, WHITE_GORILLA));
        votes.addAll(generateVotes(9, 0, p, WHITE_GORILLA));
        votes.addAll(generateVotes(99, 0, p, OWL, TURTLE));
        votes.addAll(generateVotes(3, 0, p, TURTLE));
        votes.addAll(generateVotes(3, 0, p, SNAKE, TURTLE));
        votes.addAll(generateVotes(48, 0, p, TIGER));
        votes.addAll(generateVotes(12, 0, p, LYNX, TIGER));
        votes.addAll(generateVotes(6, 0, p, JACKALOPE));
        votes.addAll(generateVotes(6, 0, p, BUFFALO​, JACKALOPE));
        votes.addAll(generateVotes(3, 0, p, BUFFALO​, TURTLE));

        // Votos en otras provincias no deberian afectar
        Province other = Province.SAVANNAH;

        votes.addAll(generateVotes(30, 0, other, BUFFALO​, TURTLE));
        votes.addAll(generateVotes(30, 0, other, JACKALOPE, TURTLE));

        votes.forEach(v -> em.notifyVote(v));

        ElectionResults partialResults = em.getProvincialResults(p);

        assertEquals(10, partialResults.getResults().keySet().size());
        assertEquals(15.0/300, partialResults.getResults(TARSIER), EPS);
        assertEquals(99.0/300, partialResults.getResults(OWL), EPS);
        assertEquals(48.0/300, partialResults.getResults(TIGER), EPS);

        em.setElectionStatus(ElectionStatus.FINISHED);
        ElectionResults results = em.getProvincialResults(p);
        Map<Party, Double> calculated = results.getResults();
        assertEquals(5, calculated.keySet().size());
        assertEquals(0.2, calculated.get(OWL), EPS);
        assertEquals(0.2, calculated.get(GORILLA), EPS);
        assertEquals(0.2, calculated.get(WHITE_GORILLA), EPS);
        assertEquals(0.2, calculated.get(TIGER), EPS);
        assertEquals(0.16, calculated.get(TURTLE), EPS);
    }

}
