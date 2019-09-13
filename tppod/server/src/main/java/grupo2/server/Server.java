package grupo2.server;

import grupo2.api.*;
import grupo2.server.service.AdministrationServiceImpl;
import grupo2.server.service.FiscalizationServiceImpl;
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
        final AdministrationService adminService = new AdministrationServiceImpl(new ElectionManager());
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
            ((FiscalizationServiceImpl) fiscalizationService).newVote(new Vote( 100, Province.JUNGLE, Collections.singletonList(Party.TIGER)));
        }
        catch(RemoteException e) {
            LOGGER.info("Remote exception.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
