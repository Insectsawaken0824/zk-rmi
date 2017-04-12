package server;

import common.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhao on 2017/4/11.
 */
public class ZKServerProvider {

    public static final Logger logger = LoggerFactory.getLogger(ZKServerProvider.class);

    // 用于等待 SyncConnected 事件触发后继续执行当前线程
    private CountDownLatch latch = new CountDownLatch(1);

    // 发布 RMI 服务并注册 RMI 地址到 ZooKeeper 中
    public void publish(Remote remote,String host,int port){
        //发布RMI服务并返回地址
        String url = publishService(remote, host, port);
        if(url != null){
            //zookeeper
            ZooKeeper zooKeeper = connectZK();
            if(zooKeeper != null){
                //创建znode
                createNode(zooKeeper,url);
            }
        }
    }

    //创建ZNode
    private void createNode(ZooKeeper zooKeeper, String url) {
        if (zooKeeper == null || url == null || "".equals(url.trim())){
            return;
        }
        try {
            zooKeeper.create(Constant.ZK_PROVIDER_PATH, url.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException | InterruptedException e) {
            logger.error("", e);
        }
    }

    //发布RMI服务
    private String publishService(Remote remote,String host,int port) {
        String url = null;
        try {
            LocateRegistry.createRegistry(port);
            String tempUrl = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            Naming.rebind(tempUrl,remote);
            url = tempUrl;
            logger.debug("publish rmi service (url: {})", url);
        } catch (RemoteException | MalformedURLException e) {
            logger.error("", e);
        }
        return url;
    }

    private ZooKeeper connectZK(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected){
                        //对CountDownLatch进行-1操作,当等于0时唤醒线程
                        latch.countDown();
                    }
                }
            });
            //使当前线程进入等待状态,当等于0时不会执行
            latch.await();
        } catch (IOException | InterruptedException e) {
            logger.error("", e);
        }
        return zk;
    }
}
