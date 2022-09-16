package cn.kong.web.job;

import cn.kong.web.config.JobCleanupConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 定时任务
 * 清理Archive的文件
 */
@Component
@ConditionalOnProperty(prefix = "job.cleanup", value = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class LogCleanupJob {
    @Autowired
    private JobCleanupConfig config;

    @Scheduled(cron = "${job.cleanup.cron}")
    public void run() {
        log.info("开始运行清理任务");
        long current = System.currentTimeMillis();
        AtomicLong count = new AtomicLong(0);

        for (String dir : config.getFiles()) {
            File file = new File(dir);
            if (!file.exists()) {
                continue;
            }
            //递归获取文件, 超过30天的文件清除
            FileUtils.listFiles(file, null, true).stream().forEach(f -> {
                long updateTs = f.lastModified();
                if (current - updateTs >= 24L * 3600 * 1000 * config.getDays()) {
                    count.incrementAndGet();
                    FileUtils.deleteQuietly(f);
                }
            });
        }
        log.info("清理任务完成, 总计清理{}", count.get());
    }
}
