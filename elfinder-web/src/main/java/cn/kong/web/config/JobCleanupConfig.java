package cn.kong.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "job.cleanup")
@Data
public class JobCleanupConfig {
    private boolean enabled;
    private String cron;
    private Integer days=30;
    private List<String> files;
}
