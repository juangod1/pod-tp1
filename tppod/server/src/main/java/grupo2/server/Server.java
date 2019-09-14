package grupo2.server;

import grupo2.api.*;
import grupo2.server.election.ElectionManager;
import grupo2.server.service.AdministrationServiceImpl;
import grupo2.server.service.ConsultingServiceImpl;
import grupo2.server.service.FiscalizationServiceImpl;
import grupo2.server.service.VotingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;

public class Server {
    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        LOGGER.info("tppod Server Starting ...");
        bindServices();
    }

    public static void bindServices(){
        ElectionManager manager = new ElectionManager();
        final AdministrationService adminService = new AdministrationServiceImpl(manager);
        final FiscalizationServiceImpl fiscalizationService = new FiscalizationServiceImpl(manager);
        final ConsultingServiceImpl consultingService = new ConsultingServiceImpl(manager);
        final VotingServiceImpl votingService = new VotingServiceImpl(manager);

        try {
            final Registry registry = LocateRegistry.getRegistry();

            final Remote remoteAdmin = UnicastRemoteObject.exportObject(adminService,0);
            final Remote remoteFiscal = UnicastRemoteObject.exportObject(fiscalizationService,0);
            final Remote remoteConsulting = UnicastRemoteObject.exportObject(consultingService,0);
            final Remote remoteVoting = UnicastRemoteObject.exportObject(votingService,0);

            registry.rebind("administration-service", remoteAdmin);
            LOGGER.info("Administration Service bound.");

            registry.rebind("fiscalization-service", remoteFiscal);
            LOGGER.info("Fiscalization service bound.");

            registry.rebind("consulting-service", remoteConsulting);
            LOGGER.info("Consulting Service bound.");

            registry.rebind("voting-service", remoteVoting);
            LOGGER.info("Voting Service bound.");
        }
        catch(RemoteException e) {
            LOGGER.info("Remote exception.");
        }
    }
}
