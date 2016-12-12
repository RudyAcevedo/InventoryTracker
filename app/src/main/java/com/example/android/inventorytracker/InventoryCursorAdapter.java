package com.example.android.inventorytracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

/**
 * Created by Rudster on 12/1/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {



    //constructs a new InventoryCursorAdapter
    public InventoryCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }
    //makes a new blank list item view. No data is set to views yet
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }



    //This method binds the inventory data to the given list item layout.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {






        //Find fields to populate in inflated template
        ImageView categoryImageView = (ImageView) view.findViewById(R.id.item_category);
        TextView itemNameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        TextView supplierTextView = (TextView) view.findViewById(R.id.item_supplier);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);

        //Find the columns of item attributes that we are interest in
        int itemNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

        //Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(itemNameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemSupplier = cursor.getString(supplierColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);

        // If the Category is empty string or null, then use some default text
        // that says "Category", so the TextView isn't blank.

        //TODO update with category
//        if (TextUtils.isEmpty(petBreed)) {
//            petBreed = context.getString(R.string.unknown_breed);
//        }


        //Update the TextViews with the attributes for the current pet
        itemNameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        supplierTextView.setText(itemSupplier);
        quantityTextView.setText(itemQuantity);


    }


}
