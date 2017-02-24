package it.liceoarzignano.bold.backup;


import com.google.android.gms.drive.DriveId;

import java.util.Date;

class BackupData {
    private DriveId mId;
    private Date mDate;
    private long mSize;

    public DriveId getId() {
        return mId;
    }

    public void setId(DriveId id) {
        this.mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

}
