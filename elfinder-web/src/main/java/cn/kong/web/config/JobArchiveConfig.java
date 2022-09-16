package cn.kong.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "job.archive")
@Data
public class JobArchiveConfig {
    private boolean enabled;
    private String cron;
    private Integer days=7;
    private List<String> compressionFormat;
    private List<String> files;

    public JobArchiveConfig(){
        this.compressionFormat = new ArrayList<>();
        this.compressionFormat.add("log");
        this.compressionFormat.add("txt");
    }
}
