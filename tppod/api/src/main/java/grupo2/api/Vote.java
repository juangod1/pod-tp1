package grupo2.api;

import java.io.Serializable;
import java.util.List;

public class Vote implements Serializable {
    static final long serialVersionUID = 146L;
    private final int ballotBox;
    private List<Party> ranking;
    private Province province;

    public Vote(int ballotBox, Province province, List<Party> ranking) {
        if(ranking.isEmpty()) {
            throw new IllegalArgumentException("Ranking can't be empty");
        }
        this.ranking = ranking;
        this.ballotBox = ballotBox;
        this.province=province;
    }

    public int getBallotBox() {
        return ballotBox;
    }

    public Party getTopVote() {
        return ranking.get(0);
    }

    public Province getProvince() {
        return province;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        ranking.forEach(v -> sb.append(',').append(v));
        return sb.toString();
    }

    public List<Party> getRanking() {
        return ranking;
    }
}
