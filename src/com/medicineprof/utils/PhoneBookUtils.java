package com.medicineprof.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Created by neurons on 11/5/15.
 */
public class PhoneBookUtils {
    public static String  findContactNameIfExists(String contactAddress, ContentResolver contextResolver){
        contactAddress = contactAddress.replace("+", "");
        if(contactAddress.indexOf("@")>0){
            contactAddress = contactAddress.substring(0, contactAddress.indexOf("@"));
        }
        String[] PROJECTION = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
        };
        String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
        Cursor contacts = contextResolver.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);


        if (contacts.getCount() > 0)
        {
            final int columnIdIndex = contacts.getColumnIndex(ContactsContract.Contacts._ID);
            final int nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            while(contacts.moveToNext()) {

                String contactId = contacts.getString(columnIdIndex);
                String contactName = null;

                if (nameFieldColumnIndex > -1)
                {
                    contactName = contacts.getString(nameFieldColumnIndex);
                }


                PROJECTION = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
                final Cursor phone = contextResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
                if(phone.moveToFirst()) {
                    int numberFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    if (numberFieldColumnIndex > -1)
                    {
                        String phoneStr = phone.getString(numberFieldColumnIndex);
                        if(phoneStr!=null){
                            phoneStr = phoneStr.replace("+","");
                            phoneStr = phoneStr.replace(" ","");
                            phoneStr = phoneStr.replace("-","");
                            phoneStr = phoneStr.replace("(","");
                            phoneStr = phoneStr.replace(")","");
                            if(phoneStr.equals(contactAddress)){
                                return contactName;
                            }
                        }

                    }
                }
                phone.close();
            }

            contacts.close();
        }

        return null;
    }
}
