package org.jitsi.android.gui.contactlist;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.*;
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
import org.jitsi.android.gui.util.AndroidUtils;
import org.jitsi.android.gui.util.ContactListUtil;
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
     * Search options menu items.
     */
    private MenuItem searchItem;

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

        String[] PROJECTION = null;/*new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHONE,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI

        };*/
        String SELECTION = null;//ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
        Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, SELECTION, null, null);


        if (contacts.getCount() > 0) {
            final int columnIdIndex = contacts.getColumnIndex(ContactsContract.Contacts._ID);
            final int nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            final int numberFieldColumnIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            final int imageUriColumnIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);
            Set<String> kp = new HashSet<String>();
            Set<String> kn = new HashSet<String>();
            while (contacts.moveToNext()) {
                Bitmap bit_thumb = null;
                Contact aContact = new Contact();

                String phone = contacts.getString(numberFieldColumnIndex);
                String name = contacts.getString(nameFieldColumnIndex);

                if (phone == null) {
                    continue;
                }
                if (kp.contains(phone)) {
                    continue;
                }
                if (kn.contains(name)) {
                    continue;
                }
                kp.add(phone);
                kn.add(name);

                aContact.setPhone(phone);
                aContact.setName(name);

                String image_thumb = contacts.getString(imageUriColumnIndex);
                try {
                    if (image_thumb != null) {
                        bit_thumb = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(image_thumb));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                contactList.add(aContact);
                avatars.put(aContact.getPhone(), bit_thumb);
            }
        }
        return contactList;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.phonebook_menu, menu);
        super.onCreateOptionsMenu(menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager
                = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        this.searchItem = menu.findItem(R.id.search);

        // OnActionExpandListener not supported prior API 14
        if(AndroidUtils.hasAPI(14))
        {
            searchItem.setOnActionExpandListener(
                    new MenuItem.OnActionExpandListener()
                    {
                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item)
                        {
                            filterContactList("");

                            return true; // Return true to collapse action view
                        }
                        public boolean onMenuItemActionExpand(MenuItem item)
                        {
                            return true; // Return true to expand action view
                        }
                    });
        }

        if(AndroidUtils.hasAPI(11))
        {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));

            int id = searchView.getContext().getResources()
                    .getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) searchView.findViewById(id);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setHintTextColor(getResources().getColor(R.color.white));

            bindSearchListener();
        }
        return true;
    }

    /**
     * Filters contact list for given <tt>query</tt>.
     * @param query the query string that will be used for filtering contacts.
     */
    private void filterContactList(String query) {
        if(dataAdapter!=null){
            dataAdapter.getFilter().filter(query);
        }
    }

    private void bindSearchListener()
    {
        if(searchItem != null)
        {
            SearchView searchView = (SearchView) searchItem.getActionView();

            SearchViewListener listener = new SearchViewListener();
            searchView.setOnQueryTextListener(listener);
            searchView.setOnCloseListener(listener);
        }
    }

    @Override
    public void onServerResponse(Intent intent) {
        if("request_contacts".equals(intent.getStringExtra("type"))){
            contacts = (List<Contact>)intent.getSerializableExtra("contacts");

            if(contacts!=null){
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





    private class MyCustomAdapter extends ArrayAdapter<Contact> {

        private List<Contact> contactsList;
        private List<Contact> contactsListOriginal;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               List<Contact> contactsList) {
            super(context, textViewResourceId, contactsList);
            this.contactsList = new ArrayList<Contact>();
            this.contactsList.addAll(contactsList);

            this.contactsListOriginal = new ArrayList<Contact>();
            this.contactsListOriginal.addAll(contactsList);
        }

        private class ViewHolder {
            TextView name;
            ImageView imageView;
            ImageView addContactIcon;
            View inviteButton;
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
                holder.inviteButton = convertView.findViewById(R.id.inviteButton);
                holder.row = convertView.findViewById(R.id.newContactLayout);
                convertView.setTag(holder);


            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            if(contactsList.size() <= position){
                return convertView;
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
                            ContactListUtil.addContact(contact.getJabberUsername(), contact.getName(),
                                    new ContactListUtil.ContactAddedListener(){

                                        @Override
                                        public void contactAdded(String address) {
                                            startChat(address);
                                        }
                                    });
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
                holder.inviteButton.setVisibility(View.GONE);
            }else{
                holder.addContactIcon.setVisibility(View.GONE);
                holder.inviteButton.setVisibility(View.VISIBLE);
                holder.inviteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                                new Intent(), 0);
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(contact.getPhone(), null,"You are invited to use http://medicine-prof.com/android", null, null);
                        Toast.makeText(getApplicationContext(), "SMS was sent to user " + contact.getName(),
                                Toast.LENGTH_LONG).show();*/
                        Uri uri = Uri.parse("smsto:" + contact.getPhone());
                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                        intent.putExtra("sms_body", "You are invited to use http://medicine-prof.com/android");
                        startActivity(intent);

                    }
                });
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

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults result = new FilterResults();
                    List<Contact> allContacts = contactsListOriginal;
                    if(constraint == null || constraint.length() == 0){

                        result.values = allContacts;
                        result.count = allContacts.size();
                    }else{
                        ArrayList<Contact> filteredList = new ArrayList<Contact>();
                        for(Contact j: allContacts){
                            if((j.getName() != null &&
                                    j.getName().toLowerCase().contains(constraint.toString().toLowerCase()))
                                    ||(j.getPhone()!=null && j.getPhone().contains(constraint)))
                                filteredList.add(j);
                        }
                        result.values = filteredList;
                        result.count = filteredList.size();
                    }

                    return result;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    if (filterResults.count == 0) {
                        notifyDataSetInvalidated();
                    } else {
                        //items = (ArrayList<Contact>) filterResults.values;
                        contactsList.clear();
                        contactsList.addAll((ArrayList<Contact>)filterResults.values);
                        notifyDataSetChanged();
                    }
                }
            };
        }
    }


    /**
     * Class used to implement <tt>SearchView</tt> listeners for compatibility
     * purposes.
     *
     */
    class SearchViewListener
            implements SearchView.OnQueryTextListener,
            SearchView.OnCloseListener
    {
        @Override
        public boolean onClose()
        {
            filterContactList("");

            return true;
        }

        @Override
        public boolean onQueryTextChange(String query)
        {
            filterContactList(query);

            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query)
        {
            filterContactList(query);

            return true;
        }
    }
}