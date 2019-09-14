package grupo2.client;

import grupo2.api.model.Vote;
import grupo2.api.iface.VoteListener;

public class Fiscal implements VoteListener {

    @Override
    public void reportVote(Vote vote) {
        System.out.println("Someone cast the following vote " + vote.toString() + " in table " + vote.getBallotBox());
    }
}
