package cn.jeffxue.test;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class JedisTest {

    public static void main(String[] args) throws InterruptedException {

        Set<String> sentinels = new HashSet<String>();

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(100);

        sentinels.add("192.168.1.200:26379");
        sentinels.add("192.168.1.200:26380");
        sentinels.add("192.168.1.200:26381");

        JedisSentinelPool sentinelPool = new JedisSentinelPool("mymaster", sentinels, poolConfig, 300, "123456");

        Jedis jedis = sentinelPool.getResource();

        System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());

        String key = "mykey";

        String cacheData = jedis.get(key);

        if (cacheData == null) {
            jedis.del(key);
        }

        // 写入
        jedis.set(key, "first write");

        // 读取
        System.out.println(jedis.get(key));

        // down掉master，观察slave是否被提升为master
        System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());

        // 测试新master的写入
        try {
            jedis.set(key, "second write");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 观察读取是否正常
        try {
            System.out.println(jedis.get(key));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());

        //重新获取
        jedis = sentinelPool.getResource();

        // 观察读取是否正常
        jedis.set(key, "third write");

        // 观察读取是否正常
        System.out.println(jedis.get(key));

        sentinelPool.close();
        jedis.close();

    }

}
