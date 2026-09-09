package com.batch.testdata;

import com.order.model.entity.Order;
import com.order.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static org.instancio.Select.field;

@Configuration
//@RequiredArgsConstructor
public class TestDataBatchConfig {

    /*
    * instancio로 생성할 데이터 갯수 = 10만개
    * chunk 단위 = 5000개 (총 20번 수행)
    * batch를 통해 Order 데이터를 그대로 사용할것이기에 별도 Record 구성하지 않고 바로 Order Entity로 테스트 데이터 생성.
    * */
    //private static final int TOTAL_COUNT = 500_000;
    //private static final int CHUNK_SIZE = 5_000;

    @Bean
    public Job orderTestDataJob(
            JobRepository jobRepository,
            Step orderTestDataStep
    ) {
        return new JobBuilder("orderTestDataJob", jobRepository)
                .start(orderTestDataStep)
                .build();
    }

    @Bean
    @JobScope
    public Step orderTestDataStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Order> orderTestDataReader,
            ItemWriter<Order> orderTestDataWriter,
            @Value("#{jobParameters['chunkSize']}") int chunkSize
    ) {
        return new StepBuilder("orderTestDataStep", jobRepository)
                .<Order, Order>chunk(chunkSize, transactionManager)
                .reader(orderTestDataReader)
                .writer(orderTestDataWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Order> orderTestDataReader(
            @Value("#{jobParameters['totalCount']}") int totalCount
    ) {

        return new ItemReader<>() {

            private long count = 0;

            @Override
            public Order read() {

                if (count >= totalCount) {
                    return null;
                }

                count++;

                /*
                * testable한 로직 작성과 DB Identity에 의한 채번은 EntityInformation이 merge의 기준으로 판단할 수 있다.
                * persist, merge의 기준은 결국 id, 이에 대한 generatedValue 정책도 적절하게 선택 필요하다.
                * */

                return Instancio.of(Order.class)
                        .ignore(field(Order.class, "orderId"))
                        .create();
            }
        };
    }

    @Bean
    public ItemWriter<Order> orderTestDataWriter(
            OrderRepository orderRepository
    ) {
        return chunk -> orderRepository.saveAll(chunk);
    }

}