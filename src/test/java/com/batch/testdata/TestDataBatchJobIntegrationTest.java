package com.batch.testdata;

import com.order.model.entity.Order;
import com.order.repository.OrderRepository;
import com.util.MySQLIntegrationTestContainerSupportUtil;
import com.util.MySQLSliceTestContainerSupportUtil;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.batch.test.JobLauncherTestUtils;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@SpringBatchTest
class TestDataBatchJobIntegrationTest extends MySQLIntegrationTestContainerSupportUtil {

    @Autowired
    private TestDataBatchConfig config;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemReader<Order> orderTestDataReader;

    @Autowired
    private ItemWriter<Order> orderTestDataWriter;

    @DisplayName("[통합테스트] Job 테스트 : Batch Job의 정상적인 수행이 이루어지는가")
    @Test
    void orderTestDataJob() throws Exception {
        // given : 5개씩 총 10개, 2번
        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addLong("totalCount", 5L)
                        .addLong("chunkSize", 5L)
                        //.addLong("runId", System.currentTimeMillis())
                        .toJobParameters();

        // when
        JobExecution jobExecution =
                jobLauncherTestUtils.launchJob(jobParameters);

        // then : BatchStatus
        assertEquals(
                BatchStatus.COMPLETED,
                jobExecution.getStatus()
        );

        //then : 최종적으로 적재된 데이터 row 수는 10개
        assertEquals(
                5,
                orderRepository.count()
        );
    }

    /*
    * 중복 커버리지 제거
    * */
//    @DisplayName("[단위테스트] Step 테스트 : 테스트 데이터를 정상적으로 생성하고 전달하는가")
//    @Test
//    void orderTestDataReader() throws Exception {
//        //given
//        JobParameters jobParameters =
//                new JobParametersBuilder()
//                        .addLong("totalCount", 5L)
//                        .addLong("chunkSize", 5L)
//                        .toJobParameters();
//
//        //Reader가 실제 StepExecution 기반으로 Step을 동작하도록 실행
//        JobExecution jobExecution =
//                jobLauncherTestUtils.launchStep(
//                        "orderTestDataStep",
//                        jobParameters
//                );
//
//        // then : 해당 Step를 실행하였을때 상태를 반환한다(*Reader의 상태 반환).
//        assertEquals(
//                BatchStatus.COMPLETED,
//                jobExecution.getStatus()
//        );
//
//        // then : 해당 Step을 실행하였을때 최종적으로 5개의 테스트데이터가 생성된다.
//        assertEquals(
//                5,
//                orderRepository.count()
//        );
//    }
//
//    @DisplayName("[단위테스트] Writer 테스트 : 읽은 데이터를 정상적으로 Write 하는가")
//    @Test
//    void orderTestDataWriter() throws Exception {
//        // given
//        List<Order> orders =
//                Instancio.ofList(Order.class)
//                        .size(5)
//                        .create();
//
//        // when
//        orderTestDataWriter.write(new Chunk<>(orders));
//
//        // then : writer에서 중요한 것은 최종적으로 데이터가 생성이 되었는가.
//        assertEquals(
//                5,
//                orderRepository.count()
//        );
//    }
}