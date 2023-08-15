package xyz.nofoot;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.nofoot.entity.User;
import xyz.nofoot.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Slf4j
@SpringBootTest
public class TokenTest extends ServiceImpl<UserMapper, User> {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Test
    void testCreateToken() throws IOException {
        List<User> allUser = query().list();
        List<User> users = allUser.subList(0, 1000);
        HashMap<String, String> userMap = new HashMap<>();
        FileWriter writer = new FileWriter("tokens.txt");


        for (User user : users) {
            String nickName = user.getNickName();
            Long id = user.getId();
            String token = UUID.randomUUID().toString(true);
            String key = "login:token:" + token;

            userMap.put("nickName", nickName);
            userMap.put("id", id.toString());
            stringRedisTemplate.opsForHash().putAll(key, userMap);

            writer.write(token + "\n");
        }
        writer.close();
    }
}
