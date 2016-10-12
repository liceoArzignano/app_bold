package it.liceoarzignano.bold.marks;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.realm.RealmController;


class AverageAdapter extends RecyclerView.Adapter<AverageAdapter.AverageHolder> {
    private final RealmController mController;
    private final String[] mResults;

    AverageAdapter(RealmController mController, String[] mResults) {
        this.mController = mController;
        this.mResults = mResults;
    }

    @Override
    public AverageAdapter.AverageHolder onCreateViewHolder(ViewGroup mParent, int mType) {
        View mItem = LayoutInflater.from(mParent.getContext())
                .inflate(R.layout.item_average, mParent, false);

        return new AverageAdapter.AverageHolder(mItem);
    }

    @Override
    public void onBindViewHolder(AverageAdapter.AverageHolder mHolder, int mPosition) {
        mHolder.setData(mResults[mPosition]);
    }

    @Override
    public int getItemCount() {
        return mResults != null ? mResults.length : 0;
    }


    class AverageHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mValue;

        AverageHolder(View mView) {
            super(mView);
            mTitle = (TextView) mView.findViewById(R.id.row_avg_title);
            mValue = (TextView) mView.findViewById(R.id.row_avg_value);
        }

        void setData(String mResult) {
            mTitle.setText(mResult);

            Double mDoubleVal = mController.getAverage(mResult, 0);
            if (mDoubleVal < 6) {
                mValue.setTextColor(Color.RED);
            }
            mValue.setText(String.format(Locale.ENGLISH, "%.2f", mDoubleVal));
        }
    }

}
