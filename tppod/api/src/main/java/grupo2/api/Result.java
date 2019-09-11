package grupo2.api;

public class Result {
    private float percentage;
    private Party party;

    public Result(Party party, float percentage){
        this.party=party;
        this.percentage=percentage;
    }

    public float getPercentage() {
        return percentage;
    }

    public Party getParty() {
        return party;
    }
}
