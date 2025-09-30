package com.loopers.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RankingJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    // 매일 새벽 3시 → 주간 MV만 갱신
    @Scheduled(cron = "0 0 3 * * *")
    public void runWeeklyMvJob() throws Exception {
        jobLauncher.run(
                weeklyRankingJob,
                new JobParametersBuilder()
                        .addLong("timestamp", Instant.now().toEpochMilli())
                        .addString("mode", "MV")
                        .toJobParameters()
        );
    }

    // 매주 월요일 새벽 3시 10분 → 주간 History 갱신
    @Scheduled(cron = "0 10 3 * * MON")
    public void runWeeklyHistoryJob() throws Exception {
        jobLauncher.run(
                weeklyRankingJob,
                new JobParametersBuilder()
                        .addLong("timestamp", Instant.now().toEpochMilli())
                        .addString("mode", "HISTORY")
                        .toJobParameters()
        );
    }

    // 매일 새벽 3시 30분 → 월간 MV 갱신
    @Scheduled(cron = "0 30 3 * * *")
    public void runMonthlyMvJob() throws Exception {
        jobLauncher.run(
                monthlyRankingJob,
                new JobParametersBuilder()
                        .addLong("timestamp", Instant.now().toEpochMilli())
                        .addString("mode", "MV")
                        .toJobParameters()
        );
    }

    // 매월 1일 새벽 3시 40분 → 월간 History 갱신
    @Scheduled(cron = "0 40 3 1 * *")
    public void runMonthlyHistoryJob() throws Exception {
        jobLauncher.run(
                monthlyRankingJob,
                new JobParametersBuilder()
                        .addLong("timestamp", Instant.now().toEpochMilli())
                        .addString("mode", "HISTORY")
                        .toJobParameters()
        );
    }
}
