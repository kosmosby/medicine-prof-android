/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.content.*;
import android.os.Bundle;

import com.medicineprof.R;
import com.medicineprof.registration.service.RegistrationServiceHelper;
import com.medicineprof.registration.service.ServerResponseReceiver;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.jitsi.android.gui.*;
import org.jitsi.android.gui.menu.*;
import org.jitsi.android.gui.util.*;

import org.osgi.framework.*;

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
               ServerResponseReceiver.ServerReponseListener
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
    public void onLoginPerformed(final String login, final String password)
    {
        String network="Jabber";
        final ProtocolProviderService protocolProvider
                = signIn(login, password, network);


        if (protocolProvider != null)
        {
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
                onLoginPerformed(user, password);
                //requestContacts(user);
            }/*else if("request_contacts".equals(intent.getStringExtra("type"))){
                String[] phones = intent.getStringArrayExtra("phones");
                String[] contacts = intent.getStringArrayExtra("names");
                //showExistingContactsActivity(this.login, phones, contacts);

                AddPhonebookContactsActivity fragment
                        = AddPhonebookContactsActivity.createInstance(this.login, this.password, phones, contacts);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit();
            }*/

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

}
