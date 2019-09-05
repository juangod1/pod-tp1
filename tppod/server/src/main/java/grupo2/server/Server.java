package grupo2.server;

import grupo2.api.AdministrationService;
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

        try {
            final Remote remote = UnicastRemoteObject.exportObject(adminService,0);
            final Registry registry = LocateRegistry.getRegistry();
            registry.rebind("administration-service", remote);
            LOGGER.info("Administration Service bound.");
        }
        catch(RemoteException e) {
            LOGGER.info("Remote exception.");
        }
    }
}
