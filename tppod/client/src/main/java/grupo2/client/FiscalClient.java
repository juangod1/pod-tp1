package grupo2.client;

import grupo2.api.*;
import jdk.nashorn.internal.runtime.logging.DebugLogger;
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

public class FiscalClient {
    private static Logger LOGGER = LoggerFactory.getLogger(VoteListener.class);


    public static void main(String[] args) {
       /* String serverAddress = System.getProperty("serverAddress");

        if (serverAddress == null) {
            System.err.println("serverAddress must be specified");
            return;
        }

        String idString = System.getProperty("id");
        if (idString == null) {
            System.err.println("id must be specified");
            return;
        }
        int id = Integer.parseInt(idString);

        String partyString = System.getProperty("partyName");
        if (partyString == null) {
            System.err.println("serverAddress must be specified");
            return;
        }

        Party party;
        try {
            party = Party.valueOf(partyString);
        } catch (IllegalArgumentException e) {
            System.err.println("The specified party is not valid");
            return;
        }

        */
        Fiscal fiscal = new Fiscal();
        try {
            UnicastRemoteObject.exportObject(fiscal,0);
            final FiscalizationService handle = (FiscalizationService) Naming.lookup("//localhost:1099/fiscalization-service");
            handle.register(fiscal, Party.TIGER, 1); // TODO: PArams
        }
        catch(RemoteException e) {
            LOGGER.info("Remote exception.");
        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
