/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import com.medicineprof.R;
import org.jitsi.service.osgi.OSGiFragment;

import java.util.Map;

/**
 * The <tt>AccountLoginFragment</tt> is used for creating new account, but can
 * be also used to obtain user credentials. In order to do that parent
 * <tt>Activity</tt> must implement {@link PhoneRegistrationListener}.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class PhoneRegistrationFragment
    extends OSGiFragment
{

    private PhoneRegistrationListener phoneRegistrationListener;
    private static PhoneRegistrationFragment fragment = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if(activity instanceof PhoneRegistrationListener)
        {
            this.phoneRegistrationListener = (PhoneRegistrationListener)activity;
        }
        else
        {
           throw new RuntimeException("Phone registration listener unspecified");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach()
    {
        super.onDetach();

        phoneRegistrationListener = null;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View content = inflater.inflate(R.layout.register_phone, container, false);



        initSignInButton(content);


        return content;
    }

    /**
     * Initializes the sign in button.
     */
    private void initSignInButton(final View content)
    {
        final Button signInButton
            = (Button) content.findViewById(R.id.registerPhoneButton);
        signInButton.setEnabled(true);

        signInButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                final EditText phoneField
                    = (EditText) content.findViewById(R.id.userPhoneField);


                String phone = phoneField.getText().toString();


                phoneRegistrationListener.onPhoneEntered(phone);
            }
        });
    }

    /**
     * Stores the given <tt>protocolProvider</tt> data in the android system
     * accounts.
     *
     * @param protocolProvider the <tt>ProtocolProviderService</tt>,
     * corresponding to the account to store
     */
    private void storeAndroidAccount(ProtocolProviderService protocolProvider)
    {
        Map<String, String> accountProps
            = protocolProvider.getAccountID().getAccountProperties();

        String username = accountProps.get(ProtocolProviderFactory.USER_ID);

        Account account
            = new Account(  username,
                            getString(R.string.ACCOUNT_TYPE));

        final Bundle extraData = new Bundle();
        for (String key : accountProps.keySet())
        {
            extraData.putString(key, accountProps.get(key));
        }

        AccountManager am = AccountManager.get(getActivity());
        boolean accountCreated
            = am.addAccountExplicitly(
                account,
                accountProps.get(ProtocolProviderFactory.PASSWORD), extraData);

        Bundle extras = getArguments();
        if (extras != null)
        {
            if (accountCreated)
            {  //Pass the new account back to the account manager
                AccountAuthenticatorResponse response
                    = extras.getParcelable(
                        AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

                Bundle result = new Bundle();
                result.putString(   AccountManager.KEY_ACCOUNT_NAME,
                                    username);
                result.putString(   AccountManager.KEY_ACCOUNT_TYPE,
                                    getString(R.string.ACCOUNT_TYPE));
                result.putAll(extraData);

                response.onResult(result);
            }
            // TODO: notify about account authentication
            //finish();
        }
    }

    /**
     * Creates new <tt>AccountLoginFragment</tt> with optionally filled login
     * and password fields(pass <tt>null</tt> arguments to omit).
     *
     *
     * @return new instance of parametrized <tt>AccountLoginFragment</tt>.
     */
    public static PhoneRegistrationFragment getInstance()
    {
        if(fragment==null){
            fragment = new PhoneRegistrationFragment();
        }

        return fragment;
    }

    /**
     * The interface is used to notify listener when user click the sign-in
     * button.
     */
    public interface PhoneRegistrationListener
    {
        /**
         * Method is called when user click the verify phone button.
         * @param phone the phone entered by the user.
         */
        void onPhoneEntered(String phone);
    }
}
