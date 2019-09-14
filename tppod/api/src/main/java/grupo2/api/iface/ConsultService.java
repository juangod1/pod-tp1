package grupo2.api.iface;

import grupo2.api.model.ElectionResults;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.Province;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConsultService extends Remote {
    ElectionResults consultTotal() throws RemoteException, ElectionStateException;
    ElectionResults consultProvince(Province province) throws RemoteException,ElectionStateException;
    ElectionResults consultTable(int tableId) throws RemoteException,ElectionStateException;
}
