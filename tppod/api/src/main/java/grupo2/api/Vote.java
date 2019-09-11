package grupo2.api;

import java.io.Serializable;
import java.util.Optional;

public class Vote implements Serializable {
    static final long serialVersionUID = 146L;
    private final int ballotBox;
    private Party vote1;
    private Party vote2;
    private Party vote3;
    private Province province;

    public Vote(int ballotBox, Province province, Party vote1, Party vote2, Party vote3) {
        this.vote1= vote1;
        this.vote2= vote2;
        this.vote3= vote3;
        this.ballotBox = ballotBox;
        this.province=province;
    }

    public int getBallotBox() {
        return ballotBox;
    }

    public Party getVote1() {
        return vote1;
    }

    public Optional<Party> getVote2() {
        return Optional.ofNullable(vote2);
    }

    public Optional<Party> getVote3() {
        return Optional.ofNullable(vote3);
    }

    public Province getProvince() {
        return province;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(vote1.name());
        getVote2().ifPresent(party -> sb.append(',').append(party.name()));
        getVote3().ifPresent(party -> sb.append(',').append(party.name()));
        return sb.toString();
    }
}
