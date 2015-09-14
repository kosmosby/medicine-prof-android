/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import com.medicineprof.R;
import com.medicineprof.registration.service.RegistrationServiceHelper;
import com.medicineprof.registration.service.ServerResponseReceiver;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.jitsi.android.gui.*;
import org.jitsi.android.gui.contactlist.ContactListUtils;
import org.jitsi.android.gui.contactlist.ListExistingContactsFragment;
import org.jitsi.android.gui.menu.*;
import org.jitsi.android.gui.util.*;

import org.osgi.framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The <tt>AccountLoginActivity</tt> is the activity responsible for creating
 * a new account.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class AccountLoginActivity
    extends ExitMenuActivity
    implements
               PhoneRegistrationFragment.PhoneRegistrationListener,
               CodeVerificationFragment.CodeVerificationListener,
               ServerResponseReceiver.ServerReponseListener,
               ListExistingContactsFragment.ContactsHandler
{
    private String sPhone;
    private String login;
    private String password;
    private boolean loginReady = false;

    private ServerResponseReceiver broadcastReceiver;

    /**
     * Called when the activity is starting. Initializes the corresponding
     * call interface.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // If we have instance state it means the fragment is already created
        if(savedInstanceState == null)
        {
            PhoneRegistrationFragment phoneRegistration
                    = PhoneRegistrationFragment.getInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, phoneRegistration)
                    .commit();
        }
    }


    /**
     * Sign in the account with the given <tt>userName</tt>, <tt>password</tt>
     * and <tt>protocolName</tt>.
     *
     * @param userName the username of the account
     * @param password the password of the account
     * @param protocolName the name of the protocol
     * @return the <tt>ProtocolProviderService</tt> corresponding to the newly
     * signed in account
     */
    private ProtocolProviderService signIn( String userName,
                                            String password,
                                            String protocolName)
    {
        BundleContext bundleContext = getBundlecontext();

        Logger logger = Logger.getLogger(Jitsi.class);

        ServiceReference<?>[] accountWizardRefs = null;
        try
        {
            accountWizardRefs = bundleContext.getServiceReferences(
                    AccountRegistrationWizard.class.getName(),
                    null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                    "Error while retrieving service refs", ex);
        }

        // in case we found any, add them in this container.
        if (accountWizardRefs == null)
        {
            logger.error("No registered registration wizards found");
            return null;
        }

        if (logger.isDebugEnabled())
            logger.debug("Found " + accountWizardRefs.length
                                  + " already installed providers.");

        AccountRegistrationWizard selectedWizard = null;

        for (int i = 0; i < accountWizardRefs.length; i++)
        {
            AccountRegistrationWizard accReg
                    = (AccountRegistrationWizard) bundleContext
                    .getService(accountWizardRefs[i]);
            if (accReg.getProtocolName().equals(protocolName))
            {
                selectedWizard = accReg;
                break;
            }
        }
        if(selectedWizard == null)
        {
            logger.warn("No wizard found for protocol name: "+protocolName);
            return null;
        }
        try
        {
            selectedWizard.setModification(false);
            return selectedWizard.signin(userName, password);
        }
        catch (OperationFailedException e)
        {
            logger.error("Sign in operation failed.", e);

            if (e.getErrorCode() == OperationFailedException.ILLEGAL_ARGUMENT)
            {
                AndroidUtils.showAlertDialog(
                        this,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_USERNAME_NULL);
            }
            else if (e.getErrorCode()
                            == OperationFailedException.IDENTIFICATION_CONFLICT)
            {
                AndroidUtils.showAlertDialog(
                        this,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_USER_EXISTS_ERROR);
            }
            else if (e.getErrorCode()
                            == OperationFailedException.SERVER_NOT_SPECIFIED)
            {
                AndroidUtils.showAlertDialog(
                        this,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_SPECIFY_SERVER);
            }
            else
            {
                AndroidUtils.showAlertDialog(
                        this,
                        R.string.service_gui_LOGIN_FAILED,
                        R.string.service_gui_ACCOUNT_CREATION_FAILED);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception while adding account: "+e.getMessage(), e);
            AndroidUtils.showAlertDialog(
                    this,
                    R.string.service_gui_ERROR,
                    R.string.service_gui_ACCOUNT_CREATION_FAILED);
        }
        return null;
    }

    /**
     *
     */
    @Override
    public void onLoginPerformed(final String login, final String password, final List<String> contacts)
    {
        String network="Jabber";
        final ProtocolProviderService protocolProvider
                = signIn(login, password, network);


        if (protocolProvider != null)
        {
            new Thread(){
                @Override
                public void run() {
                    //addAndroidAccount(protocolProvider);
                    while(!protocolProvider.getRegistrationState().equals(RegistrationState.REGISTERED)){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                        if(contacts!=null){
                            for(String contactId:contacts){
                                if(contactId.startsWith("+")){
                                    contactId = contactId.substring(1);
                                }
                                ContactListUtils
                                        .addContact(protocolProvider,
                                                AndroidGUIActivator.getContactListService().getRoot(),
                                                contactId);
                            }
                        }


                }
            }.start();



            Intent showContactsIntent = new Intent(Jitsi.ACTION_SHOW_CONTACTS);
            startActivity(showContactsIntent);
            finish();
        }

    }

    @Override
    public void onPhoneEntered(String phone) {
        showWaitScreen();
        sPhone = phone;
        RegistrationServiceHelper.getInstance(getApplicationContext()).requestRegistrationCode(phone);
        //requestContacts(phone);
    }

    private void showWaitScreen(){
        WaitServerResponseFragment waitFragment
                = WaitServerResponseFragment.createInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, waitFragment)
                .commit();
    }

    @Override
    public void onServerResponse(Intent intent) {
        if((intent.getIntExtra(RegistrationServiceHelper.EXTRA_RESULT_CODE, 0)!=200)||
                !"OK".equals(intent.getStringExtra("status"))){
            if("request_code".equals(intent.getStringExtra("type"))){
                PhoneRegistrationFragment phoneRegistration
                        = PhoneRegistrationFragment.getInstance();

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(android.R.id.content, phoneRegistration)
                        .commit();
                if ("BAD_PHONE".equals(intent.getStringExtra("status")))
                {
                    AndroidUtils.showAlertDialog(
                            this,
                            "Wrong phone number",
                            "Please check that you have entered your full phone number including country and operator codes");
                }else{
                    AndroidUtils.showAlertDialog(
                            this,
                            "Something goes wrong :(",
                            "Server returned status: "+intent.getIntExtra(RegistrationServiceHelper.EXTRA_RESULT_CODE, 0)+
                            ". Error message: " + intent.getStringExtra("status"));
                }
            }else if("verify_code".equals(intent.getStringExtra("type"))){
                CodeVerificationFragment fragment
                        = CodeVerificationFragment.createInstance();

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit();


                if ("CODE_INCORRECT".equals(intent.getStringExtra("status")))
                {
                    AndroidUtils.showAlertDialog(
                            this,
                            "Wrong verification code",
                            "Please check that you have entered the verification code properly.");
                }else{
                    AndroidUtils.showAlertDialog(
                            this,
                            "Something goes wrong :(",
                            "Server returned status: "+intent.getIntExtra(RegistrationServiceHelper.EXTRA_RESULT_CODE, 0)+
                                    ". Error message: " + intent.getStringExtra("status"));
                }

            }
        }
        else{
            if("request_code".equals(intent.getStringExtra("type"))){
                CodeVerificationFragment fragment
                        = CodeVerificationFragment.createInstance();

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit();
            }else if ("verify_code".equals(intent.getStringExtra("type"))){
                String user = intent.getStringExtra("user");
                String password = intent.getStringExtra("password");
                SharedPreferences sPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("medicineprof_user", user);
                ed.putString("medicineprof_password", password);
                ed.commit();
                this.login = user;
                this.password = password;
                loginReady=true;
                requestContacts(user);
            }else if("request_contacts".equals(intent.getStringExtra("type"))){
                String[] phones = intent.getStringArrayExtra("phones");
                String[] contacts = intent.getStringArrayExtra("names");
                //showExistingContactsActivity(this.login, phones, contacts);

                ListExistingContactsFragment fragment
                        = ListExistingContactsFragment.createInstance(this.login, this.password, phones, contacts);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit();
            }

        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver = new ServerResponseReceiver(this);
        registerReceiver(broadcastReceiver,
                new IntentFilter(RegistrationServiceHelper.ACTION_REQUEST_RESULT));

    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onCodeEntered(String code) {
        showWaitScreen();
        RegistrationServiceHelper.getInstance(getApplicationContext()).verifyRegistrationCode(sPhone, code);
    }

    private void requestContacts(String user){
        List<Person> contacts = getContactList();
        String[] phones = new String[contacts.size()];
        String[] names = new String[contacts.size()];
        int i = 0;
        for(Person person:contacts){
            phones[i] = person.getPhoneNum();
            names[i] = person.getName();
            i++;
        }
        RegistrationServiceHelper.getInstance(getApplicationContext()).requestContacts(user, "", phones, names);
    }

    private List<Person> getContactList(){
        ArrayList<Person> contactList = new ArrayList<Person>();

        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
        String[] PROJECTION = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
        };
        String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
        Cursor contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);


        if (contacts.getCount() > 0)
        {
            while(contacts.moveToNext()) {
                Person aContact = new Person();
                int idFieldColumnIndex = 0;
                int nameFieldColumnIndex = 0;
                int numberFieldColumnIndex = 0;

                String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));

                nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                if (nameFieldColumnIndex > -1)
                {
                    aContact.setName(contacts.getString(nameFieldColumnIndex));
                }

                PROJECTION = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
                final Cursor phone = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
                if(phone.moveToFirst()) {
                    while(!phone.isAfterLast())
                    {
                        numberFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (numberFieldColumnIndex > -1)
                        {
                            aContact.setPhoneNum(phone.getString(numberFieldColumnIndex));
                            phone.moveToNext();
                            TelephonyManager mTelephonyMgr;
                            mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (!mTelephonyMgr.getLine1Number().contains(aContact.getPhoneNum()))
                            {
                                contactList.add(aContact);
                            }
                        }
                    }
                }
                phone.close();
            }

            contacts.close();
        }

        return contactList;
    }

    private void showExistingContactsActivity(String accountId, String[] phones, String[] contacts){
        Intent i=new Intent(getApplicationContext(),ListExistingContactsFragment.class);
        i.putExtra("phones", phones);
        i.putExtra("contacts", contacts);
        i.putExtra("accountId", accountId);
        startActivity(i);
    }

    private class Person {
        String myName = "";
        String myNumber = "";

        public String getName() {
            return myName;
        }

        public void setName(String name) {
            myName = name;
        }

        public String getPhoneNum() {
            return myNumber;
        }

        public void setPhoneNum(String number) {
            myNumber = number;
        }
    }
}
