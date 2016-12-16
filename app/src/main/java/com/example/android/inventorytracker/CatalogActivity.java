package com.example.android.inventorytracker;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventorytracker.data.InventoryContract;
import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    int quantity = 0;

    private  static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);



        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        //Setup item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new Intent to go to EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                //form the content URI that represents the specific item that was clicked on,
                //by appending the "id"
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

                //Set the URI on the data field of the Intent
                intent.setData(currentItemUri);

                //Launch the EditorActivity to display the data for the current pet
                startActivity(intent);
            }
        });


        //Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }

    public void reorderHandler(View view){

        sendEmail();

    }
    protected void sendEmail(){
        //Get supplier name
        TextView itemSupplier = (TextView) findViewById(R.id.item_supplier);
        CharSequence supplierCharSequence = itemSupplier.getText();
        String supplier = supplierCharSequence.toString();

        //Get item name
        TextView itemName = (TextView) findViewById(R.id.item_name);
        CharSequence nameCharSequence = itemName.getText();
        String item = nameCharSequence.toString();

        //Display the request to the supplier
        String message = createSupplierRequest(supplier, item);

        //Use intent to launch email app. send the supplier request in the email body
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.supplier_request_subject));
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(intent);

    }

    private String createSupplierRequest(String supplier, String item){
        String emailMessage = "Hello " + supplier + ","
                + "\n\nIt looks like we are running low on " + item
                + " and would like to reorder more."
                + "\nRegards,"
                +"\nConvenience Store Co.";
        return emailMessage;
    }






    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from teh table we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
                InventoryEntry.COLUMN_ITEM_QUANTITY};

        //This loader will execute the Content Provider's query method on a background thred
        return new android.content.CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }


}


