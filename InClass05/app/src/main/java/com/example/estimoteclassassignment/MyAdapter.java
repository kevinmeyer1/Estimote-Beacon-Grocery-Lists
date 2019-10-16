package com.example.estimoteclassassignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.estimoteclassassignment.Model.Item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Item> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ConstraintLayout constraintLayout;
        public MyViewHolder(ConstraintLayout v) {
            super(v);
            constraintLayout = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<Item> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_discount, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        View v = holder.itemView;

        ImageView img = v.findViewById(R.id.imageview_product_image);
        TextView name = v.findViewById(R.id.textview_product_name);
        TextView discount = v.findViewById(R.id.textview_discount);
        TextView price = v.findViewById(R.id.textview_price);
        TextView region = v.findViewById(R.id.textview_region);

        Item currentItem = mDataset.get(position);

        name.setText(currentItem.name);

        if (currentItem.name.startsWith("Fresh Seedless")) {
            name.setTextSize(17);
        } else {
            name.setTextSize(25);
        }

        region.setText(currentItem.region);
        price.setText("$" +currentItem.price.toString());
        discount.setText("Discount: " + currentItem.discount + "%");

        Picasso.with(v.getContext()).load("https://inclass05.herokuapp.com/" + currentItem.photo).fit().into(img);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
