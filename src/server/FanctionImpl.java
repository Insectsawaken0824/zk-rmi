package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by zhao on 2017/4/10.
 */
public class FanctionImpl extends UnicastRemoteObject implements FanctionService {
    protected FanctionImpl() throws RemoteException {
    }

    @Override
    public String  doSomething(String arg) throws RemoteException{
        return String.format("do %s", arg);
    }
}
