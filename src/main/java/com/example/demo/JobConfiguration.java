package com.example.demo;

import jdk.jshell.Snippet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class JobConfiguration {
    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job", jobRepository) //job name 설정
                .start(step)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        // tasklet = Step이 중지될 때 까지 execute 메서드가 계속 반복해서 수행하고 수행할 때 마다 독립적인 트랜잭션이 만들어진다.
        // 초기화, 저장 프로시저 실행, 알림 전송과 같은 job에서 일반적으로 실행
        final Tasklet tasklet = new Tasklet() {
            private int count = 0;
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                count++;
                if (count == 15) {
                    log.info("tasklet FINISHED");
                    return RepeatStatus.FINISHED;
                }
                log.info("tasklet CONTINUABLE {}", count);
                return RepeatStatus.CONTINUABLE;
            }
        };

        return new StepBuilder("step", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .build();
    }
}
