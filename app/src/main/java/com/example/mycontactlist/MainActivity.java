package com.example.mycontactlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.mycontactlist.DatePickerDialog.SaveDateListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.SaveDateListener {

    boolean isBFF;

    private Contact currentContact; //creates the association between the this MainActivity class (ContactActivity) and a Contact object

    final int PERMISSION_REQUEST_PHONE = 102;
    final int PERMISSION_REQUEST_CAMERA = 103;
    final int CAMERA_REQUEST = 1888;
    final int PERMISSION_REQUEST_SMS = 105;

    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListButton();
        initMapButton();
        initSettingsButton();
        initToggleButton();
        setForEditing(false);
        initChangeDateButton();
        initTextChangedEvents();
        initSaveButton();
        initCallFunction();
        initImageButton();
        initTextFunction();


        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            initContact(extras.getInt("contactid"));
        } else {
            currentContact = new Contact();
        }





    }


    private void initListButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    private void initMapButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonMap);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactMapActivity.class);
                if(currentContact.getContactID() == -1) {
                    Toast.makeText(getBaseContext(), "Contact must be saved before it can be mapped", Toast.LENGTH_LONG).show();
                } else {
                    intent.putExtra("contactid", currentContact.getContactID());
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    private void initSettingsButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonSettings);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }



    private void initToggleButton() {
        final ToggleButton editToggle = (ToggleButton)findViewById(R.id.toggleButtonEdit);
        editToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setForEditing(editToggle.isChecked());
            }
        });
    }



    private void setForEditing(boolean enabled) {
        EditText editName = (EditText) findViewById(R.id.editName);
        EditText editAddress = (EditText) findViewById(R.id.editAddress);
        EditText editCity = (EditText) findViewById(R.id.editCity);
        EditText editState = (EditText) findViewById(R.id.editState);
        EditText editZipCode = (EditText) findViewById(R.id.editZipcode);
        EditText editPhone = (EditText) findViewById(R.id.editHome);
        EditText editCell = (EditText) findViewById(R.id.editCell);
        EditText editEmail = (EditText) findViewById(R.id.editEMail);
        Button buttonChange = (Button) findViewById(R.id.btnBirthday);
        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        CheckBox bff = (CheckBox) findViewById(R.id.checkBoxBFF);
        CheckBox bbff = (CheckBox) findViewById(R.id.checkBoxBBFF);
        ImageButton picture = (ImageButton) findViewById(R.id.imageContact);

        picture.setEnabled(enabled);
        editName.setEnabled(enabled);
        editAddress.setEnabled(enabled);
        editCity.setEnabled(enabled);
        editState.setEnabled(enabled);
        editZipCode.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        buttonChange.setEnabled(enabled);
        buttonSave.setEnabled(enabled);
        bff.setEnabled(enabled);
        bbff.setEnabled(enabled);

        if (enabled) {
            editName.requestFocus();
            editPhone.setInputType(InputType.TYPE_CLASS_PHONE);
            editCell.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            editPhone.setInputType(InputType.TYPE_NULL);
            editCell.setInputType(InputType.TYPE_NULL);
            ScrollView s = (ScrollView) findViewById(R.id.scrollView1);
            s.fullScroll(ScrollView.FOCUS_UP);
            s.clearFocus();
        }

        if(bff.isChecked()){
            bbff.setEnabled(false);
        }else if(bbff.isChecked()) {
            bff.setEnabled(false);
        }



    }


    @Override
    public void didFinishDatePickerDialog(Calendar selectedTime) {
        TextView birthday = (TextView) findViewById(R.id.textBirthday);
        birthday.setText(DateFormat.format("MM/dd/yyyy", selectedTime.getTimeInMillis()).toString());

        currentContact.setBirthday(selectedTime); /* uses the Contact Class's setBirthday method to assign the date *
                                                   * selected in the custom dialog to the currentContact object     */
    }

    private void initChangeDateButton() {
        Button changeDate = (Button) findViewById(R.id.btnBirthday);
        changeDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DatePickerDialog datePickerDialog = new DatePickerDialog();
                datePickerDialog.show(fm, "DatePick");
            }

        });


    }


    private void initTextChangedEvents() {
        final EditText etContactName = (EditText) findViewById(R.id.editName); //declared Final because it is used inside the event code

        etContactName.addTextChangedListener(new TextWatcher() { /*  TextWatcher is an Object that, when attached to a widget that allows editing, *
                                                                  *  will execute its methods when the text in the widget is changed.              *
                                                                  *  REQUIRES the three methods below to be implemented even if only one is used   */
            @Override
            public void afterTextChanged(Editable s) { /*  Method called after the user completes editing the data and leaves the EditText.  *
                                                        *  This is the event that this app uses to capture the data the user entered         */
                currentContact.setContactName(etContactName.getText().toString()); /* Code executed when the user ends editing of the EditText.   *
                                                                                    * Gets the text in EditText, converts it to a string, and     *
                                                                                    * sets the contactName attribute of the currentContact object *
                                                                                    * to that value                                               */
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { /* Required for the TextWatcher Object. Executed when           *
                                                                                              * the user presses down on a key to enter it into an EditText  *
                                                                                              * but before the value in the EditText is actually changed     */
                //Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { /* Required for the TextWatcher Object. Executed after *
                                                                                           * each and every character change in an EditText      */
                //Auto-generated method stub
            }
        });



        //BFF CheckBox Test Code Start
        final CheckBox etBFF = (CheckBox) findViewById(R.id.checkBoxBFF);
        final CheckBox etBFFF = (CheckBox) findViewById(R.id.checkBoxBBFF);
        etBFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    currentContact.setBestFriendForever(1);
                    etBFFF.setEnabled(false);
                } else {
                    currentContact.setBestFriendForever(0);
                    etBFFF.setEnabled(true);
                }


            }
        });
        //End of BFF CheckBox Test Code */


        etBFFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    currentContact.setBestFriendForever(2);
                    etBFF.setEnabled(false);
                } else if(!etBFF.isChecked() && !isChecked) {
                    currentContact.setBestFriendForever(0);
                    etBFF.setEnabled(true);
                }
            }
        });




        final EditText etStreetAddress = (EditText) findViewById(R.id.editAddress);
        etStreetAddress.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setStreetAddress(etStreetAddress.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Auto-generated method stub
            }
        });

        final EditText etCity = (EditText) findViewById(R.id.editCity);
        etCity.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setCity(etCity.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Auto-generated method stub
            }
        });

        final EditText etState = (EditText) findViewById(R.id.editState);
        etState.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setState(etState.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Auto-generated method stub
            }
        });

        final EditText etZipcode = (EditText) findViewById(R.id.editZipcode);
        etZipcode.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setZipCode(etZipcode.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Auto-generated method stub
            }
        });

        final EditText etHomePhone = (EditText) findViewById(R.id.editHome);
        etHomePhone.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setPhoneNumber(etHomePhone.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //  Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  Auto-generated method stub
            }
        });


        final EditText etCellPhone = (EditText) findViewById(R.id.editCell);
        etCellPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setCellNumber(etCellPhone.getText().toString());
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //  Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  Auto-generated method stub
            }
        });

        final EditText etEmail = (EditText) findViewById(R.id.editEMail);
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setEMail(etEmail.getText().toString());
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //  Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //  Auto-generated method stub
            }
        });

        etHomePhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher()); // code adds a listener to to the Phone Number editTexts that calls
        etCellPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher()); // the PhoneNumberFormattingTextWatcher object, which in turn
                                                                                    // adds the appropriate formatting as the user types

    }


    private void initSaveButton() {
        Button saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard();
                boolean wasSuccessful = false; //captures the return value of ContactDataSource Methods and used to determine the operations that should be performed
                ContactDataSource ds = new ContactDataSource(MainActivity.this); //new ContactDataSource object is instantiated
                try {
                    ds.open(); //Opens the database

                    if (currentContact.getContactID() == -1)  { //only new Contacts will have a -1 value
                        wasSuccessful = ds.insertContact(currentContact); //if method runs successfully, insertContact returns True
                    }
                    if(wasSuccessful) { //checks to see if, IF statement above was run successfully (new Contact)
                        int newID = ds.getLastContactID(); //uses retrieval method on ContactDataSource to get the newly inserted contact's ID
                        currentContact.setContactID(newID); //Sets the currentContact object's ID to the retrieved value from the database
                    }
                    else{
                        wasSuccessful = ds.updateContact(currentContact);
                    }

                    ds.close(); //closes the database

                } catch (Exception e) {
                    wasSuccessful = false; //if an exception is found, wasSuccessful stays false;
                }

                if (wasSuccessful) { //if it runs succesfully the edit Toggle button is turned off and the screen is set to Viewing
                    ToggleButton editToggle = (ToggleButton) findViewById(R.id.toggleButtonEdit);
                    editToggle.toggle();
                    setForEditing(false);
                }

            }
        });

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); //gets a system service that manages user input
        EditText editName = (EditText) findViewById(R.id.editName); //gets a reference to an EditText
        imm.hideSoftInputFromWindow(editName.getWindowToken(), 0); // closes the keyboard
        EditText editAddress = (EditText) findViewById(R.id.editAddress);
        imm.hideSoftInputFromWindow(editAddress.getWindowToken(), 0);
        EditText editCity = (EditText) findViewById(R.id.editCity);
        imm.hideSoftInputFromWindow(editCity.getWindowToken(), 0);
        EditText editState= (EditText) findViewById(R.id.editState);
        imm.hideSoftInputFromWindow(editState.getWindowToken(), 0);
        EditText editZip = (EditText) findViewById(R.id.editZipcode);
        imm.hideSoftInputFromWindow(editZip.getWindowToken(), 0);
        EditText editHome = (EditText) findViewById(R.id.editHome);
        imm.hideSoftInputFromWindow(editHome.getWindowToken(), 0);
        EditText editCell = (EditText) findViewById(R.id.editCell);
        imm.hideSoftInputFromWindow(editCell.getWindowToken(), 0);
        EditText editEMail = (EditText) findViewById(R.id.editEMail);
        imm.hideSoftInputFromWindow(editEMail.getWindowToken(), 0);
    }

    private void initContact(int id) {

        ContactDataSource ds = new ContactDataSource(MainActivity.this);
        try {
            ds.open();
            currentContact = ds.getSpecificContact(id);
            ds.close();
        } catch (Exception e) {
            Toast.makeText(this, "Load Contact Failed", Toast.LENGTH_LONG).show();
        }

        EditText editName = (EditText) findViewById(R.id.editName);
        EditText editAddress = (EditText) findViewById(R.id.editAddress);
        EditText editCity = (EditText) findViewById(R.id.editCity);
        EditText editState= (EditText) findViewById(R.id.editState);
        EditText editZip = (EditText) findViewById(R.id.editZipcode);
        EditText editHome = (EditText) findViewById(R.id.editHome);
        EditText editCell = (EditText) findViewById(R.id.editCell);
        EditText editEMail = (EditText) findViewById(R.id.editEMail);
        TextView birthDay = (TextView) findViewById(R.id.textBirthday);
        //Test
        CheckBox editBFF = (CheckBox) findViewById(R.id.checkBoxBFF);
        //test

        CheckBox editBBFF = (CheckBox) findViewById(R.id.checkBoxBBFF);

        editName.setText(currentContact.getContactName());
        editAddress.setText(currentContact.getStreetAddress());
        editCity.setText(currentContact.getCity());
        editState.setText(currentContact.getState());
        editZip.setText(currentContact.getZipCode());
        editHome.setText(currentContact.getPhoneNumber());
        editCell.setText(currentContact.getCellNumber());
        editEMail.setText(currentContact.getEMail());
        birthDay.setText(DateFormat.format("MM/dd/yyyy", currentContact.getBirthday().getTimeInMillis()).toString());

        ImageButton picture = (ImageButton) findViewById(R.id.imageContact);
        if (currentContact.getPicture() != null) {
            picture.setImageBitmap(currentContact.getPicture());
        }
        else {
            picture.setImageResource(R.drawable.pinkguytwo);
        }

        try{
            if(currentContact.getBestFriendForever() == 1) {
                editBFF.setChecked(true);
                editBBFF.setChecked(false);
            }
            else if (currentContact.getBestFriendForever() == 2){
                editBBFF.setChecked(true);
                editBFF.setChecked(false);
            }
            else {
                editBFF.setChecked(false);
                editBBFF.setChecked(false);

            }

        }catch (Exception e) {

        }

    }

    private void initCallFunction() {
        EditText editPhone = (EditText) findViewById(R.id.editHome);
        editPhone.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                checkPhonePermission(currentContact.getPhoneNumber());
                return false;
            }
        });

        /*
        EditText editCell = (EditText) findViewById(R.id.editCell);
        editCell.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                checkPhonePermission(currentContact.getCellNumber());
                return false;
            }
        });
        */

    }

    private void initTextFunction() {
        EditText editCell = (EditText) findViewById(R.id.editCell);
        editCell.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                checkTextPermission(currentContact.getCellNumber());
                return false;
            }

        });

    }


    private void checkTextPermission (String phoneNumber) {
        if (Build.VERSION.SDK_INT >= 23) {
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.SEND_SMS)) {

                    Snackbar.make(findViewById(R.id.actvity_main),
                            "MyContactList requires this permission to send a text from the app.",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[] {
                                            android.Manifest.permission.SEND_SMS},
                                    PERMISSION_REQUEST_SMS);

                        }
                    }).show();

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.SEND_SMS},
                            PERMISSION_REQUEST_SMS);
                }
            }else {
                textCell(phoneNumber);
            }
        } else {
            textCell(phoneNumber);
        }
    }




    private void checkPhonePermission (String phoneNumber) {
        if (Build.VERSION.SDK_INT >= 23) {
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        android.Manifest.permission.CALL_PHONE)) {

                    Snackbar.make(findViewById(R.id.actvity_main),
                            "MyContactList requires this permission to place a call from the app.",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[] {
                                            android.Manifest.permission.CALL_PHONE},
                                    PERMISSION_REQUEST_PHONE);

                        }
                    }).show();

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_PHONE);
                }
            }else {
                callContact(phoneNumber);
            }
        } else {
            callContact(phoneNumber);
        }
    }

    private void initImageButton() {
        ImageButton ib = (ImageButton) findViewById(R.id.imageContact);
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                MainActivity.this, android.Manifest.permission.CAMERA)) {
                            Snackbar.make(findViewById(R.id.actvity_main), "The app needs permission to take pictures.",
                                    Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(
                                            MainActivity.this, new String[]
                                                    {android.Manifest.permission.CAMERA},
                                            PERMISSION_REQUEST_CAMERA);
                                }
                            }).show();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    PERMISSION_REQUEST_CAMERA);
                        }
                    } else {
                        takePhoto();
                    }
                } else {
                    takePhoto();
                }
            }
        });
    }

    public void takePhoto(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); //tells the system to open the camera in image capture mode
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }


    @SuppressLint("MissingSuperCall")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) { //checks to see if camera returned a picture
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 144, 144, true);
                ImageButton imageContact = (ImageButton) findViewById(R.id.imageContact);
                imageContact.setImageBitmap(scaledPhoto);
                currentContact.setPicture(scaledPhoto);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_PHONE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You may now call from this app,", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this, "You will not be able to make calls from this app.", Toast.LENGTH_LONG).show();
                }
            }
            case PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(MainActivity.this, "You will not be able to save contact pictures from this app", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case PERMISSION_REQUEST_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }else {
                    Toast.makeText(MainActivity.this, "You will not be able to send sms from this app", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void callContact(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL); //Action_Call tells Android that you want to use the phone to make a call
        intent.setData(Uri.parse("tel:" + phoneNumber)); //uses Uri to identify a local resource
        if( Build.VERSION.SDK_INT >= 23 &&
        ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.CALL_PHONE) !=
        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else{
            startActivity(intent);
        }
    }

    private void textCell(String cellNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SMS Text");
        //set up the input
        final EditText input = new EditText(this);
        //specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        //set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sms = input.getText().toString();
                String phoneNum = currentContact.getCellNumber();
                if (sms.length() > 0) {
                    sendSMS(phoneNum, sms);
                } else {
                    Toast.makeText(getBaseContext(), "Please Enter a Message.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();



    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }









 /*
    private void initCheckBoxBFFByClick() {
        int bff = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getInt("BFF", 0);
        CheckBox checkBoxBFF = (CheckBox) findViewById(R.id.checkBoxBFF);
        checkBoxBFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).edit().putInt("BFF", 1).commit();
                }else {
                    getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).edit().putInt("BFF", 0).commit();
                }
            }
        });





    }

    private void initCheckBoxBFF() {
        int bff = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getInt("BFF", 0);
        CheckBox checkBoxBFF = (CheckBox) findViewById(R.id.checkBoxBFF);
        if(bff == 1) {
            checkBoxBFF.setChecked(true);
        } else {
            checkBoxBFF.setChecked(false);
        }

    }

 */



}
