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

    public void setId(DriveId mId) {
        this.mId = mId;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long mSize) {
        this.mSize = mSize;
    }

}
