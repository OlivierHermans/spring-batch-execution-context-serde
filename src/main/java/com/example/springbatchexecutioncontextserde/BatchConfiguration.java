package com.example.springbatchexecutioncontextserde;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
class BatchConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES, true);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ExecutionContextSerializer serializer(ObjectMapper objectMapper) {
        final Jackson2ExecutionContextStringSerializer serializer = new Jackson2ExecutionContextStringSerializer();
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    @Bean
    public BatchConfigurer configurer(DataSource dataSource, ExecutionContextSerializer serializer) {
        return new DefaultBatchConfigurer() {
            @Override
            protected JobRepository createJobRepository() throws Exception {
                final JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
                factory.setDataSource(dataSource);
                factory.setTransactionManager(getTransactionManager());
                factory.setSerializer(serializer);
                factory.afterPropertiesSet();
                return factory.getObject();
            }

            // The ExecutionContextSerializer configured in the job explorer is used to read the last run's execution
            // context (getLastJobExecution in SimpleJobExplorer). It needs to be the same as the serializer used in the
            // job repository.
            @Override
            protected JobExplorer createJobExplorer() throws Exception {
                final JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
                jobExplorerFactoryBean.setDataSource(dataSource);
                jobExplorerFactoryBean.setSerializer(serializer);
                jobExplorerFactoryBean.afterPropertiesSet();
                return jobExplorerFactoryBean.getObject();
            }
        };
    }
}
