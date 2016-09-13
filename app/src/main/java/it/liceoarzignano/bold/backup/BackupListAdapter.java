package it.liceoarzignano.bold.backup;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.drive.DriveId;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.R;

class BackupListAdapter extends ArrayAdapter<BackupData> {
    private final Context context;

    BackupListAdapter(Context context, List<BackupData> items) {
        super(context, R.layout.item_backup, items);
        this.context = context;
    }

    private static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.ITALIAN, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View contentView, @NonNull ViewGroup parent) {
        View v = contentView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_backup, null);
        }

        BackupData data = getItem(position);
        final DriveId id;
        final String modified;
        String dataSize = null;
        Calendar calendar = Calendar.getInstance();
        if (data != null) {
            id = data.getId();
            dataSize = humanReadableByteCount(data.getSize());
            calendar.setTime(data.getDate());
        } else {
            id = null;
        }
        modified = calendar.get(Calendar.DAY_OF_MONTH) + "-" + (calendar.get(Calendar.MONTH) + 1) +
                "-" + calendar.get(Calendar.YEAR) + " " + (calendar.get(Calendar.HOUR_OF_DAY) < 10 ?
                "0" : "") + calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + calendar.get(Calendar.MINUTE);

        TextView title = (TextView) v.findViewById(R.id.row_backup_title);
        TextView modifiedDate = (TextView) v.findViewById(R.id.date);
        TextView size = (TextView) v.findViewById(R.id.size);

        title.setText(String.format(context.getString(R.string.backup_title), position + 1));
        modifiedDate.setText(modified);
        size.setText(dataSize);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .title(R.string.restore_dialog_title)
                        .content(String.format(context.getString(R.string.restore_dialog_message),
                                modified))
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.no)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                ((BackupActivity) context).downloadFromDrive(id.asDriveFile());
                            }
                        })
                        .show();
            }
        });
        return v;
    }


}
