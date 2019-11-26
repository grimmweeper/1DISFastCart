package com.drant.FastCartMain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CartListAdaptor extends ArrayAdapter {
    private final CartActivity context;
    private final ArrayList<String> itemArray;
    private final ArrayList<BigDecimal> priceArray;
    private final ArrayList<Integer> imageIDarray;


    public CartListAdaptor(CartActivity context,
                           ArrayList<String> itemsAdded,
                           ArrayList<BigDecimal> priceAdded,
                           ArrayList<Integer> imageIDarray){
        super(context, R.layout.listview_tworows, itemsAdded);
        this.context = context;
        this.imageIDarray = imageIDarray;
        this.itemArray = itemsAdded;
        this.priceArray = priceAdded;
    }

    @NonNull
    @Override
    public CartActivity getContext() {
        return context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_tworows, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.nameTextViewID);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.infoTextViewID);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView1ID);

        //sets the values of the objects to values from the arrays
        nameTextField.setText(itemArray.get(position));
        infoTextField.setText(priceArray.get(position).toString());
        imageView.setImageResource(imageIDarray.get(position));

        return rowView;

    };

    public void add(String item, BigDecimal price) {
        this.itemArray.add(item);
        this.priceArray.add(price);
        this.imageIDarray.add(R.drawable.fries);
        notifyDataSetChanged();  //without this line listView doesn't update UI

    }
}
