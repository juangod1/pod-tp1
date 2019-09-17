package grupo2.client;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import grupo2.api.iface.VotingService;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.Party;
import grupo2.api.model.Province;
import grupo2.api.model.Vote;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VoteClient {

    public static void main(String[] args){

        String path = System.getProperty("votesPath");
        String ipAdd = System.getProperty("serverAddress");

       List<Vote> votes = parseVotes(path);
       sendVotes(votes,ipAdd);
    }

    private static List<Vote> parseVotes(String path) {
        List<Vote> votes = new ArrayList<>();
        CSVParser semiColonParser = new CSVParserBuilder().withSeparator(';').build();
        try (Reader reader = Files.newBufferedReader(Paths.get(path));
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(semiColonParser).build()) {

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                int mesa = Integer.valueOf(nextRecord[0]);
                Province province = Province.valueOf(nextRecord[1]);
                String[] votos = nextRecord[2].split(",");
                List<Party> ranking = Arrays.stream(votos).map(Party::valueOf).collect(Collectors.toList());
                votes.add(new Vote(mesa,province,ranking));
            }
        } catch (IOException e) {
            System.err.println("Unexpected path: '"+e.getMessage()+"'");
            System.exit(-1);
        }
        return votes;
    }

    private static void sendVotes(List<Vote> votes, String ipAddress) {
        try {
            final VotingService handle = (VotingService) Naming.lookup("//" + ipAddress + "/voting-service");

            votes.parallelStream().forEach(vote -> {
                try {
                    handle.addVote(vote);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (ElectionStateException e) {
                    System.err.println(e.getMessage());
                }
            });
            //for(Vote v : votes){
            //    handle.addVote(v); //todo: send votes in batch?
            //}
        }
    //    catch (ElectionStateException e){
    //        System.err.println(e.getMessage());
    //        System.exit(-1);
    //    }
        catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Unexpected ipAddress: '"+e.getMessage()+"'"); //todo: handle remote exceptions...
            System.exit(-1);
        }
    }
}
