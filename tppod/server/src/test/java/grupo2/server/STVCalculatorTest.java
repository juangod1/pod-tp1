package grupo2.server;

import grupo2.api.model.Party;
import grupo2.api.model.Province;
import grupo2.api.model.Vote;
import grupo2.server.election.SingleTransferableVoteCalculator;
import org.junit.Test;

import java.util.*;

import static grupo2.api.model.Party.*;
import static junit.framework.Assert.assertEquals;

public class STVCalculatorTest {

    private static final double EPS = 0.000001;

    public List<Vote> generateVotes(int amount, int ballotBox, Province province, Party... parties) {
        List<Vote> votes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            votes.add(new Vote(ballotBox, province, Arrays.asList(parties)));
        }

        return votes;
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

        SingleTransferableVoteCalculator calculator = new SingleTransferableVoteCalculator(votes, 5);
        Map<Party, Double> calculated = calculator.calculate();
        assertEquals(5, calculated.keySet().size());
        assertEquals(0.2, calculated.get(OWL), EPS);
        assertEquals(0.2, calculated.get(GORILLA), EPS);
        assertEquals(0.2, calculated.get(WHITE_GORILLA), EPS);
        assertEquals(0.2, calculated.get(TIGER), EPS);
        assertEquals(0.16, calculated.get(TURTLE), EPS);
    }

}
