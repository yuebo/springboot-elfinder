package cn.kong.web.job;

import cn.kong.web.config.JobArchiveConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(prefix = "job.archive", value = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class LogArchiveJob {
    @Autowired
    private JobArchiveConfig config;

    @Value("${ARCHIVE_PATH:/opt/archive}")
    private String archiveRoot;

    @Scheduled(cron = "${job.archive.cron}")
    public void run() {
        log.info("开始运行归档任务");
        AtomicLong count = new AtomicLong(0);
        long current = System.currentTimeMillis();

        for (String dir : config.getFiles()) {
            File file = new File(dir);
            if (!file.exists()) {
                continue;
            }
            //递归获取文件，如果是支持压缩文件，则压缩，处理后将文件移动到archive，原文件删除
            FileUtils.listFiles(file, null, true).stream().forEach(f -> {
                long updateTs = f.lastModified();
                if (current - updateTs >= 24L * 3600 * 1000 * config.getDays()) {
                    int result = doArchive(file, f);
                    count.addAndGet(result);
                }
            });
        }

        log.info("归档任务完成，总计归档文件 {}", count);


    }

    private int doArchive(File root, File f) {
        String ext = FilenameUtils.getExtension(f.getName());
        File archive = new File(archiveRoot);
        //若支持压缩，则使用zip压缩文件后放到archive里面，否则直接拷贝过去
        if (config.getCompressionFormat().contains(ext)) {
            try {
                String path = StringUtils.replace(f.getCanonicalPath(), root.getCanonicalPath(), archive.getCanonicalPath());
                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    FileUtils.forceMkdir(file.getParentFile());
                }
                zip(path.concat(".zip"), Collections.singletonList(f), f.getParentFile().getCanonicalPath() + File.separator);
                FileUtils.deleteQuietly(f);
                return 1;
            } catch (Exception e) {
                log.error("error to zip file {}", e);
            }

        } else {
            try {
                String path = StringUtils.replace(f.getCanonicalPath(), root.getCanonicalPath(), archive.getCanonicalPath());
                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    FileUtils.forceMkdir(file.getParentFile());
                }
                FileUtils.copyFile(f, file);
                FileUtils.deleteQuietly(f);
                return 1;
            } catch (IOException e) {
                log.error("error to copy file {}", e);
            }
        }

        return 0;

    }

    private static void zip(String outPathStr, List<File> filesToArchive, String rootPath) throws Exception {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(Paths.get(outPathStr).toFile()));
        try (ZipArchiveOutputStream o = (ZipArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, out)) {
            for (File f : filesToArchive) {
                // 获取每个文件相对路径,作为在ZIP中路径
                ArchiveEntry entry = o.createArchiveEntry(f, StringUtils.replace(f.getCanonicalPath(), rootPath, ""));
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }

            o.finish();
        }
        IOUtils.closeQuietly(out);
    }
}
