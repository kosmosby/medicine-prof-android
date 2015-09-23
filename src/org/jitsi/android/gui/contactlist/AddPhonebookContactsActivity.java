package org.jitsi.android.gui.contactlist;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.medicineprof.R;
import com.medicineprof.registration.model.Contact;
import com.medicineprof.registration.service.RegistrationServiceHelper;
import com.medicineprof.registration.service.ServerResponseReceiver;
import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.event.MetaContactEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactListAdapter;
import net.java.sip.communicator.service.contactlist.event.ProtoContactEvent;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.AccountManager;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.ServiceUtils;
import net.java.sip.communicator.util.account.AccountUtils;
import org.jitsi.android.gui.AndroidGUIActivator;
import org.jitsi.android.gui.chat.ChatSessionManager;
import org.jitsi.service.osgi.OSGiActivity;

public class AddPhonebookContactsActivity extends OSGiActivity
    implements ServerResponseReceiver.ServerReponseListener{

    public static final String USER_ID_KEY =  "USER_ID_KEY";
    public static final String CONTACTS_KEY = "CONTACTS_KEY";
    public static final String AVATARS_KEY =  "AVATARS_KEY";

    MyCustomAdapter dataAdapter = null;
    private String userId = null;
    Map<String, Bitmap> avatars = new HashMap<String, Bitmap>();
    List<Contact> contacts;
    private ServerResponseReceiver broadcastReceiver;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.existing_phone_contacts);

        setTitle(R.string.service_gui_ADD_CONTACT);
