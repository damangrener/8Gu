package org.example.jedis;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WTF
 * @date 2024/3/15 13:59
 */
public class JedisTest {

    public static void main(String[] args) {
        JedisPool pool = new JedisPool("172.16.10.130",6379);

        try(Jedis jedis = pool.getResource();) {
            jedis.set("foo","bar");
            System.out.println(jedis.get("foo"));

            Map<String, String> hash = new HashMap<>();;
            hash.put("name", "John");
            hash.put("surname", "Smith");
            hash.put("company", "Redis");
            hash.put("age", "29");
            jedis.hset("user_session:123",hash);
            System.out.println(jedis.hgetAll("user_session:123"));
            System.out.println(jedis.hget("user_session:123","age"));
        }

    }

    @Test
    public void rank(){
        JedisPool pool = new JedisPool("172.16.10.130",6379);

        try(Jedis jedis = pool.getResource();) {
            jedis.zadd("like_num",Math.random(),"a");
            jedis.zadd("like_num",Math.random(),"aa");
            jedis.zadd("like_num",Math.random(),"aaa");
            jedis.zadd("like_num",Math.random(),"aaaa");
            jedis.zadd("like_num",Math.random(),"aaaaa");

            List<String> likeNum = jedis.zrange("like_num", 0, 2);
            for (String s : likeNum) {
                System.out.println(s);
            }
        }
    }

}
