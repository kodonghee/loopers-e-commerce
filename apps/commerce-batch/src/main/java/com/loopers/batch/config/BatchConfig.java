package com.loopers.batch.config;

import com.loopers.batch.entity.AggregatedRanking;
import com.loopers.batch.processor.RankingAggregatorProcessor;
import com.loopers.batch.reader.MonthlyMetricsReader;
import com.loopers.batch.reader.WeeklyMetricsReader;
import com.loopers.batch.writer.MonthlyRankingWriter;
import com.loopers.batch.writer.WeeklyRankingWriter;
import com.loopers.collector.entity.ProductMetrics;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Step weeklyRankingStep(WeeklyMetricsReader weeklyMetricsReader,
                                  RankingAggregatorProcessor processor,
                                  WeeklyRankingWriter weeklyRankingWriter) {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<ProductMetrics, AggregatedRanking>chunk(100, transactionManager)
                .reader(weeklyMetricsReader.weeklyMetricsItemReader())
                .processor(processor)
                .writer(weeklyRankingWriter)
                .build();
    }

    @Bean
    public Step monthlyRankingStep(MonthlyMetricsReader monthlyMetricsReader,
                                   RankingAggregatorProcessor processor,
                                   MonthlyRankingWriter monthlyRankingWriter) {
        return new StepBuilder("monthlyRankingStep", jobRepository)
                .<ProductMetrics, AggregatedRanking>chunk(100, transactionManager)
                .reader(monthlyMetricsReader.monthlyMetricsItemReader())
                .processor(processor)
                .writer(monthlyRankingWriter)
                .build();
    }

    @Bean
    public Job weeklyRankingJob(Step weeklyRankingStep) {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(weeklyRankingStep)
                .build();
    }

    @Bean
    public Job monthlyRankingJob(Step monthlyRankingStep) {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .start(monthlyRankingStep)
                .build();
    }
}
