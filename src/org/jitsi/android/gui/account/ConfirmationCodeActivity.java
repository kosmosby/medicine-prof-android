/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.content.Intent;
import android.os.Bundle;
import com.medicineprof.registration.service.RegistrationServiceHelper;
import net.java.sip.communicator.service.gui.AccountRegistrationWizard;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;
import org.jitsi.R;
import org.jitsi.android.gui.Jitsi;
import org.jitsi.android.gui.menu.ExitMenuActivity;
import org.jitsi.android.gui.util.AndroidUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * The <tt>AccountLoginActivity</tt> is the activity responsible for creating
 * a new account.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class ConfirmationCodeActivity
    extends ExitMenuActivity
    implements CodeVerificationFragment.CodeVerificationListener
{
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
            CodeVerificationFragment codeVerification
                    = CodeVerificationFragment.createInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, codeVerification)
                    .commit();
        }
    }

    @Override
    public void onCodeEntered(String code) {
        Bundle extras = getIntent().getExtras();
        String phone = extras.getString("PHONE_NUMBER");
        RegistrationServiceHelper.getInstance(getApplicationContext()).verifyRegistrationCode(phone, code);
    }
}
