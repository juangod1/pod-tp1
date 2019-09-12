package grupo2.client;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import grupo2.api.*;
import org.apache.commons.cli.*;

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
import java.util.Optional;
import java.util.stream.Collectors;

public class VoteClient {

    public static void main(String[] args){
        CommandLine parsedCommandLine = null;
        try{
            parsedCommandLine = getParsedCommandLine(args);
        }
        catch (ParseException e){
            System.err.println("Unexpected Command line arguments: '"+e.getMessage()+"'");
            System.exit(-1);
        }

        String path = parsedCommandLine.getOptionValue("P");
        String ipAdd = parsedCommandLine.getOptionValue("A");
        System.out.println("ipAdd = '"+ipAdd+"', path ='"+path+"'");

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
        try{
            final VoteService handle = (VoteService) Naming.lookup(ipAddress);

            for(Vote v : votes){
                handle.registerVote(v);
            }
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Unexpected ipAddress: '"+e.getMessage()+"'");
            System.exit(-1);
        }
    }

    private static CommandLine getParsedCommandLine(String[] args) throws ParseException {
        Options options = initializeOptions();
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static Options initializeOptions() {
        final Options options = new Options();
        options.addOption(Option.builder("A")
                .longOpt("DserverAddress")
                .required()
                .desc( "Dirección IP y el puerto donde está publicado el servicio de votación."  )
                .hasArg()
                .argName( "IPADD" )
                .build());
        options.addOption(Option.builder("P")
                .required()
                .longOpt("DvotesPath")
                .desc( "Path del archivo de entrada con los votos de los ciudadanos"  )
                .hasArg()
                .argName( "PATH" )
                .build());
        return options;

    }
}
