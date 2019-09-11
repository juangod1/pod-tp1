package grupo2.api;

import java.io.Serializable;
import java.util.Optional;

public class Vote implements Serializable {
    static final long serialVersionUID = 145L;
    private final int ballotBox;
    private final int position;
    private Party party;

    public Vote(Party party, int ballotBox, int position) {
        this.party = party;
        this.ballotBox = ballotBox;
        this.position = position;
    }

    public Party getParty() {
        return party;
    }

    public int getBallotBox() {
        return ballotBox;
    }

    public int getPosition() {
        return position;
    }
}
