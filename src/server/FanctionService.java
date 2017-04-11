package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by zhao on 2017/4/11.
 */
public interface FanctionService extends Remote{
    String doSomething(String arg) throws RemoteException;
}
