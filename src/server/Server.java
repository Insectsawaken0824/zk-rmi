package server;

import common.Constant;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by zhao on 2017/4/10.
 */
public class Server {
    public static void main(String[] args) throws RemoteException, MalformedURLException {
        LocateRegistry.createRegistry(Constant.RMI_PORT);
        Naming.rebind(Constant.RMI_URL,new FanctionImpl());
    }
}
