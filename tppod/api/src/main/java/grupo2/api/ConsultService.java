package grupo2.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConsultService extends Remote {
    ElectionResults consultTotal() throws RemoteException;
    ElectionResults consultProvince(Province province) throws RemoteException;
    ElectionResults consultTable(int tableId) throws RemoteException;
}
