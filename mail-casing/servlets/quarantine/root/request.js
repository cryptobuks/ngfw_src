Ext.namespace('Ung');

//The location of the blank pixel image
Ext.BLANK_IMAGE_URL = '/ext/resources/images/default/s.gif';

var i18n;
var qr;
var message = null;
Ung.QuarantineRequest = function() {
}

Ung.QuarantineRequest.prototype =  {
    init : function()
    {
        this.rpc = new JSONRpcClient("/quarantine/JSON-RPC").Quarantine;
		message = i18n._('Your Remote Access Portal login has been configured without an email address or with an incorrect email address.');
        this.requestForm  = new Ext.FormPanel({

            border : false,
            bodyStyle : 'padding:10px 10px 0px 10px;',
            autoScroll: true,
            defaults : {
                selectOnFocus : true,
                msgTarget : 'side'
            },			
			//title:'Request Quarantine Digest Email',
            //border: true,
			//frame:true,
            bodyStyle : 'background-color: white;',
            items : [
					{ xtype:'label',text : message, region : "north", cls:'message-2',ctCls:'message-container' ,margins:'4 4 4 4'},
			
			{
                xtype : 'fieldset',
                title : this.singleSelectUser ? i18n._('Select User') : i18n._('Enter the email address for which you would like the Quarantine Digest'),
                autoHeight : true,
				buttonAlign : 'left',				
				buttons : [{
								text : i18n._( "Request" ),
								cls:'quarantine-left-indented-2',
								handler : function() {
									var field = this.requestForm.find( "name", "email_address" )[0];
									var email = field.getValue();
									field.disable();
									this.rpc.requestDigest( this.requestEmail.createDelegate( this ), 
															email );
								},
								scope : this
							}]	,				
				
				items:[
				
					{
					xtype:'textfield',
	                fieldLabel : i18n._( "Email Address" ),
	                name : "email_address",
	                width: '350'
					},
				]
			}
			],
            
        });
    },

    completeInit : function()
    {
        this.init();
        this.requestForm.render( "quarantine-request-digest" );
    },

    requestEmail : function( result, exception ) 
    {
        if ( exception ) {
            Ext.MessageBox.alert("Failed",exception); 
            return;
        }

        var field = this.requestForm.find( "name", "email_address" )[0];

        if ( result == true ) {
            alert( "Sent a digest to : " + field.getValue());
            field.setValue( "" );
        }
        
        field.enable();
    }
};

Ext.onReady( function(){
    qr = new Ung.QuarantineRequest();

    // Initialize the I18n
    Ext.Ajax.request({
        url : 'i18n',
            success : function( response, options ) {
            i18n = new Ung.I18N({ map : Ext.decode( response.responseText )});
            qr.completeInit();
        },
        method : "GET",
        failure : function() {
            Ext.MessageBox.alert("Error", "Unable to load the language pack." );
        },
      params : { module : 'untangle-casing-mail' }
    });
});




