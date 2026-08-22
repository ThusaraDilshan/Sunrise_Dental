package model;

import java.io.Serializable;

public class Notice implements Serializable {

    private int noticeId;
    private int dentistId;
    private String description;
    private String sentBy;
    private boolean isRead;
    private String createdAt;

    public Notice() {
    }

    public Notice(int noticeId, int dentistId, String description, String sentBy,
                   boolean isRead, String createdAt) {
        this.noticeId = noticeId;
        this.dentistId = dentistId;
        this.description = description;
        this.sentBy = sentBy;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Used when staff creates a new notice (before it has an id / created_at)
    public Notice(int dentistId, String description, String sentBy) {
        this.dentistId = dentistId;
        this.description = description;
        this.sentBy = sentBy;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notice{" +
                "noticeId=" + noticeId +
                ", dentistId=" + dentistId +
                ", description='" + description + '\'' +
                ", sentBy='" + sentBy + '\'' +
                ", isRead=" + isRead +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}