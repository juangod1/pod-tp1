package grupo2.server;

import grupo2.api.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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

        em.setElectionStatus(ElectionStatus.FINISHED);
        ElectionResults nationalResults = em.getNationalResults();
        assertEquals(ElectionStatus.FINISHED, em.getElectionStatus());

        Map.Entry<Party, Double> winner = nationalResults.getResults().entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get();
        assertEquals(D, winner.getKey());
        assertEquals(11.0/20, winner.getValue(), EPS);
    }
}
