package grupo2.client;

import grupo2.api.*;
import jdk.nashorn.internal.runtime.logging.DebugLogger;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;

import static java.lang.System.exit;

public class FiscalClient {
    private static Logger LOGGER = LoggerFactory.getLogger(VoteListener.class);

    public static void main(String[] args) {
        CommandLine parsedCommandLine = null;
        try{
            parsedCommandLine = getParsedCommandLine(args);
        }
        catch (ParseException e){
            System.err.println("Unexpected Command line arguments: '"+e.getMessage()+"'");
            exit(-1);
        }

        Party party = Party.valueOf(parsedCommandLine.getOptionValue("N"));
        int tableId = Integer.parseInt(parsedCommandLine.getOptionValue("I"));
        String ipAdd = parsedCommandLine.getOptionValue("A");

        registerFiscal(ipAdd,party,tableId);
    }

    private static void registerFiscal(String ipAdd, Party party, int tableId) {
        Fiscal fiscal = new Fiscal();
        try {
            UnicastRemoteObject.exportObject(fiscal,0);
            final FiscalizationService handle = (FiscalizationService) Naming.lookup(ipAdd);
            handle.register(fiscal, party, tableId);
        }
        catch(RemoteException e) {
            LOGGER.info("Remote exception.");//todo: handle remote exceptions...
            exit(-1);
        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
            exit(-1);
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
        options.addOption(Option.builder("I")
                .required()
                .longOpt("Did")
                .desc("El número de la mesa de votación a fiscalizar")
                .hasArg()
                .argName( "NUMBER" )
                .build());
        options.addOption(Option.builder("N")
                .required()
                .longOpt("Dparty")
                .desc("El nombre del partido político del fiscal.")
                .hasArg()
                .argName("NAME")
                .build());
        return options;

    }
}
