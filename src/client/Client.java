package client;

import common.Constant;
import server.FanctionService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by zhao on 2017/4/10.
 */
public class Client {
    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        FanctionService fanctionService = (FanctionService) Naming.lookup(Constant.RMI_URL);
        System.out.println(fanctionService.doSomething("client"));
    }
}
