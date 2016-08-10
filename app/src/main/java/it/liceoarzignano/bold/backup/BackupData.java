package it.liceoarzignano.bold.backup;


import com.google.android.gms.drive.DriveId;

import java.util.Date;

class BackupData {
    private DriveId id;
    private Date date;
    private long size;


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public DriveId getId() {
        return id;
    }

    public void setId(DriveId id) {
        this.id = id;
    }
}
