package com.sacco.sync;

public class OutboxItem {
    private final long id;
    private final String type;
    private final String payload;
    private final String status;

    public OutboxItem(long id, String type, String payload, String status) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }
}
