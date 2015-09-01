/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.medicineprof.R;
import org.jitsi.service.osgi.OSGiFragment;

/**
 * The <tt>AccountLoginFragment</tt> is used for creating new account, but can
 * be also used to obtain user credentials. In order to do that parent
 * <tt>Activity</tt> must implement {@link CodeVerificationListener}.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class CodeVerificationFragment
    extends OSGiFragment
{

    private CodeVerificationListener codeVerificationListener;
    private static CodeVerificationFragment fragment = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if(activity instanceof CodeVerificationListener)
        {
            this.codeVerificationListener = (CodeVerificationListener)activity;
        }
        else
        {
           throw new RuntimeException("Code verification listener unspecified");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach()
    {
        super.onDetach();

        codeVerificationListener = null;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View content = inflater.inflate(R.layout.verify_code, container, false);



        initSignInButton(content);


        return content;
    }

    /**
     * Initializes the sign in button.
     */
    private void initSignInButton(final View content)
    {
        final Button verificationCodeButton
            = (Button) content.findViewById(R.id.verificationCodeButton);
        verificationCodeButton.setEnabled(true);

        verificationCodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final EditText codeField
                        = (EditText) content.findViewById(R.id.verificationCodeField);
                String code = codeField.getText().toString();
                codeVerificationListener.onCodeEntered(code);
            }
        });
    }

    /**
     * Creates new <tt>AccountLoginFragment</tt> with optionally filled login
     * and password fields(pass <tt>null</tt> arguments to omit).
     *
     *
     * @return new instance of parametrized <tt>AccountLoginFragment</tt>.
     */
    public static CodeVerificationFragment createInstance()
    {
        if(fragment==null){
            fragment = new CodeVerificationFragment();
        }
        return fragment;
    }

    /**
     * The interface is used to notify listener when user click the sign-in
     * button.
     */
    public interface CodeVerificationListener
    {
        /**
         * Method is called when user clicks the verify code button.
         * @param code the phone entered by the user.
         */
        void onCodeEntered(String code);
    }
}
