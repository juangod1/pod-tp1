package grupo2.client;

import grupo2.api.Party;
import grupo2.api.Province;
import grupo2.api.Vote;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static grupo2.api.Party.*;

public class TestClient {


    public static void main(String[] args) throws IOException {
        generateCSV(1,"client/src/main/resources/new.csv");
        System.setProperty("serverAddress", "127.0.0.1");
        management("open");
        vote("client/src/main/resources/new.csv");
        consulting("client/src/main/resources/national_parcial.csv", null, null);
        consulting("client/src/main/resources/provincial_parcial.csv", "JUNGLE", null);
        consulting("client/src/main/resources/table_parcial.csv", null, "0");
        management("close");
        consulting("client/src/main/resources/national_final.csv", null, null);
        consulting("client/src/main/resources/provincial_final.csv", "JUNGLE", null);
        consulting("client/src/main/resources/table_final.csv", null, "0");
    }


    public static void management(String actionName) {
        System.setProperty("actionName", actionName);
        ManagementClient.main(new String[]{});
    }

    public static void fiscal(Party p, int id) {
        System.setProperty("party", p.toString());
        System.setProperty("id", ""+id);
        FiscalClient.main(new String[]{});
    }

    public static void consulting(String outPath, String province, String table) {
        System.setProperty("outPath", outPath);

        if(province != null) {
            System.setProperty("state", province);
        } else {
            System.clearProperty("state");
        }
        if (table != null) {
            System.setProperty("id", table);
        } else {
            System.clearProperty("id");
        }

        ConsultingClient.main(new String[]{});
    }

    public static void vote(String filePath) {
        System.setProperty("votesPath", filePath);

        VoteClient.main(new String[]{});
    }



    private static List<Vote> generateVotes(int amount, int ballotBox, Province province, Party... parties) {
        List<Vote> votes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            votes.add(new Vote(ballotBox, province, Arrays.asList(parties)));
        }

        return votes;
    }


    private static void generateCSV(int multiplier, String path) throws IOException {
        Province p = Province.JUNGLE;
        List<Vote> votes = new ArrayList<>();
        votes.addAll(generateVotes(multiplier * 15, 0, p, TARSIER, WHITE_GORILLA));
        votes.addAll(generateVotes(multiplier * 32, 0, p, GORILLA, TARSIER, WHITE_GORILLA));
        votes.addAll(generateVotes(multiplier * 64, 0, p, GORILLA, WHITE_GORILLA));
        votes.addAll(generateVotes(multiplier * 9, 0, p, WHITE_GORILLA));
        votes.addAll(generateVotes(multiplier * 99, 0, p, OWL, TURTLE));
        votes.addAll(generateVotes(multiplier * 3, 0, p, TURTLE));
        votes.addAll(generateVotes(multiplier * 3, 0, p, SNAKE, TURTLE));
        votes.addAll(generateVotes(multiplier * 48, 0, p, TIGER));
        votes.addAll(generateVotes(multiplier * 12, 0, p, LYNX, TIGER));
        votes.addAll(generateVotes(multiplier * 6, 0, p, JACKALOPE));
        votes.addAll(generateVotes(multiplier * 6, 0, p, BUFFALO​, JACKALOPE));
        votes.addAll(generateVotes(multiplier * 3, 0, p, BUFFALO​, TURTLE));

        FileWriter csvWriter = new FileWriter(path);
        for(Vote v: votes) {
            csvWriter.append("" + v.getBallotBox());
            csvWriter.append(";");
            csvWriter.append(v.getProvince().toString());
            csvWriter.append(";");
            csvWriter.append(v.getRanking().get(0).toString());
            for(int i = 1; i < v.getRanking().size();i++) {
                csvWriter.append(",");
                csvWriter.append(v.getRanking().get(i).toString());
            }
            csvWriter.append("\n");
        }
        csvWriter.close();
    }

}
