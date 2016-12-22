package com.example.android.inventorytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by Rudster on 12/1/2016.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

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
     *
     */
    private static final int PICK_IMAGE_REQUEST = 0;

    private ImageView mImageView;
    private TextView mTextView;

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

        //Examine the intent that was used to launch this activity,
        //in order to figure out if we"re creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        //If the intent does not contain an item content URI , then we know that we are creating
        //a new item
        if (mCurrentItemUri == null) {
            //This is a new item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            invalidateOptionsMenu();
        } else {
            //Otherwise this is an existing item, so change the app bar to say "Edit an Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));
            //Initialize a loader to read the item data from the database
            //and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mItemNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mItemPrice = (EditText) findViewById(R.id.edit_item_price);
        mItemSupplier = (EditText) findViewById(R.id.edit_item_supplier);
        mItemQuantity = (EditText) findViewById(R.id.edit_item_quantity);

        mTextView = (TextView) findViewById(R.id.image_uri);
        mImageView = (ImageView) findViewById(R.id.image);


        //Setup OnTouchListeners on all the imput fields, so we can determine if the user
        //has touched or modified them. This will let us know if there are unsaved changes
        //or not, if the user tries to leave the editor without saveing
        mItemNameEditText.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mItemSupplier.setOnTouchListener(mTouchListener);
        mItemQuantity.setOnTouchListener(mTouchListener);

        Button addPictureButton = (Button) findViewById(R.id.upload_picture);
        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    public void openImageSelector(View view) {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mCurrentItemUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mCurrentItemUri.toString());

                mTextView.setText(mCurrentItemUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
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
                TextUtils.isEmpty(itemSupplierString) && TextUtils.isEmpty(itemQuantityString)) {
            return;
        }


        //Create a Content Values object where column names are keys,
        //and Toto's pet attributes are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, itemNameString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, itemPriceString);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, itemSupplierString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, itemQuantityString);
        values.put(InventoryEntry.COLUMN_ITEM_CATEGORY, mCurrentItemUri.toString());
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
            case R.id.action_delete:
                //Pop up cofirmation dialog for deletion
                showDeleteConfirmationDialog();
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
                InventoryEntry.COLUMN_ITEM_CATEGORY,
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
        if (cursor == null || cursor.getCount() < 1) {
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
            mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri));
            mItemNameEditText.setText(name);
            mItemPrice.setText(price);
            mItemSupplier.setText(supplier);
            mItemQuantity.setText(Integer.toString(quantity));

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from teh input fields
        mItemNameEditText.setText("");
        mItemPrice.setText("");
        mItemSupplier.setText("");
        mItemQuantity.setText("");

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

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the movie.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing Movie.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the movie at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentMovieUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

//    /**
//     * This method is called when the order button is clicked.
//     */
//    public void submitOrder(View view) {
//        EditText nameField = (EditText) findViewById(R.id.edit_item_name);
//        String name = nameField.getText().toString();
//
//
//        EditText supField = (EditText) findViewById(R.id.edit_item_supplier);
//        String supplierField = supField.getText().toString();
//
//        EditText qtyField = (EditText) findViewById(R.id.edit_item_quantity);
//        String quantityField = qtyField.getText().toString();
//
//
//        String priceMessage = createOrderSummary(name, supplierField, quantityField);
//
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Movie order for " + name);
//        intent.putExtra(Intent.EXTRA_TEXT, priceMessage);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        }
//    }
//
//    private String createOrderSummary(String name, String supplier, String quantity) {
//        String emailMessage = "Hello " + supplier + ","
//                + "\n\nIt looks like we are running low on " + name
//                + "; our current quantity is " + quantity
//                + " and would like to reorder more."
//                + "\nRegards,"
//                + "\nConvenience Store Co.";
//        return emailMessage;
//    }


}