package com.example.mycontactlist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {

    ArrayList<Contact> contacts;
    boolean isDeleting = false;
    ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        initListButton();
        initMapButton();
        initSettingsButton();
        initItemClick();
        initAddContactButton();
        initDeleteButton();

        String sortBy = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortfield", "contactname");
        String sortOrder = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortorder", "ASC");

        ContactDataSource ds = new ContactDataSource(this);

        try{
            ds.open();
            contacts = ds.getContacts(sortBy, sortOrder);
            ds.close();
            ListView listView = (ListView) findViewById(R.id.lvContacts);
            adapter = new ContactAdapter(this, contacts);
            listView.setAdapter(adapter);
        }catch (Exception e){
            Toast.makeText(this, "Error retrieving contacts", Toast.LENGTH_LONG).show();
        }

        BroadcastReceiver batteryReceiver = new BroadcastReceiver() { //This object receives Intents and has the code used to respond to the Intent
            @Override
            public void onReceive(Context context, Intent intent) {
                double batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); //gets the extra associated with the batterys current charge level form the OS
                double levelScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0); //gets the extra asspcoated with the batterys scale used for measuring the charge level
                int batteryPercent = (int) Math.floor(batteryLevel/levelScale * 100); //percentage level calculated
                TextView textBatteryState = (TextView) findViewById(R.id.textBatteryLevel);
                textBatteryState.setText(batteryPercent + "%"); //sets the percentage level on the TextView associated with the battery
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED); //listens for battery status changed intent broadcast by the system
        registerReceiver(batteryReceiver, filter); //the app uses the broadcast receiver to listen and handle the battery status intents

    }
    @Override
    public void onResume() {
        super.onResume();
        String sortBy = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortfield", "contactname");
        String sortOrder = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortorder", "ASC");

        ContactDataSource ds = new ContactDataSource(this);

        try{
            ds.open();
            contacts = ds.getContacts(sortBy, sortOrder);
            ds.close();
            if(contacts.size() > 0) {
                ListView listView = (ListView) findViewById(R.id.lvContacts);
                adapter = new ContactAdapter(this, contacts);
                listView.setAdapter(adapter);
            }else {
                Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }catch (Exception e){
            Toast.makeText(this, "Error retrieving contacts", Toast.LENGTH_LONG).show();
        }
    }




    private void initListButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonList);
        ibList.setEnabled(false);

    }

    private void initMapButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonMap);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, ContactMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initSettingsButton() {
        ImageButton ibList = (ImageButton) findViewById(R.id.imageButtonSettings);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initItemClick(){
        ListView listView = (ListView) findViewById(R.id.lvContacts);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                Contact selectedContact = contacts.get(position); //creates Contact object from the ArrayList using the position value
                if (isDeleting) {
                    adapter.showDelete(position, itemClicked, ContactListActivity.this, selectedContact);

                } else {

                Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
                intent.putExtra("contactid", selectedContact.getContactID()); //places the contact ID in the Bundle that is passed to the Contact Activity
                startActivity(intent);                                                  //a BUNDLE is an object used in Android to pass data between Activites
            }
            }
        });
    }

    private void initAddContactButton() {
        Button newContact = (Button) findViewById(R.id.buttonAdd);
        newContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDeleteButton() {
        final Button deleteButton = (Button) findViewById(R.id.buttonDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isDeleting) { //checks to see if user has deleting enabled when user
                    deleteButton.setText("Delete");
                    isDeleting = false;
                    adapter.notifyDataSetChanged(); //tells the adapter to update itself; sets the list back to the not deleting mode
                } else {
                    deleteButton.setText("Done Deleting");
                    isDeleting = true;
                }
            }
        });
    }




}