/*TODO:
 1. Obtain contacts from phone book.
 2. Send contacts to the server and check who is added.
 3. Obtain response and display contacts list.
  */
        ListView listView = (ListView) findViewById(R.id.listView1);
        View waitContainer = findViewById(R.id.loadingPanel);
        waitContainer.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        if(savedInstanceState!=null){
            userId = savedInstanceState.getString(USER_ID_KEY);
            contacts = (List<Contact>)savedInstanceState.getSerializable(CONTACTS_KEY);
            avatars = (Map<String, Bitmap>)savedInstanceState.getSerializable(AVATARS_KEY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver = new ServerResponseReceiver(this);
        registerReceiver(broadcastReceiver,
                new IntentFilter(RegistrationServiceHelper.ACTION_REQUEST_RESULT));

        ListView listView = (ListView) findViewById(R.id.listView1);
        if(contacts==null) {
            new Thread()
            {
                @Override
                public void run()
                {
                    displayListView();
                }
            }.start();

        }else{
            showView();
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USER_ID_KEY, userId);
        outState.putSerializable(CONTACTS_KEY, (Serializable) contacts);
        outState.putSerializable(AVATARS_KEY, (Serializable) avatars);
    }

    private void displayListView() {
        List<Contact> contacts = getContactList();
        //Get current account
        if(userId==null) {
            AccountManager accountManager
                    = ServiceUtils.getService(getBundlecontext(), AccountManager.class);

            AccountID acc = accountManager.getStoredAccounts().iterator().next();
            userId = acc.getUserID();
        }

        RegistrationServiceHelper.getInstance(
                getApplicationContext()).requestContacts(userId, "", contacts);


    }

    private List<Contact> getContactList(){
        ArrayList<Contact> contactList = new ArrayList<Contact>();

        String[] PROJECTION = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        };
        String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
        Cursor contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);


        if (contacts.getCount() > 0)
        {
            while(contacts.moveToNext()) {
                Bitmap bit_thumb = null;
                Contact aContact = new Contact();
                int nameFieldColumnIndex = 0;
                int numberFieldColumnIndex = 0;

                String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));

                nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                if (nameFieldColumnIndex > -1)
                {
                    aContact.setName(contacts.getString(nameFieldColumnIndex));
                }
                String image_thumb = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                try {
                    if (image_thumb != null) {
                        bit_thumb = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(image_thumb));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                PROJECTION = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
                final Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
                if(phone.moveToFirst()) {
                    //while(!phone.isAfterLast())
                    //{
                        numberFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (numberFieldColumnIndex > -1)
                        {
                            aContact.setPhone(phone.getString(numberFieldColumnIndex));
                            phone.moveToNext();
                            TelephonyManager mTelephonyMgr;
                            mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (!mTelephonyMgr.getLine1Number().contains(aContact.getPhone()))
                            {
                                contactList.add(aContact);
                                avatars.put(aContact.getPhone(), bit_thumb);
                            }
                        }
                    //}
                }
                phone.close();
            }

            contacts.close();
        }

        return contactList;
    }

    private void requestContacts(String user){

    }

    @Override
    public void onServerResponse(Intent intent) {
        if("request_contacts".equals(intent.getStringExtra("type"))){
            contacts = (List<Contact>)intent.getSerializableExtra("contacts");

            Collections.sort(contacts, new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact t1) {
                    if(contact.isContactExists() && !t1.isContactExists()){
                        return -1;
                    }
                    if(t1.isContactExists() && !contact.isContactExists()){
                        return 1;
                    }
                    if(contact.getName()==null){
                        return 1;
                    }
                    return contact.getName().compareTo(t1.getName());
                }
            });
            for(Contact contact:contacts){
                if(contact.getJabberUsername()!=null&&
                        AndroidGUIActivator.getContactListService().findAllMetaContactsForAddress(contact.getJabberUsername()).hasNext()){
                    contact.setContactAdded(true);
                }else{
                    contact.setContactAdded(false);
                }
            }
            showView();

        }
    }
    private void showView(){
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.existing_phone_contacts_info, contacts);
        ListView listView = (ListView) findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
        View waitContainer = findViewById(R.id.loadingPanel);
        waitContainer.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    private void addContact(String contactAddress, String displayName){
        ProtocolProviderService pps =
                AccountUtils.getRegisteredProviders().iterator().next();

        if (displayName != null && displayName.length() > 0)
        {
            addRenameListener(  pps,
                    null,
                    contactAddress,
                    displayName);
        }

        ContactListUtils
                .addContact(pps,
                        AndroidGUIActivator.getContactListService().getRoot(),
                        contactAddress);
    }

    private void startChat(String contactAddress){

        if(AndroidGUIActivator.getContactListService().findAllMetaContactsForAddress(contactAddress).hasNext()){
            MetaContact metaContact = AndroidGUIActivator.getContactListService().findAllMetaContactsForAddress(contactAddress).next();
            Intent chatIntent = ChatSessionManager.getChatIntent(metaContact);

            if(chatIntent != null)
            {
                finish();
                startActivity(chatIntent);
            }
            else
            {
                //logger.warn("Failed to start chat with " + metaContact);
            }
        }

    }

    /**
     * Adds a rename listener.
     *
     * @param protocolProvider the protocol provider to which the contact was
     * added
     * @param metaContact the <tt>MetaContact</tt> if the new contact was added
     * to an existing meta contact
     * @param contactAddress the address of the newly added contact
     * @param displayName the new display name
     */
    private void addRenameListener(
            final ProtocolProviderService protocolProvider,
            final MetaContact metaContact,
            final String contactAddress,
            final String displayName)
    {
        AndroidGUIActivator.getContactListService().addMetaContactListListener(
                new MetaContactListAdapter()
                {
                    @Override
                    public void metaContactAdded(MetaContactEvent evt)
                    {
                        if (evt.getSourceMetaContact().getContact(
                                contactAddress, protocolProvider) != null)
                        {
                            renameContact(evt.getSourceMetaContact(),
                                    displayName);

                            startChat(contactAddress);
                        }
                    }

                    @Override
                    public void protoContactAdded(ProtoContactEvent evt)
                    {
                        if (metaContact != null
                                && evt.getNewParent().equals(metaContact))
                        {
                            renameContact(metaContact, displayName);
                        }
                    }
                });
    }

    /**
     * Renames the given meta contact.
     *
     * @param metaContact the <tt>MetaContact</tt> to rename
     * @param displayName the new display name
     */
    private void renameContact( final MetaContact metaContact,
                                final String displayName)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                AndroidGUIActivator.getContactListService()
                        .renameMetaContact( metaContact,
                                displayName);
            }
        }.start();
    }

    private class MyCustomAdapter extends ArrayAdapter<Contact> {

        private List<Contact> contactsList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               List<Contact> contactsList) {
            super(context, textViewResourceId, contactsList);
            this.contactsList = new ArrayList<Contact>();
            this.contactsList.addAll(contactsList);
        }

        private class ViewHolder {
            TextView name;
            ImageView imageView;
            ImageView addContactIcon;
            View row;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            Log.v("ConvertView", String.valueOf(position));
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.existing_phone_contacts_info, null);


                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.imageView = (ImageView) convertView.findViewById(R.id.pic);
                holder.addContactIcon = (ImageView) convertView.findViewById(R.id.addContact);
                holder.row = convertView.findViewById(R.id.newContactLayout);
                convertView.setTag(holder);


            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = contactsList.get(position);
            final ViewHolder holderFinal = holder;

            //holder.code.setText(" (" +  contact.getPhone() + ")");
            holder.name.setText(contact.getName());
            //holder.name.setChecked(country.isSelected());
            holder.name.setTag(contact);
            if(contact.isContactExists() ){
                holder.addContactIcon.setVisibility(View.VISIBLE);
                if(!contact.isContactAdded()) {
                    holder.row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addContact(contact.getJabberUsername(), contact.getName());
                            //holderFinal.addContactIcon.setVisibility(View.INVISIBLE);

                        }
                    });
                }else{
                    holder.row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startChat(contact.getJabberUsername());
                        }
                    });
                }

            }else{
                holder.addContactIcon.setVisibility(View.GONE);
            }
            // Set image if exists
            try {

                if (avatars.get(contact.getPhone()) != null) {
                    holder.imageView.setImageBitmap(avatars.get(contact.getPhone()));
                } else {
                    holder.imageView.setImageResource(R.drawable.avatar);
                }

            } catch (OutOfMemoryError e) {
                // Add default picture
                //v.imageView.setImageDrawable(this._c.getDrawable(R.drawable.image));
                e.printStackTrace();
            }

            return convertView;

        }

    }
}