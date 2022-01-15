package com.example.springbatchexecutioncontextserde;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Slf4j
@Configuration
class SimpleBatchJob {

    @Bean
    public Step firstTasklet(StepBuilderFactory factory) {
        return factory
                .get("firstTasklet")
                .tasklet(((stepContribution, chunkContext) -> {
                    final ExecutionContext context = stepContribution.getStepExecution().getExecutionContext();
                    context.put("person", new Person("Pipo", "De Clown", LocalDate.now()));
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step secondTasklet(StepBuilderFactory factory) {
        return factory
                .get("secondTasklet")
                .tasklet(((stepContribution, chunkContext) -> {
                    final ExecutionContext context =
                            stepContribution.getStepExecution().getJobExecution().getExecutionContext();
                    final Person person = (Person) context.get("person");
                    log.info("Person: {}", person);
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory factory, Step firstTasklet, Step secondTasklet) {
        return factory
                .get("simpleJob")
                .start(firstTasklet)
                .next(secondTasklet)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        final ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] { "person" });
        return listener;
    }
}
