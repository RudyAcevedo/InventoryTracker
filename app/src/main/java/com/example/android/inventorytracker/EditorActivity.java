package com.example.android.inventorytracker;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

/**
 * Created by Rudster on 12/1/2016.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    /**
     * Content URI for the existing pet data loader
     */
    private Uri mCurrentItemUri;

    /**
     * EditText field to enter the item name
     */
    private EditText mItemNameEditText;

    /**
     * EditText field to enter the items prcie
     */
    private EditText mItemPrice;

    /**
     * EditText field to enter the items supplier
     */
    private EditText mItemSupplier;
    /**
     * EditText field to enter the items quantity
     */
    private EditText mItemQuantity;

    /**
     * EditText field to enter the items category
     */
    private Spinner mCategorySpinner;

    /**
     * Category of item The possible values are:
     * 0 for beverage, 1 for chips, 2 for candy.
     */
    private int mCategory = InventoryEntry.CATEGORY_UNKNOWN;

    /**
     * Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we chagne teh mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //TODO decide activity_editor can be reused and if code below is usable

//        //Examine the intent that was used to launch this activity, In order to figure out if
//        //we are creating a new pet or editing an existing one
//        Intent intent = getIntent();
//        mCurrentPetUri = intent.getData();
//
//        //If the intent DOES NOT contain pet content URI, then we know that we are creating
//        //a new pet
//        if (mCurrentPetUri == null){
//            //This is a new pet, so change the app bar to say " Add a Pet"
//            setTitle(getString(R.string.editor_activity_title_new_pet));
//            //Invalidate teh options menu, so the "Delete" menu option can be hidden.
//            invalidateOptionsMenu();
//        }else {
//            //Otherwise this is an existing pet, so change the app bar to say "Edit Pet"
//            setTitle((R.string.editor_activity_title_edit));
//
//            //Initialize loader to read the pet data from the database
//            //and display the current values in the editor
//            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
//        }

        // Find all relevant views that we will need to read user input from
        mItemNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mItemPrice = (EditText) findViewById(R.id.edit_item_price);
        mItemSupplier = (EditText) findViewById(R.id.edit_item_supplier);
        mItemQuantity = (EditText) findViewById(R.id.edit_item_quantity);
        mCategorySpinner = (Spinner) findViewById(R.id.spinner_item_category);

        //Setup OnTouchListeners on all the imput fields, so we can determine if the user
        //has touched or modified them. This will let us know if there are unsaved changes
        //or not, if the user tries to leave the editor without saveing
        mItemNameEditText.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mItemSupplier.setOnTouchListener(mTouchListener);
        mItemQuantity.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter categorySpinnderAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnderAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.category_beverage))) {
                        mCategory = InventoryEntry.CATEGORY_BEVERAGE; // Beverage
                    } else if (selection.equals(getString(R.string.category_chips))) {
                        mCategory = InventoryEntry.CATEGORY_CHIPS; // Chips
                    } else if (selection.equals(getString(R.string.category_candy))) {
                        mCategory = InventoryEntry.CATEGORY_CANDY; // Candy
                    } else {
                        mCategory = InventoryEntry.CATEGORY_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = InventoryEntry.CATEGORY_UNKNOWN; // Unknown
            }
        });
    }

    /**
     * Get user input from editor and save new item into database
     */
    private void saveItem() {

        String itemNameString = mItemNameEditText.getText().toString().trim();
        String itemPriceString = mItemPrice.getText().toString().trim();
        String itemSupplierString = mItemSupplier.getText().toString().trim();
        String itemQuantityString = mItemQuantity.getText().toString().trim();
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(itemNameString) && TextUtils.isEmpty(itemPriceString) &&
                TextUtils.isEmpty(itemSupplierString) && TextUtils.isEmpty(itemQuantityString)
                && mCategory == InventoryEntry.CATEGORY_UNKNOWN) {return;}


        //Create a Content Values object where column names are keys,
        //and Toto's pet attributes are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, itemNameString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, itemPriceString);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, itemSupplierString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, itemQuantityString);
        values.put(InventoryEntry.COLUMN_ITEM_CATEGORY, mCategory);
        // If the price and quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;

        if (!TextUtils.isEmpty(itemPriceString)) {
            price = Integer.parseInt(itemPriceString);
        }
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(itemQuantityString)) {
            quantity = Integer.parseInt(itemQuantityString);
        }
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);

        //Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {

            //Insert a new pet into the provider, returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            //Show a toast message depending on wether or not the insertion was successful
            if (newUri == null) {
                //If the new content URI is null, then there was an error with the insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save pet to database
                saveItem();
                //exit activity
                finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Since the editor shows all the pet attributes, define a projection that contains
        //all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
                InventoryEntry.COLUMN_ITEM_QUANTITY};

        //This loader will execute the ContentProvider's query method on a background thread
        return new android.content.CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1){
            return;
        }

        //Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            //Find teh columns of the pet attributes that we're interested in
            int itemCategoryColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_CATEGORY);
            int itemNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int itemPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int itemSupplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
            int itemQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);


            // Extract out the value from the Cursor for the given column index
            int category = cursor.getInt(itemCategoryColumnIndex);
            String name = cursor.getString(itemNameColumnIndex);
            String price = cursor.getString(itemPriceColumnIndex);
            String supplier = cursor.getString(itemSupplierColumnIndex);
            int quantity = cursor.getInt(itemQuantityColumnIndex);


            // Update the views on the screen with the values from the database
            mItemNameEditText.setText(name);
            mItemPrice.setText(price);
            mItemSupplier.setText(supplier);
            mItemQuantity.setText(Integer.toString(quantity));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (category) {
                case InventoryEntry.CATEGORY_BEVERAGE:
                    mCategorySpinner.setSelection(1);
                    break;
                case InventoryEntry.CATEGORY_CHIPS:
                    mCategorySpinner.setSelection(2);
                    break;
                case InventoryEntry.CATEGORY_CANDY:
                    mCategorySpinner.setSelection(3);
                    break;
                default:
                    mCategorySpinner.setSelection(0);
                    break;
            }
        }




    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from teh input fields
        mItemNameEditText.setText("");
        mItemPrice.setText("");
        mItemSupplier.setText("");
        mItemQuantity.setText("");
        mCategorySpinner.setSelection(0);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
