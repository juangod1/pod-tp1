package grupo2.client;

import grupo2.api.iface.AdministrationService;
import grupo2.api.model.ElectionStateException;
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
        ActionName action = ActionName.valueOf(System.getProperty("actionName"));
        String ipAdd = System.getProperty("serverAddress");

        executeAction(ipAdd,action);
    }

    private static void executeAction(String ipAdd, ActionName action) {
        try{
            final AdministrationService handle = (AdministrationService) Naming.lookup("//" + ipAdd + "/administration-service");
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
        }catch (ElectionStateException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (RemoteException |NotBoundException |MalformedURLException e) {
            System.err.println("Unexpected IpAddress: '"+e.getMessage()+"'");//todo: handle remote exceptions...
            System.exit(-1);
        }
    }

    enum ActionName{
        open,
        state,
        close
    }
}
