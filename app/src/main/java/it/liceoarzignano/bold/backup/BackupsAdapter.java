package it.liceoarzignano.bold.backup;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

class BackupsAdapter extends RecyclerView.Adapter<BackupsAdapter.BackupHolder> {
    private final List<BackupData> mBackups;

    BackupsAdapter(List<BackupData> mBackups) {
        this.mBackups = mBackups;
    }

    @Override
    public BackupHolder onCreateViewHolder(ViewGroup mParent, int mType) {
        View mItem = LayoutInflater.from(mParent.getContext())
        .inflate(R.layout.item_backup, mParent, false);

        return new BackupHolder(mItem);
    }

    @Override
    public void onBindViewHolder(BackupHolder mHolder, int mPosition) {
        BackupData mBackup = mBackups.get(mPosition);
        mHolder.setData(mBackup, mPosition);
    }

    @Override
    public int getItemCount() {
        return mBackups.size();
    }


    /**
     * Convert long bytes format to a human-friendly format (eg: 12kb)
     *
     * @param bytes bytes size
     * @return file size: sth{kMGTPE}b
     */
    private static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.ITALIAN, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }


    class BackupHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mDate;
        private final TextView mSize;

        BackupHolder(View mView) {
            super(mView);
            mTitle = (TextView) mView.findViewById(R.id.row_backup_title);
            mDate = (TextView) mView.findViewById(R.id.row_backup_date);
            mSize = (TextView) mView.findViewById(R.id.row_backup_size);
        }

        void setData(BackupData mBackup, int mPosition) {
            final String mModifiedDate;
            Calendar mCal = Calendar.getInstance();

            if (mBackup == null) {
                return;
            }

            mCal.setTime(mBackup.getDate());
            mModifiedDate = Utils.rightDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1,
                    mCal.get(Calendar.DAY_OF_MONTH)) + " " + mCal.get(Calendar.HOUR_OF_DAY) +
                    ":" + mCal.get(Calendar.MINUTE);

            mTitle.setText(String.format(
                    BoldApp.getBoldContext().getString(R.string.backup_title), mPosition + 1));
            mDate.setText(mModifiedDate);
            mSize.setText(humanReadableByteCount(mBackup.getSize()));
        }
    }
}
