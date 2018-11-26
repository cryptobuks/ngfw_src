Ext.define('Ung.config.administration.view.Certificates', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.config-administration-certificates',
    itemId: 'certificates',
    scrollable: true,

    viewModel: {
        formulas: {
            rootCertValidStarting: function (get) {
                return Ext.util.Format.date(new Date(get('rootCertificateInformation.dateValid.time')), 'timestamp_fmt'.t());
            },
            rootCertValidUntil: function (get) {
                return Ext.util.Format.date(new Date(get('rootCertificateInformation.dateExpires.time')), 'timestamp_fmt'.t());
            }
        }
    },

    title: 'Certificates'.t(),

    layout: 'border',

    items: [{
        title: 'Certificate Authority'.t(),
        region: 'center',
        itemId: 'rootCertificateView',
        bodyPadding: 10,
        scrollable: 'y',
        defaults: {
            labelWidth: 150,
            labelAlign: 'right'
        },
        items: [{
            xtype: 'component',
            margin: '0 0 10 0',
            html: 'The Certificate Authority is used to create and sign SSL certificates used by several applications and services such as SSL Inspector and Captive Portal.  It can also be used to sign the internal web server certificate. To eliminate certificate security warnings on client computers and devices, you should download the root certificate and add it to the list of trusted authorities on each client connected to your network.'.t()
        }, {
            xtype: 'displayfield',
            fieldLabel: 'Valid starting'.t(),
            bind: '{rootCertValidStarting}'
        },{
            xtype: 'displayfield',
            fieldLabel: 'Valid until'.t(),
            bind: '{rootCertValidUntil}'
        },{
            xtype: 'displayfield',
            fieldLabel: 'Subject DN'.t(),
            bind: '{rootCertificateInformation.certSubject}'
        }, {
            xtype: 'container',
            margin: '10 0 0 0',
            items: [{
                xtype: 'button',
                iconCls: 'fa fa-certificate',
                text: 'Generate Certificate Authority'.t(),
                certMode: 'ROOT',
                handler: 'generateCertificate'
            }, {
                xtype: 'component',
                style: { fontSize: '11px', color: '#999' },
                margin: '5 0 15 0',
                html: 'Click here to re-create the internal certificate authority.  Use this to change the information in the Subject DN of the root certificate.'.t()
            }, {
                xtype: 'button',
                iconCls: 'fa fa-download',
                text: 'Download Root Certificate Authority (CA)'.t(),
                handler: 'downloadRootCertificate'
            }, {
                xtype: 'component',
                style: { fontSize: '11px', color: '#999' },
                margin: '5 0 15 0',
                html: 'Click here to download the root certificate authority (CA).  Installing this CA on client devices will allow them to trust certificates generated by this CA.'.t()
            }, {
                xtype: 'button',
                iconCls: 'fa fa-download',
                text: 'Download Root Certificate Authority (CA) Installer'.t(),
                handler: 'downloadRootCertificateInstaller'
            }, {
                xtype: 'component',
                style: { fontSize: '11px', color: '#999' },
                margin: '5 0 15 0',
                html: 'Click here to download the root certificate authority (CA) installer.  It installs the root CA appropriately on a Windows device.'.t()
            }]
        }]
    }, {
        title: 'Server Certificates'.t(),
        region: 'south',
        height: '40%',
        split: true,

        layout: {
            type: 'vbox',
            align: 'stretch'
        },

        items: [{
            xtype: 'component',
            padding: 10,
            html: 'The Server Certificates list is used to select the SSL certificate to be used for each service provided by this server.  The <B>HTTPS</B> column selects the certificate used by the internal web server.  The <B>SMTPS</B> column selects the certificate to use for SMTP+STARTTLS when using SSL Inspector to scan inbound email.  The <B>IPSEC</B> column selects the certificate to use for the IPsec IKEv2 VPN server.'.t()
        }, {
            xtype: 'grid',
            itemId: 'serverCertificateView',
            flex: 1,
            bind: '{certificates}',
            layout: 'fit',

            sortableColumns: false,
            enableColumnHide: false,

            columns: [{
                header: 'Subject'.t(),
                dataIndex: 'certSubject',
                width: 220
            }, {
                header: 'Issued By'.t(),
                flex: 1,
                dataIndex: 'certIssuer'
            }, {
                header: 'Date Valid'.t(),
                width: 140,
                dataIndex: 'dateValid',
                renderer: function (date) {
                    return date.time ? Ext.util.Format.date(new Date(date.time), 'timestamp_fmt'.t()) : '';
                }
            }, {
                header: 'Date Expires'.t(),
                width: 140,
                dataIndex: 'dateExpires',
                renderer: function (date) {
                    return date.time ? Ext.util.Format.date(new Date(date.time), 'timestamp_fmt'.t()) : '';
                }
            }, {
                header: 'HTTPS'.t(),
                xtype: 'checkcolumn',
                width: 80,
                dataIndex: 'httpsServer',
                listeners: {
                    // don't allow uncheck - they must pick a different cert
                    beforecheckchange: function (el, rowIndex, checked) {
                        return checked ? true : false;
                    },
                    // when a new cert is selected uncheck all others
                    checkchange: function (el, rowIndex, checked, record) {
                        el.up('grid').getStore().each(function (rec) {
                            if (rec !== record) {
                                rec.set('httpsServer', false);
                            }
                        });
                    }
                }
            }, {
                header: 'SMTPS'.t(),
                xtype: 'checkcolumn',
                width: 80,
                dataIndex: 'smtpsServer',
                listeners: {
                    // don't allow uncheck - they must pick a different cert
                    beforecheckchange: function (el, rowIndex, checked) {
                        return checked ? true : false;
                    },
                    // when a new cert is selected uncheck all others
                    checkchange: function (el, rowIndex, checked, record) {
                        el.up('grid').getStore().each(function (rec) {
                            if (rec !== record) {
                                rec.set('smtpsServer', false);
                            }
                        });
                    }
                }
            }, {
                header: 'IPSEC'.t(),
                xtype: 'checkcolumn',
                width: 80,
                dataIndex: 'ipsecServer',
                listeners: {
                    // don't allow uncheck - they must pick a different cert
                    beforecheckchange: function (el, rowIndex, checked) {
                        return checked ? true : false;
                    },
                    // when a new cert is selected uncheck all others
                    checkchange: function (el, rowIndex, checked, record) {
                        el.up('grid').getStore().each(function (rec) {
                            if (rec !== record) {
                                rec.set('ipsecServer', false);
                            }
                        });
                    }
                }
            }, {
                xtype: 'actioncolumn',
                header: 'View'.t(),
                width: 60,
                align: 'center',
                resizable: false,
                iconCls: 'fa fa-file-text',
                tdCls: 'action-cell',
                handler: function(view, rowIndex, colIndex, item, e, record) {
                    var detail = '';
                    detail += '<b>VALID:</b> ' + Ext.util.Format.date(new Date(record.get('dateValid').time), 'timestamp_fmt'.t()) + '<br/><br/>';
                    detail += '<b>EXPIRES:</b> ' + Ext.util.Format.date(new Date(record.get('dateExpires').time), 'timestamp_fmt'.t()) + '<br/><br/>';
                    detail += '<b>ISSUER:</b> ' + record.get('certIssuer') + '<br/><br/>';
                    detail += '<b>SUBJECT:</b> ' + record.get('certSubject') + '<br/><br/>';
                    detail += '<b>SAN:</b> ' + record.get('certNames') + '<br/><br/>';
                    detail += '<b>EKU:</b> ' + record.get('certUsage') + '<br/><br/>';
                    Ext.MessageBox.alert({ buttons: Ext.Msg.OK, maxWidth: 1024, title: 'Certificate Details'.t(), msg: '<tt>' + detail + '</tt>' });
                }
            }, {
                xtype: 'actioncolumn',
                header: 'Delete',
                width: 60,
                align: 'center',
                resizable: false,
                iconCls: 'fa fa-trash-o fa-red',
                tdCls: 'action-cell',
                handler: 'deleteServerCert',
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    return record.get('httpsServer') || record.get('smtpsServer') || record.get('ipsecServer');
                }
            }]
        }],
        bbar: [{
            text: 'Generate Server Certificate'.t(),
            certMode: 'SERVER',
            iconCls: 'fa fa-pencil',
            handler: 'generateCertificate'
        }, {
            text: 'Upload Server Certificate'.t(),
            iconCls: 'fa fa-upload',
            handler: 'uploadServerCertificate',
        }, {
            text: 'Create Certificate Signing Request'.t(),
            certMode: 'CSR',
            iconCls: 'fa fa-certificate',
            handler: 'generateCertificate'
        }, {
            text: 'Import Signing Request Certificate'.t(),
            iconCls: 'fa fa-certificate',
            handler: 'importSignedRequest',
        }]
    }, {
        region: 'east',
        title: 'Server Certificate Verification'.t(),
        width: 300,
        collapsible: false,
        split: true,
        bodyPadding: 10,
        items: [{
            xtype: 'component',
            userCls: 'cert-verification',
            bind: '{serverCertificateVerification}'
        }]
    }]

});
