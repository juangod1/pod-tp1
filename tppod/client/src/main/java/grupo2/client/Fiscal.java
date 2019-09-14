package grupo2.client;

import grupo2.api.model.Party;
import grupo2.api.model.Vote;
import grupo2.api.iface.VoteListener;

public class Fiscal implements VoteListener {
    private Party party;


    public Fiscal(Party party){
        this.party=party;
    }

    @Override
    public void reportVote(Vote vote) {
        System.out.println("New vote for " + party.name() + " in table " + vote.getBallotBox());
    }
}
