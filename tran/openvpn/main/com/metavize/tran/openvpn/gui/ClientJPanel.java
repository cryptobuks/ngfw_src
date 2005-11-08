/*
 * RobertsJPanel.java
 *
 * Created on October 27, 2005, 3:06 PM
 */

package com.metavize.tran.openvpn.gui;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import com.metavize.mvvm.tran.TransformContext;
import com.metavize.mvvm.tran.IPaddr;

import com.metavize.tran.openvpn.VpnTransform;
import com.metavize.tran.openvpn.VpnClient;
import com.metavize.tran.openvpn.VpnSettings;
import com.metavize.tran.openvpn.VpnGroup;
import com.metavize.tran.openvpn.ClientSiteNetwork;

/**
 *
 * @author  inieves
 */
public class ClientJPanel extends javax.swing.JPanel {
    
    private final VpnTransform openvpn;

    /** Creates new form RobertsJPanel */
    public ClientJPanel( TransformContext transformContext ) {
        this.openvpn = (VpnTransform)transformContext.transform();
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        acceptJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 20, 270, 20));

        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 50, 270, 20));

        jPanel1.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 80, 270, 20));

        jPanel1.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 110, 270, 20));

        jPanel1.add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 140, 270, 20));

        jPanel1.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 170, 270, 20));

        jPanel1.add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, 270, 20));

        jPanel1.add(jTextField8, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 230, 270, 20));

        jPanel1.add(jTextField9, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 260, 270, 20));

        jPanel1.add(jTextField10, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 290, 270, 20));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Client 1:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 180, -1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Client 1 Status:");
        jTextField2.setEnabled(false);
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 180, -1));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Client 2:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 180, -1));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Client 2 Status:");
        jTextField4.setEnabled(false);
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 180, -1));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Client 3:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 180, -1));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Client 3 Status:");
        jTextField6.setEnabled(false);
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 180, -1));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Client 4:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 180, -1));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText( "Client 4 Status:" );
        jTextField8.setEnabled(false);
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 180, -1));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Client 5:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 180, -1));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Client 5 Status:");
        jTextField10.setEnabled(false);
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 180, -1));

        acceptJButton.setText("Generate Client!");
        acceptJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptJButtonActionPerformed(evt);
            }
        });

        jPanel1.add(acceptJButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 340, -1, -1));

        cancelJButton.setText("Load Client!");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        jPanel1.add(cancelJButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 340, -1, -1));

        jScrollPane1.setViewportView(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jScrollPane1, gridBagConstraints);

        updateClientList( openvpn.getVpnSettings());
    }//GEN-END:initComponents

    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        updateClientList( openvpn.getVpnSettings());
    }//GEN-LAST:event_cancelJButtonActionPerformed

    private void acceptJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptJButtonActionPerformed
        VpnSettings settings = openvpn.getVpnSettings();
        
        List clientList = new LinkedList();
        List groupList = settings.getGroupList();
        
        addClient( jTextField1, clientList, groupList );
        addClient( jTextField3, clientList, groupList );
        addClient( jTextField5, clientList, groupList );
        addClient( jTextField7, clientList, groupList );
        addClient( jTextField9, clientList, groupList );
        
        settings.setClientList( clientList );

        try {
            settings.validate();
            openvpn.setVpnSettings( settings );
            
            updateClientList( openvpn.getVpnSettings());
        } catch ( Exception e ) {
            System.err.println( "Invalid settings" + e );
        }
    }//GEN-LAST:event_acceptJButtonActionPerformed

    private void updateClientList( VpnSettings settings )
    {
        List clientList = settings.getClientList();
        
        if ( null == clientList ) return;

        Iterator iter = clientList.iterator();
        updateClient( jTextField1, jTextField2,  iter );
        updateClient( jTextField3, jTextField4,  iter );
        updateClient( jTextField5, jTextField6,  iter );
        updateClient( jTextField7, jTextField8,  iter );
        updateClient( jTextField9, jTextField10, iter );
    }

    private void addClient( javax.swing.JTextField field, List clientList, List groupList )
    {
        String value = field.getText().trim();
        
        if ( value.length() == 0 ) return;
        String[] clientDescription = value.split( " *\\| *" );

        if (( clientDescription.length < 2 ) || (( clientDescription.length & 1 ) == 1 )) {
            System.err.println( "Invalid client description [" + value + "]" );
            return;
        }

        VpnClient client = new VpnClient();
        client.setLive( true );
        client.setName( clientDescription[0].trim() );
        
        client.setGroup( null );
        
        /* Get the group */
        String groupName = clientDescription[1];
        for ( VpnGroup group : ((List<VpnGroup>)groupList)) {
            if ( !group.getName().equalsIgnoreCase( groupName )) continue;
            client.setGroup( group );
            break;
        }

        if ( client.getGroup() == null ) {
            System.err.println( "Unknown group [" + groupName + "]" );
            return;
        }

        List siteList = new LinkedList();

        for ( int c = 2 ; c < clientDescription.length ; c += 2 ) {
            try {
                String network = clientDescription[c];
                String netmask = clientDescription[c+1];
                ClientSiteNetwork site = new ClientSiteNetwork();
                site.setLive( true );
                site.setNetwork( IPaddr.parse( network ));
                site.setNetmask( IPaddr.parse( netmask ));
                siteList.add( site );
            } catch ( Exception e ) {
                System.err.println( "Error parsing network or netmask: " + e );
            }
        }
        
        client.setExportedAddressList( siteList );

        try {
            client.validate();
        } catch ( Exception e ) {
            System.err.println( "Invalid client" + e );
        }
        
        clientList.add( client );
    }

    private void updateClient( javax.swing.JTextField field, javax.swing.JTextField statusField, 
                               Iterator iter )
    {
        if ( !iter.hasNext()) {
            field.setText( "" );
            statusField.setText( "" );
            return;
        }
        
        VpnClient client = (VpnClient)iter.next();

        String value = client.getName() + " | ";
        
        VpnGroup group = client.getGroup();

        value += ( group == null ) ? "" : group.getName();

        for ( ClientSiteNetwork site : ((List<ClientSiteNetwork>)client.getExportedAddressList())) {
            value += " | " + site.getNetwork() + " | " + site.getNetmask();
        }
        
        field.setText( value );

        statusField.setText( "" + client.getAddress() + " " + client.getCertificateStatus());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
    
}
