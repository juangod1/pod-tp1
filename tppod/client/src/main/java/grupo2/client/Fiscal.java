package grupo2.client;

import grupo2.api.Party;
import grupo2.api.Vote;
import grupo2.api.VoteListener;

public class Fiscal implements VoteListener {

    @Override
    public void reportVote(Vote vote) {
        System.out.println("Someone cast the following vote " + vote.toString() + " in table " + vote.getBallotBox());
    }
}
