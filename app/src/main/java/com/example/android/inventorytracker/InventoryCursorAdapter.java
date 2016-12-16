package com.example.android.inventorytracker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

/**
 * Created by Rudster on 12/1/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();




    //constructs a new InventoryCursorAdapter
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //makes a new blank list item view. No data is set to views yet
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    //This method binds the inventory data to the given list item layout.
    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {


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
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

        //Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(itemNameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemSupplier = cursor.getString(supplierColumnIndex);
        final String itemQuantity = cursor.getString(quantityColumnIndex);


        //Update the TextViews with the attributes for the current pet
        itemNameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        supplierTextView.setText(itemSupplier);
        quantityTextView.setText(itemQuantity);
        final int currentQuantity = Integer.parseInt(itemQuantity);


        //declare button and initialize it
        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        saleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (currentQuantity > 0){
                    int quantityValue = currentQuantity;

                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, --quantityValue);

                    Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Long.parseLong(InventoryEntry._ID));
                    resolver.update(
                            uri,
                            values,
                            null,
                            null);


                }
            }
        });
        Button reorderButton = (Button) view.findViewById(R.id.button_reorder);
        reorderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Get supplier name
                TextView itemSupplier = (TextView) view.findViewById(R.id.item_supplier);
                CharSequence supplierCharSequence = itemSupplier.getText();
                String supplier = supplierCharSequence.toString();

                //Get item name
                TextView itemName = (TextView) view.findViewById(R.id.item_name);
                CharSequence nameCharSequence = itemName.getText();
                String item = nameCharSequence.toString();

                //Display the request to the supplier
                String message = createSupplierRequest(supplier, item);

                //Use intent to launch email app. send the supplier request in the email body
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        R.string.supplier_request_subject);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                //startActivity(intent);
            }

            private String createSupplierRequest(String supplier, String item){
                String emailMessage = "Hello " + supplier + ","
                        + "\n\nIt looks like we are running low on " + item
                        + " and would like to reorder more."
                        + "\nRegards,"
                        +"\nConvenience Store Co.";
                return emailMessage;
            }
        });
    }


}
