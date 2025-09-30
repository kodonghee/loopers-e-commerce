package com.loopers.batch.config;

import com.loopers.batch.entity.AggregatedRanking;
import com.loopers.batch.processor.RankingAggregatorProcessor;
import com.loopers.batch.reader.MonthlyMetricsReader;
import com.loopers.batch.reader.WeeklyMetricsReader;
import com.loopers.batch.writer.MonthlyHistoryRankingWriter;
import com.loopers.batch.writer.MonthlyMvRankingWriter;
import com.loopers.batch.writer.WeeklyHistoryRankingWriter;
import com.loopers.batch.writer.WeeklyMvRankingWriter;
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
    public Step weeklyMvRankingStep(WeeklyMetricsReader weeklyMetricsReader,
                                  RankingAggregatorProcessor processor,
                                  WeeklyMvRankingWriter weeklyMvRankingWriter) {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<ProductMetrics, AggregatedRanking>chunk(100, transactionManager)
                .reader(weeklyMetricsReader.weeklyMetricsItemReader())
                .processor(processor)
                .writer(weeklyMvRankingWriter)
                .build();
    }

    @Bean
    public Step weeklyHistoryRankingStep(WeeklyMetricsReader weeklyMetricsReader,
                                         RankingAggregatorProcessor processor,
                                         WeeklyHistoryRankingWriter weeklyHistoryRankingWriter) {
        return new StepBuilder("weeklyHistoryRankingStep", jobRepository)
                .<ProductMetrics, AggregatedRanking>chunk(100, transactionManager)
                .reader(weeklyMetricsReader.weeklyMetricsItemReader())
                .processor(processor)
                .writer(weeklyHistoryRankingWriter)
                .build();
    }

    @Bean
    public Step monthlyMvRankingStep(MonthlyMetricsReader monthlyMetricsReader,
                                     RankingAggregatorProcessor processor,
                                     MonthlyMvRankingWriter monthlyMvRankingWriter) {
        return new StepBuilder("monthlyRankingStep", jobRepository)
                .<ProductMetrics, AggregatedRanking>chunk(100, transactionManager)
                .reader(monthlyMetricsReader.monthlyMetricsItemReader())
                .processor(processor)
                .writer(monthlyMvRankingWriter)
                .build();
    }

    @Bean
    public Step monthlyHistoryRankingStep(MonthlyMetricsReader monthlyMetricsReader,
                                          RankingAggregatorProcessor processor,
                                          MonthlyHistoryRankingWriter monthlyHistoryRankingWriter) {
        return new StepBuilder("monthlyHistoryRankingStep", jobRepository)
                .<ProductMetrics, AggregatedRanking>chunk(100, transactionManager)
                .reader(monthlyMetricsReader.monthlyMetricsItemReader())
                .processor(processor)
                .writer(monthlyHistoryRankingWriter)
                .build();
    }

    @Bean
    public Job weeklyRankingJob(Step weeklyMvRankingStep, Step weeklyHistoryRankingStep) {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(weeklyMvRankingStep)
                .next(weeklyHistoryRankingStep)
                .build();
    }

    @Bean
    public Job monthlyRankingJob(Step monthlyMvRankingStep, Step monthlyHistoryRankingStep) {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .start(monthlyMvRankingStep)
                .next(monthlyHistoryRankingStep)
                .build();
    }
}
