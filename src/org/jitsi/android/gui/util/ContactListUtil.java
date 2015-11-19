package org.jitsi.android.gui.util;

import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.event.MetaContactEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactListAdapter;
import net.java.sip.communicator.service.contactlist.event.ProtoContactEvent;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.account.AccountUtils;
import org.jitsi.android.gui.AndroidGUIActivator;
import org.jitsi.android.gui.contactlist.ContactListUtils;

/**
 * Created by neurons on 11/17/15.
 */
public class ContactListUtil {
    public static void addContact(final String contactAddress, final String displayName, final ContactAddedListener listener){
        ProtocolProviderService pps =
                AccountUtils.getRegisteredProviders().iterator().next();

        if (displayName != null && displayName.length() > 0)
        {
            addRenameListener(  pps,
                    null,
                    contactAddress,
                    displayName,
                    listener);
        }

        ContactListUtils
                .addContact(pps,
                        AndroidGUIActivator.getContactListService().getRoot(),
                        contactAddress);
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
    public static void addRenameListener(
            final ProtocolProviderService protocolProvider,
            final MetaContact metaContact,
            final String contactAddress,
            final String displayName,
            final ContactAddedListener listener) {
        AndroidGUIActivator.getContactListService().addMetaContactListListener(
                new MetaContactListAdapter() {
                    @Override
                    public void metaContactAdded(MetaContactEvent evt) {
                        if (evt.getSourceMetaContact().getContact(
                                contactAddress, protocolProvider) != null) {
                            renameContact(evt.getSourceMetaContact(),
                                    displayName);
                            if(listener!=null) {
                                listener.contactAdded(contactAddress);
                            }
                        }
                    }

                    @Override
                    public void protoContactAdded(ProtoContactEvent evt) {
                        if (metaContact != null
                                && evt.getNewParent().equals(metaContact)) {
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
    public static void renameContact( final MetaContact metaContact,
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

    public interface ContactAddedListener{
        void contactAdded(String address);
    }
}
