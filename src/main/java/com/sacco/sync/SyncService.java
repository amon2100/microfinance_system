package com.sacco.sync;

import com.sacco.config.Database;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncService {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_SYNC_KEY = "CHANGE_ME";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final OutboxRepository outboxRepository = new OutboxRepository();

    private static final SyncService INSTANCE = new SyncService();

    private SyncService() {
    }

    public static SyncService getInstance() {
        return INSTANCE;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::syncOnce, 5, 15, TimeUnit.SECONDS);
    }

    private void syncOnce() {
        if (!isOnline()) {
            return;
        }
        try (var connection = Database.getConnection()) {
            for (OutboxItem item : outboxRepository.findPending(connection)) {
                try {
                    if ("MEMBER_CREATE".equals(item.getType())) {
                        postJson("/api/sync/members", item.getPayload());
                        outboxRepository.markSent(connection, item.getId());
                    } else if ("MEMBER_PHOTO".equals(item.getType())) {
                        sendPhoto(item.getPayload());
                        outboxRepository.markSent(connection, item.getId());
                    } else if ("USER_CREATE".equals(item.getType())) {
                        postJson("/api/sync/users", item.getPayload());
                        outboxRepository.markSent(connection, item.getId());
                    } else if ("SAVINGS_CREATE".equals(item.getType())) {
                        postJson("/api/sync/savings", item.getPayload());
                        outboxRepository.markSent(connection, item.getId());
                    } else if ("LOAN_CREATE".equals(item.getType())) {
                        postJson("/api/sync/loans", item.getPayload());
                        outboxRepository.markSent(connection, item.getId());
                    } else if ("TRANSACTION_CREATE".equals(item.getType())) {
                        postJson("/api/sync/transactions", item.getPayload());
                        outboxRepository.markSent(connection, item.getId());
                    }
                } catch (Exception ex) {
                    outboxRepository.markFailed(connection, item.getId(), ex.getMessage());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void postJson(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + path))
                .header("Content-Type", "application/json")
                .header("X-Sync-Key", getSyncKey())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Sync failed: " + response.statusCode());
        }
    }

    private void sendPhoto(String payload) throws IOException, InterruptedException {
        String[] parts = payload.split("\\|", 2);
        if (parts.length != 2) {
            throw new RuntimeException("Invalid photo payload");
        }
        String externalId = parts[0];
        String filePath = parts[1];
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Photo file missing");
        }
        String boundary = "----SyncBoundary" + UUID.randomUUID();
        String mimeType = "application/octet-stream";
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";
        String footer = "\r\n--" + boundary + "--\r\n";
        byte[] body = concat(header.getBytes(StandardCharsets.UTF_8), fileBytes, footer.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/sync/members/" + externalId + "/photo"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("X-Sync-Key", getSyncKey())
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Photo sync failed: " + response.statusCode());
        }
    }

    private boolean isOnline() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("1.1.1.1", 53), 1500);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String getBaseUrl() {
        return System.getProperty("backendUrl", DEFAULT_BASE_URL);
    }

    private String getSyncKey() {
        return System.getProperty("syncKey", DEFAULT_SYNC_KEY);
    }

    private byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] merged = new byte[length];
        int pos = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, merged, pos, part.length);
            pos += part.length;
        }
        return merged;
    }
}
