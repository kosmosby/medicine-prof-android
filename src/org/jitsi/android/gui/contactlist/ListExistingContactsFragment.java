package org.jitsi.android.gui.contactlist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.medicineprof.R;
import net.java.sip.communicator.service.contactlist.MetaContactGroup;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.account.AccountUtils;
import org.jitsi.android.gui.AndroidGUIActivator;
import org.jitsi.android.gui.util.AccountUtil;
import org.jitsi.service.osgi.OSGiFragment;

public class ListExistingContactsFragment extends OSGiFragment {

    MyCustomAdapter dataAdapter = null;
    String[] phones;
    String[] contacts;
    String user;
    String password;

    ContactsHandler contactsHandler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof ContactsHandler){
            contactsHandler = (ContactsHandler)activity;
        }
    }

    public static ListExistingContactsFragment createInstance(String user, String password, String[] phones, String[] contacts)
    {
        ListExistingContactsFragment fragment = new ListExistingContactsFragment();
        fragment.phones = phones;
        fragment.contacts = contacts;
        fragment.user = user;
        fragment.password = password;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View content =
                inflater.inflate(R.layout.existing_phone_contacts, container, false);

        //Generate list View from ArrayList
        displayListView(content);

        checkButtonClick(content);
        return content;

    }

    private void displayListView(View content) {
        ArrayList<Country> countryList = new ArrayList<Country>();

        if(phones!=null && contacts!=null){

            for(int i = 0 ; i < phones.length ; i++){
                Country country = new Country(phones[i],contacts[i],false);
                countryList.add(country);
            }
        }
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(getActivity(),
                R.layout.existing_phone_contacts_info, countryList);
        ListView listView = (ListView) content.findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
    }

    private class MyCustomAdapter extends ArrayAdapter<Country> {

        private ArrayList<Country> countryList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Country> countryList) {
            super(context, textViewResourceId, countryList);
            this.countryList = new ArrayList<Country>();
            this.countryList.addAll(countryList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.existing_phone_contacts_info, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Country country = (Country) cb.getTag();
                        country.setSelected(cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Country country = countryList.get(position);
            holder.code.setText(" (" +  country.getCode() + ")");
            holder.name.setText(country.getName());
            holder.name.setChecked(country.isSelected());
            holder.name.setTag(country);

            return convertView;

        }

    }

    private void checkButtonClick(View content) {


        Button myButton = (Button) content.findViewById(R.id.addSelectedContacts);
        myButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//TODO: pass contact ID here.
                /*String accountIdStr = getIntent().getStringExtra("accountId");
                AccountID accountId = AccountUtils.getAccountForID(accountIdStr);
                ProtocolProviderService pps = AccountUtils.getRegisteredProviderForAccount(accountId);
                ArrayList<Country> countryList = dataAdapter.countryList;
                for(int i=0;i<countryList.size();i++){
                    Country country = countryList.get(i);
                    if(country.isSelected()){
                        //Add contacts here
                        ContactListUtils
                                .addContact( pps,
                                        AndroidGUIActivator.getContactListService().getRoot(),
                                        country.getCode());
                    }
                }

                finish();*/
                List<String> contactsToAdd = new ArrayList<String>();
                ArrayList<Country> countryList = dataAdapter.countryList;
                for(int i=0;i<countryList.size();i++){
                    Country country = countryList.get(i);
                    if(country.isSelected()){
                        contactsToAdd.add(country.getCode());
                    }
                }
                contactsHandler.onLoginPerformed(user, password, contactsToAdd);
            }
        });

    }

    public class Country {

        String code = null;
        String name = null;
        boolean selected = false;

        public Country(String code, String name, boolean selected) {
            super();
            this.code = code;
            this.name = name;
            this.selected = selected;
        }

        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public boolean isSelected() {
            return selected;
        }
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }

    public interface ContactsHandler{
        void onLoginPerformed(String user, String password, List<String>requestedContacts);
    }
}