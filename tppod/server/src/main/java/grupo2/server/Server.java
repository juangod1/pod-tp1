package grupo2.server;

import grupo2.server.service.HelloServerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import grupo2.api.HelloServerService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        LOGGER.info("tppod Server Starting ...");
        final HelloServerService hss = new HelloServerServiceImpl();

        final Remote remote = UnicastRemoteObject.exportObject(hss,0);
        final Registry registry = LocateRegistry.getRegistry();
        registry.rebind("hello-server", remote);
        LOGGER.info("HelloServerService bound");
    }
}
