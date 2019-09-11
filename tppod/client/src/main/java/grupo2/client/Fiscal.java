package grupo2.client;

import grupo2.api.Vote;
import grupo2.api.VoteListener;

public class Fiscal implements VoteListener {

    @Override
    public void reportVote(Vote vote) {
        System.out.println("Someone voted for " + vote.getParty() + " in table " + vote.getBallotBox() + " as their choice #" + vote.getPosition());
    }
}
