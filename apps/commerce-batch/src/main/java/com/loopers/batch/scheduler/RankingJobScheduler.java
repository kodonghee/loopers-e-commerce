package com.loopers.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    // 매주 월요일 새벽 3시
    @Scheduled(cron = "0 0 3 * * MON")
    public void runWeeklyJob() throws Exception {
        jobLauncher.run(weeklyRankingJob, new JobParameters());
    }

    // 매월 1일 새벽 3시 30분
    @Scheduled(cron = "0 30 3 1 * *")
    public void runMonthlyJob() throws Exception {
        jobLauncher.run(monthlyRankingJob, new JobParameters());
    }
}
