package client;

import server.FanctionService;


/**
 * Created by zhao on 2017/4/11.
 */
public class RmiClientZK {
    public static void main(String[] args) throws Exception {
        while (true){
            ServerConsumer serverConsumer = new ServerConsumer();
            FanctionService fanctionService = serverConsumer.lookup();
            String client = fanctionService.doSomething("client");
            System.out.println(client);
            Thread.sleep(1000);
        }
    }

}
