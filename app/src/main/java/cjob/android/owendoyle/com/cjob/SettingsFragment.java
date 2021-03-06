/*
* A fragment that inflates different views depending on the event
* type that is chosen. Here we get the event details from the user
* and create the new event.
* */

package cjob.android.owendoyle.com.cjob;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cjob.android.owendoyle.com.cjob.events.Event;
import cjob.android.owendoyle.com.cjob.events.EventManager;

/**
 * Created by Owner on 30/10/2015.
 */
public class SettingsFragment extends Fragment {
    private static final int REQUEST_EMAIL_PASSWORD = 123;
    private static final String DIALOG_EMIAL = "dialogemail";
    private static final String TAG = "SettingsFragment";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_ADDRESS = "address";
    private static final String ARG_EVENT_TYPE = "event_id";
    private static final int REQUEST_CONTACT = 0;

    private String event_title = "";
    private String text_message = "";
    private String email_to = "";
    private String email_subject = "";
    private String contact_name = "";
    private String cotact_number = "";
    private int event_radius = 10;
    private boolean deleteOnComplete = true;

    private Event newEvent = new Event();
    private EventManager mEventManager;

    private SeekBar mRadiusBar;
    private TextView mRadiusBarProgress;
    private EditText mEventTitle;
    private Button mChooseContactButton;
    private EditText mEmailTo;
    private EditText mEmailSubject;
    private EditText mTextMessage;
    private CheckBox mDeleteOnComplete;

    @Override
    public void onCreate(Bundle savedInsatnceState) {
        super.onCreate(savedInsatnceState);
        setHasOptionsMenu(true);
    }

    //inflates the appropriate view for the event type
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mEventManager = new EventManager(getActivity());
        newEvent.setLatitude(getArguments().getDouble(ARG_LATITUDE));
        newEvent.setLongitude(getArguments().getDouble(ARG_LONGITUDE));
        newEvent.setAddress(getArguments().getString(ARG_ADDRESS));
        newEvent.setType(Integer.toString(getArguments().getInt(ARG_EVENT_TYPE)));

