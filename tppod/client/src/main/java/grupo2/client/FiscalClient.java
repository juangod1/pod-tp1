package grupo2.client;

import grupo2.api.iface.FiscalizationService;
import grupo2.api.iface.VoteListener;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import static java.lang.System.exit;

public class FiscalClient {
    private static Logger LOGGER = LoggerFactory.getLogger(VoteListener.class);

    public static void main(String[] args) {
        Party party = Party.valueOf(System.getProperty("party"));
        int tableId = Integer.parseInt(System.getProperty("id"));
        String ipAdd = System.getProperty("serverAddress");

        registerFiscal(ipAdd,party,tableId);
    }

    private static void registerFiscal(String ipAdd, Party party, int tableId) {
        Fiscal fiscal = new Fiscal(party);
        try {
            UnicastRemoteObject.exportObject(fiscal,0);
            final FiscalizationService handle = (FiscalizationService) Naming.lookup("//" + ipAdd + "/fiscalization-service");
            boolean ret = handle.register(fiscal, party, tableId);
            if(ret)
                System.out.println("Fiscal of party " + party + " registered on polling place " + tableId);
        }catch (ElectionStateException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        catch(RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
            exit(-1);
        }
    }
}
