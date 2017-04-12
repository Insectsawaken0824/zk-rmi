package server;

import java.rmi.RemoteException;

/**
 * Created by zhao on 2017/4/11.
 */
public class RmiServerZK {
    public static void main(String[] args) throws Exception {

        ZKServerProvider zkServerProvider = new ZKServerProvider();
        FanctionService fanctionService = new FanctionImpl();
        zkServerProvider.publish(fanctionService,"127.0.0.1",8892);
        Thread.sleep(Long.MAX_VALUE);
    }
}