        if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.ALARM){
            View v = inflater.inflate(R.layout.alarm_settings,container,false);
            editTitle(v);
            radiusPicker(v);
            deleteOnCompleteCheck(v);
            return v;
        }
        else if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.SMS){
            View v = inflater.inflate(R.layout.sms_settings,container,false);
            editTitle(v);
            radiusPicker(v);
            chooseContact(v);
            textMessage(v);
            deleteOnCompleteCheck(v);
            return v;
        }
        else if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.NOTIFICATION){
            View v = inflater.inflate(R.layout.notification_settings,container,false);
            editTitle(v);
            radiusPicker(v);
            textMessage(v);
            deleteOnCompleteCheck(v);
            return v;
        }
        else{
            View v = inflater.inflate(R.layout.email_settings,container,false);
            editTitle(v);
            radiusPicker(v);
            emailToSubject(v);
            textMessage(v);
            deleteOnCompleteCheck(v);
            return v;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_settings_menu, menu);
    }

    //preforms check when create event is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_create_event:
                    if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.ALARM && newEvent.getTitle() != null && newEvent.getTitle() != ""){
                       createEvent();
                    }
                    else if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.SMS && completeSmsEvent()){
                       createEvent();
                    }
                    else if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.NOTIFICATION && completeNotificationEvent()){
                        createEvent();
                    }
                    else if((Integer)getArguments().get(ARG_EVENT_TYPE) == EventManager.EMAIL && completeEmailEvent()){
                       createEmailEvent();
                    }
                    else{
                        Toast.makeText(getActivity(),R.string.event_empty_fields_toast,Toast.LENGTH_SHORT).show();
                    }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //adds the event to the database, goes back to the map and stats the location service
    private void createEvent(){
        mEventManager.addEvent(newEvent);
        Toast.makeText(getActivity(),R.string.event_created_toast,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(),MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Intent i = new Intent(getActivity(),BackgroundLocationService.class);
        getActivity().startService(i);
    }

    //asks the user for their email and password foe the email event
    private void createEmailEvent(){
        FragmentManager manager = getFragmentManager();
        EmailPasswordFragment dialog = new EmailPasswordFragment();
        dialog.setTargetFragment(SettingsFragment.this,REQUEST_EMAIL_PASSWORD);
        dialog.show(manager, DIALOG_EMIAL);
    }

    //allows arguments to be passed to the settings fragment
    public static SettingsFragment newInstance(double latitude, double longitude, String address, int event_type) {
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        args.putString(ARG_ADDRESS, address);
        args.putInt(ARG_EVENT_TYPE, event_type);

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //sets up the title field
    private void editTitle(View v){
        mEventTitle = (EditText) v.findViewById(R.id.event_title_text);
        mEventTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                newEvent.setTitle(charSequence.toString());
                event_title = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //sets up the radius picker seekBar
    private void radiusPicker(View v){
        mRadiusBar = (SeekBar) v.findViewById(R.id.alarm_radius_seekbar);
        mRadiusBarProgress = (TextView) v.findViewById(R.id.seekbar_progress_text_view);
        mRadiusBarProgress.setText("" + (mRadiusBar.getProgress() + 10) + "m");
        mRadiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    i = 1;
                }
                mRadiusBarProgress.setText("" + i * 10 + "m");
                newEvent.setRadius(i * 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                event_radius = (seekBar.getProgress() * 10);
            }
        });
    }

    //sets up the contact picker if needed
    private void chooseContact(View v){
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mChooseContactButton = (Button) v.findViewById(R.id.choose_contact_button);
        mChooseContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
    }

    //sets up the email subject field and receiver field
    private void emailToSubject(View v){
        mEmailTo = (EditText) v.findViewById(R.id.event_email_to);
        mEmailSubject = (EditText) v.findViewById(R.id.event_email_subject);
        mEmailTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email_to = charSequence.toString();
                newEvent.setEmail(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEmailSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email_subject = charSequence.toString();
                newEvent.setEmailSubject(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //sets up the message field
    private void textMessage(View v){
        mTextMessage = (EditText) v.findViewById(R.id.event_text_message);
        mTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                text_message = charSequence.toString();
                newEvent.setText(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //sets up the delete on complete check box
    private void deleteOnCompleteCheck(View v){
        mDeleteOnComplete = (CheckBox) v.findViewById(R.id.event_delete_on_complete);
        mDeleteOnComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                deleteOnComplete = b;
                if (b) {
                    newEvent.setDeleteOnComplete(1);
                } else {
                    newEvent.setDeleteOnComplete(0);
                }

            }
        });
    }

    // gets the result from the corresponding intents
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //gets result from the contact picker
        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
            try{

                if(c.getCount() == 0){
                    return;
                }
                c.moveToFirst();
                String recieverid = c.getString(0);
                String reciever = c.getString(1);
                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                // this gets the numbers returned for the contact chosen
                while (phones.moveToNext()){
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    //picks out the mobile number from the numbers returned
                    switch (type) {
                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                            mChooseContactButton.setText(reciever + ": " + number);
                            contact_name = reciever;
                            cotact_number = number;
                            newEvent.setContact(reciever);
                            newEvent.setContactNumber(number);
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                            break;
                    }
                }
                phones.close();


            }finally {
                c.close();
            }
        }

        //gets the result from the email password intent and creates the event
        else if(requestCode == REQUEST_EMAIL_PASSWORD){
            newEvent.setUserEmail(data.getStringExtra(EmailPasswordFragment.EXTRA_EMAIL));
            newEvent.setUserPassword(data.getStringExtra(EmailPasswordFragment.EXTRA_PASS));
            createEvent();
        }
    }

    //ensures the sms event if completely filled out
    private boolean completeSmsEvent(){
        if(newEvent.getTitle() != null && newEvent.getTitle() != ""
                && newEvent.getContact() != null && newEvent.getContact() != ""
                && newEvent.getContactNumber() != null && newEvent.getContactNumber() != ""
                && newEvent.getText() != null && newEvent.getText() != ""){
            return true;
        }
        else
            return false;
    }

    //ensures the email event if completely filled out
    private boolean completeEmailEvent(){
        if(newEvent.getTitle() != null && newEvent.getTitle() != ""
                && newEvent.getEmail() != null && newEvent.getEmail() != ""
                && newEvent.getEmailSubject() != null && newEvent.getEmailSubject() != ""
                && newEvent.getText() != null && newEvent.getText() != ""){
            return true;
        }
        else
            return false;
    }

    //ensures the notification event if completely filled out
    private boolean completeNotificationEvent(){
        if(newEvent.getTitle() != null && newEvent.getTitle() != ""
                && newEvent.getText() != null && newEvent.getText() != ""){
            return true;
        }
        else
            return false;
    }

}
