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
import org.jitsi.R;
import org.jitsi.service.osgi.OSGiFragment;

/**
 * The <tt>AccountLoginFragment</tt> is used for creating new account, but can
 * be also used to obtain user credentials. In order to do that parent
 * <tt>Activity</tt> must implement {@link CodeVerificationListener}.
 *
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class WaitServerResponseFragment
    extends OSGiFragment
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View content = inflater.inflate(R.layout.wait_for_server, container, false);
        return content;
    }

    /**
     * Creates new <tt>AccountLoginFragment</tt> with optionally filled login
     * and password fields(pass <tt>null</tt> arguments to omit).
     *
     *
     * @return new instance of parametrized <tt>AccountLoginFragment</tt>.
     */
    public static WaitServerResponseFragment createInstance()
    {
        WaitServerResponseFragment fragment = new WaitServerResponseFragment();
        return fragment;
    }
}
