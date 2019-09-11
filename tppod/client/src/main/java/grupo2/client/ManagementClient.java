package grupo2.client;

import grupo2.api.AdministrationService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ManagementClient {
    private static Logger logger = LoggerFactory.getLogger(ManagementClient.class);

    public static void main(String[] args){
        logger.info("tppod ManagementClient Starting ...");

        CommandLine parsedCommandLine = null;
        try{
            parsedCommandLine = getParsedCommandLine(args);
        }
        catch (ParseException e){
            System.err.println("Unexpected Command line arguments: '"+e.getMessage()+"'");
            System.exit(-1);
        }

        ActionName action = ActionName.valueOf(parsedCommandLine.getOptionValue("N"));
        String ipAdd = parsedCommandLine.getOptionValue("A");

        executeAction(ipAdd,action);
    }

    private static void executeAction(String ipAdd, ActionName action) {
        try{
            final AdministrationService handle = (AdministrationService) Naming.lookup(ipAdd);
            switch (action){
                case open:
                    handle.openElection();
                    System.out.println("Election Open");
                    break;
                case close:
                    handle.closeElection();
                    System.out.println("Election Closed");
                    break;
                case state:
                    String output = String.format("The election state is %s.", handle.consultElectionState());
                    System.out.println(output);
                    break;
            }
        } catch (RemoteException |NotBoundException |MalformedURLException e) {
            System.err.println("Unexpected IpAddress: '"+e.getMessage()+"'");
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
        options.addOption(Option.builder("N")
                .required()
                .longOpt("Daction")
                .desc("El nombre de la acción a realizar.")
                .hasArg()
                .argName( "ACTIONNAME" )
                .build());
        return options;

    }

    enum ActionName{
        open,
        state,
        close
    }
}
