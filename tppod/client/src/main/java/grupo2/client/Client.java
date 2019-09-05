package grupo2.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import grupo2.api.HelloServerService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        logger.info("tppod Client Starting ...");
        // Say Hello to the server!
        final HelloServerService handle = (HelloServerService)Naming.lookup("//localhost:1099/hello-server");
        String reply = handle.hello("Chelo");
        logger.info("Server replied: {}", reply);
    }
}
