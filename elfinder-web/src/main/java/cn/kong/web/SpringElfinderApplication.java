package cn.kong.web;

import cn.kong.web.config.JobArchiveConfig;
import cn.kong.web.config.JobCleanupConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({JobArchiveConfig.class, JobCleanupConfig.class})
public class SpringElfinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringElfinderApplication.class, args);
	}
}
