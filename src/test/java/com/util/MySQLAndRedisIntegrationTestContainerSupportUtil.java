package com.util;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
* TestContainer는 기본적으로 본인 스스로 컨테이너를 띄우고, 접속정보를 생성하여 테스트 컨테이너 환경을 생성한다.
* Spring은 이러한 접속정보를 datasource 객체에 주입하여, 테스트 시 해당 컨테이너 환경을 활용할 수 있도록 한다.
* 의존성 설정을 통해 container 환경을 구성할 수도 있지만, 좀 더 간편화하기 위함
* */
@Testcontainers
public class MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0.42");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:8.2.1")
            .withExposedPorts(6379);

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @DynamicPropertySource
    static void serviceConnections(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void beforeEach() {
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

//    @BeforeEach
//    protected void insertData() {
//        entityManager
//                .createNativeQuery("DELETE FROM article")
//                .executeUpdate();
//
//        Long boardId = 1L;
//        for (int i = 1; i <= 20; i++) {
//            Article article = Article.create(
//                    new ArticleCreateRequest((long) i, "test_" + i, boardId)
//            );
//            entityManager.persist(article);
//        }
//        entityManager.flush();
//        entityManager.clear();
//
//    }

}
