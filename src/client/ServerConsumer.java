package client;

import common.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zhao on 2017/4/11.
 */
public class ServerConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ServerConsumer.class);

    // 用于等待 SyncConnected 事件触发后继续执行当前线程
    private CountDownLatch latch = new CountDownLatch(1);

    // 定义一个 volatile 成员变量，用于保存最新的 RMI 地址（考虑到该变量或许会被其它线程所修改，一旦修改后，该变量的值会影响到所有线程）
    // volatile让变量每次在使用的时候，都从主存中取。而不是从各个线程的“工作内存”。
    // volatile具有synchronized关键字的“可见性”，但是没有synchronized关键字的“并发正确性”，也就是说不保证线程执行的有序性。
    // 也就是说，volatile变量对于每次使用，线程都能得到当前volatile变量的最新值。但是volatile变量并不保证并发的正确性。
    private volatile List<String> urlList = new ArrayList<>();

    public ServerConsumer() {
        ZooKeeper zooKeeper = connectZKServer();
        if (zooKeeper != null){
            // 观察 /registry 节点的所有子节点并更新 urlList 成员变量
            watchNode(zooKeeper);
        }
    }

    private void watchNode(final ZooKeeper zooKeeper) {
        try {
            List<String> node_list = zooKeeper.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        // 若子节点有变化，则重新调用该方法（为了获取最新子节点中的数据）
                        watchNode(zooKeeper);
                    }
                }
            });
            //存储目录下的数据
            List<String> data_list = new ArrayList<String>();
            //遍历目录(多个server注册)
            for (String node : node_list){
                byte[] data = zooKeeper.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                data_list.add(new String(data));
            }
            urlList=data_list;
            logger.debug("node data: {}", data_list);
        } catch (KeeperException | InterruptedException e) {
            logger.error("", e);
        }
    }

    private ZooKeeper connectZKServer() {
        ZooKeeper zk = null;
        try {
            new ZooKeeper(Constant.ZK_PROVIDER_PATH, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            logger.error("", e);
        }
        return zk;
    }

    // 查找 RMI 服务
    public <T extends Remote> T lookup() {
        T service = null;
        int size = urlList.size();
        if (size > 0) {
            String url;
            if (size == 1) {
                url = urlList.get(0); // 若 urlList 中只有一个元素，则直接获取该元素
                logger.debug("using only url: {}", url);
                System.out.println(url);
            } else {
                url = urlList.get(ThreadLocalRandom.current().nextInt(size)); // 若 urlList 中存在多个元素，则随机获取一个元素
                logger.debug("using random url: {}", url);
                System.out.println(url);
            }
            service = lookupService(url); // 从 JNDI 中查找 RMI 服务
        }
        return service;
    }

    private <T> T lookupService(String url) {
        T remote = null;
        try {
            remote = (T) Naming.lookup(url);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            if (e instanceof ConnectException) {
                // 若连接中断，则使用 urlList 中第一个 RMI 地址来查找（这是一种简单的重试方式，确保不会抛出异常）
                logger.error("ConnectException -> url: {}", url);
                if (urlList.size() != 0) {
                    url = urlList.get(0);
                    return lookupService(url);
                }
            }
            logger.error("", e);
        }
        return remote;
    }
}
