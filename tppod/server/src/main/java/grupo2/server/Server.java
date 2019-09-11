package grupo2.server;

import grupo2.api.AdministrationService;
import grupo2.api.FiscalizationService;
import grupo2.api.Party;
import grupo2.api.Vote;
import grupo2.server.service.AdministrationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        LOGGER.info("tppod Server Starting ...");
        bindServices();
    }

    public static void bindServices(){
        final AdministrationService adminService = new AdministrationServiceImpl();
        final FiscalizationService fiscalizationService = new FiscalizationServiceImpl();

        try {
            final Registry registry = LocateRegistry.getRegistry();

            final Remote remoteAdmin = UnicastRemoteObject.exportObject(adminService,0);
            final Remote remoteFiscal = UnicastRemoteObject.exportObject(fiscalizationService,0);
            registry.rebind("administration-service", remoteAdmin);
            LOGGER.info("Administration Service bound.");

            registry.rebind("fiscalization-service", remoteFiscal);
            LOGGER.info("Fiscalization service bound.");

            Thread.sleep(30_000);
            ((FiscalizationServiceImpl) fiscalizationService).newVote(new Vote(Party.TIGER, 100, 2));
        }
        catch(RemoteException e) {
            LOGGER.info("Remote exception.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
