package com.checkpoint.strategy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckPointSelector {

    /*
    * 전략패턴을 여기서 정의해준다.
    * */
    @Bean
    public CheckPointStrategy checkPointStrategy() {
        return new InMemoryCheckPointStrategy();
    }

    /*
    * DB/File/...
    * */
}
