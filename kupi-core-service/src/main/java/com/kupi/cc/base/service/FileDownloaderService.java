package com.kupi.cc.base.service;

import com.kupi.cc.base.exception.CouponUploadFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

@Service
@Slf4j
public class FileDownloaderService {

    private final Path baseDir;

    public FileDownloaderService(@Value("${couponsUpload.csv.saveLocation}") String saveLocation) {
        this.baseDir = Paths.get(saveLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
            try {
                Set<PosixFilePermission> dirPerms = PosixFilePermissions.fromString("rwx------");
                Files.setPosixFilePermissions(baseDir, dirPerms);
            } catch (UnsupportedOperationException ignored) {
                log.debug("POSIX file permissions not supported on this OS. Skipping permission set.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare save directory: " + baseDir, e);
        }
    }

    /**
     * Download a file from a HTTPS URL and save it under the configured directory.
     * The file name is sanitized and the file is created with restrictive permissions.
     */
    public void download(String url, String outputFileName) {
        try {
            URL u = URI.create(url).toURL();
            if (!"https".equalsIgnoreCase(u.getProtocol())) {
                throw new IllegalArgumentException("Only HTTPS is allowed for downloads");
            }
            String safeName = outputFileName.replaceAll("[^A-Za-z0-9._-]", "_");
            Path target = baseDir.resolve(safeName).normalize();
            if (!target.startsWith(baseDir)) {
                throw new SecurityException("Invalid output path");
            }
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(30_000);

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP status: " + code);
            }

            try (InputStream in = conn.getInputStream()) {
                Files.deleteIfExists(target);
                Files.createFile(target);
                try {
                    Set<PosixFilePermission> filePerms = PosixFilePermissions.fromString("rw-------");
                    Files.setPosixFilePermissions(target, filePerms);
                } catch (UnsupportedOperationException ignored) {
                    log.debug("File permissions not supported on this OS. Skipping permission set.");
                }
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("File:{} downloaded to {}", safeName, target);

        } catch (Exception e) {
            log.error("Downloading csv file failed.", e);
            throw new CouponUploadFailedException("Failed to download coupon csv file to the local.", e);
        }
    }
}
