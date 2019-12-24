package tz.co.nezatech.apps.twigapmt.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import tz.co.nezatech.apps.twigapmt.R;
import tz.co.nezatech.apps.twigapmt.model.IdName;

public class IdNameAdapter extends RecyclerView.Adapter<IdNameAdapter.MyViewHolder> {
    private IdName[] mDataset;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public MyViewHolder(LinearLayout v) {
            super(v);
            layout = v;
        }
    }

    public IdNameAdapter(IdName[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public IdNameAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.id_name_spinner_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ((TextView) holder.layout.findViewById(R.id.text1)).setText(mDataset[position].getName());
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
