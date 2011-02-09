/* $HeadURL$ */
package com.untangle.node.cpd;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.catalina.Valve;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import com.untangle.node.cpd.CPDSettings.AuthenticationType;
import com.untangle.uvm.LocalAppServerManager;
import com.untangle.uvm.LocalUvmContext;
import com.untangle.uvm.LocalUvmContextFactory;
import com.untangle.uvm.UvmException;
import com.untangle.uvm.UvmState;
import com.untangle.uvm.RemoteBrandingManager;
import com.untangle.uvm.license.License;
import com.untangle.uvm.logging.EventLogger;
import com.untangle.uvm.logging.EventLoggerFactory;
import com.untangle.uvm.logging.EventManager;
import com.untangle.uvm.logging.UncachedEventManager;
import com.untangle.uvm.message.BlingBlinger;
import com.untangle.uvm.message.Counters;
import com.untangle.uvm.message.MessageManager;
import com.untangle.uvm.node.Node;
import com.untangle.uvm.node.NodeContext;
import com.untangle.uvm.node.NodeState;
import com.untangle.uvm.node.firewall.intf.IntfMatcher;
import com.untangle.uvm.node.firewall.ip.IPSimpleMatcher;
import com.untangle.uvm.servlet.UploadHandler;
import com.untangle.uvm.util.I18nUtil;
import com.untangle.uvm.util.OutsideValve;
import com.untangle.uvm.util.TransactionWork;
import com.untangle.uvm.util.JsonClient.ConnectionException;
import com.untangle.uvm.vnet.AbstractNode;
import com.untangle.uvm.vnet.PipeSpec;

public class CPDImpl extends AbstractNode implements CPD
{
    private static int deployCount = 0;
    
    private final CustomUploadHandler uploadHandler = new CustomUploadHandler(); 
    private final Logger logger = Logger.getLogger(CPDImpl.class);

    private final CPDIpUsernameMapAssistant assistant = new CPDIpUsernameMapAssistant(this);

    private final PipeSpec[] pipeSpecs;
    
    private final EventLogger<CPDLoginEvent> loginEventLogger;
    private final UncachedEventManager<BlockEvent> blockEventLogger;
    
    private final CPDManager manager = new CPDManager(this);
    
    private final BlingBlinger blockBlinger;
    private final BlingBlinger authorizeBlinger;

    public static final String DEFAULT_USERNAME = "captive portal user";
    
    private CPDSettings settings;

    // constructor ------------------------------------------------------------

    public CPDImpl()
    {
        NodeContext nodeContext = getNodeContext();
        this.loginEventLogger = EventLoggerFactory.factory().getEventLogger(nodeContext);
        this.loginEventLogger.addSimpleEventFilter(new LoginEventFilter());
        this.blockEventLogger = new UncachedEventManager<BlockEvent>();
        this.blockEventLogger.makeRepository(new BlockEventFilter(this));
        
        this.settings = new CPDSettings();
        this.pipeSpecs = new PipeSpec[0];
        
        MessageManager lmm = LocalUvmContextFactory.context().messageManager();
        Counters c = lmm.getCounters(getNodeId());
        blockBlinger = c.addActivity("block", I18nUtil.marktr("Blocked Sessions"), null, I18nUtil.marktr("BLOCK"));
        authorizeBlinger = c.addActivity("authorize", I18nUtil.marktr("Authorized Clients"), null, I18nUtil.marktr("AUTHORIZE"));
        lmm.setActiveMetricsIfNotSet(getNodeId(), blockBlinger, authorizeBlinger);
    }

    public void initializeSettings()
    {
        logger.info("Initializing Settings...");
        
        CPDSettings settings = new CPDSettings(this.getNodeId());
        /* Create a set of default capture rules */
        List<CaptureRule> rules = new LinkedList<CaptureRule>();
        rules.add(new CaptureRule(false, true,
                                  "Require a login for traffic on the internal (non-wan) interfaces", 
                                  IntfMatcher.getNonWanMatcher(), 
                                  IPSimpleMatcher.getAllMatcher(), IPSimpleMatcher.getAllMatcher(),
                                  CaptureRule.START_OF_DAY, CaptureRule.END_OF_DAY, CaptureRule.ALL_DAYS));
        
        rules.add(new CaptureRule(false, true,
                                  "Require a login between 8:00 AM and 5 PM on the internal (non-wan) interfaces.", 
                                  IntfMatcher.getNonWanMatcher(), 
                                  IPSimpleMatcher.getAllMatcher(), IPSimpleMatcher.getAllMatcher(),
                                  "8:00", "17:00", CaptureRule.ALL_DAYS));
        
        settings.setCaptureRules(rules);
        settings.getBaseSettings().setPageParameters(getDefaultPageParameters());

        try {
            setCPDSettings(settings);
        } catch (Exception e) {
            logger.error( "Unable to initialize the settings", e );
            throw new IllegalStateException("Error initializing cpd", e);
        }
    }

    // CPDNode methods --------------------------------------------------

    @Override
    public void setCPDSettings(final CPDSettings settings) throws Exception {
        if ( settings == this.settings ) {
            throw new IllegalArgumentException("Unable to update original settings, set this.settings to null first.");
        }
        
        TransactionWork<Void> tw = new TransactionWork<Void>()
        {
            public boolean doWork(Session s)
            {
                s.saveOrUpdate(settings);
                CPDImpl.this.settings = settings;
                return true;
            }

            public Void getResult() {
                return null;
            }
        };
        getNodeContext().runTransaction(tw);
        
        reconfigure();
    }

    public CPDSettings getCPDSettings() {
        return this.settings;
    }
    
    @Override
    public CPDBaseSettings getBaseSettings()
    {
        CPDBaseSettings baseSettings = this.settings.getBaseSettings();
        if ( baseSettings == null ) {
            baseSettings = new CPDBaseSettings();
        }
        
        return baseSettings;        
    }
    
    @Override
    public void setBaseSettings(final CPDBaseSettings baseSettings) throws Exception
    {
        TransactionWork<Void> tw = new TransactionWork<Void>()
        {
            public boolean doWork(Session s)
            {
                CPDImpl.this.settings.setBaseSettings( baseSettings );
                CPDImpl.this.settings = (CPDSettings)s.merge(settings);
                return true;
            }

            public Void getResult() {
                return null;
            }
        };
        getNodeContext().runTransaction(tw);
        
        reconfigure();
    }

    @Override
    public List<HostDatabaseEntry> getCaptiveStatus()
    {
        List<HostDatabaseEntry> captiveStatus = assistant.getCaptiveStatus();

        if (captiveStatus == null) {
           captiveStatus = new LinkedList<HostDatabaseEntry>();
        }

        return captiveStatus;
    }

    @Override
    public List<CaptureRule> getCaptureRules()
    {
        List<CaptureRule> captureRules = this.settings.getCaptureRules();
        if ( captureRules == null ) {
            captureRules = new LinkedList<CaptureRule>();
        }
        
        return captureRules;
    }
    
    @Override
    public void setCaptureRules( final List<CaptureRule> captureRules ) throws Exception
    {
        TransactionWork<Void> tw = new TransactionWork<Void>()
        {
            public boolean doWork(Session s)
            {
                CPDImpl.this.settings.setCaptureRules( captureRules );
                CPDImpl.this.settings = (CPDSettings)s.merge(settings);
                return true;
            }

            public Void getResult() {
                return null;
            }
        };
        getNodeContext().runTransaction(tw);
        
        reconfigure();
    }
    
    @Override
    public List<PassedClient> getPassedClients()
    {
        return this.settings.getPassedClients();
    }
    
    @Override
    public void setPassedClients( final List<PassedClient> newValue ) throws Exception
    {
        TransactionWork<Void> tw = new TransactionWork<Void>()
        {
            public boolean doWork(Session s)
            {
                CPDImpl.this.settings.setPassedClients( newValue );
                CPDImpl.this.settings = (CPDSettings)s.merge(settings);
                return true;
            }

            public Void getResult() {
                return null;
            }
        };
        getNodeContext().runTransaction(tw);
        
        reconfigure();
    }
    
    @Override
    public List<PassedServer> getPassedServers()
    {
        return this.settings.getPassedServers();
    }

    @Override
    public void setPassedServers( final List<PassedServer> newValue ) throws Exception
    {
        TransactionWork<Void> tw = new TransactionWork<Void>()
        {
            public boolean doWork(Session s)
            {
                CPDImpl.this.settings.setPassedServers( newValue );
                CPDImpl.this.settings = (CPDSettings)s.merge(settings);
                return true;
            }

            public Void getResult() {
                return null;
            }
        };
        getNodeContext().runTransaction(tw);
        
        reconfigure();
    }
    
    @Override
    public void setAll( final CPDBaseSettings baseSettings, 
            final List<CaptureRule> captureRules,
            final List<PassedClient> passedClients, 
            final List<PassedServer> passedServers ) throws Exception
    {
        TransactionWork<Void> tw = new TransactionWork<Void>()
        {
            public boolean doWork(Session s)
            {
                if ( baseSettings != null ) {
                    CPDImpl.this.settings.setBaseSettings( baseSettings );
                }
                
                if ( captureRules != null ) {
                    CPDImpl.this.settings.setCaptureRules(captureRules);
                }
                
                if ( passedClients != null ) {
                    CPDImpl.this.settings.setPassedClients( passedClients );
                }

                if ( passedServers != null ) {
                    CPDImpl.this.settings.setPassedServers( passedServers );
                }
                CPDImpl.this.settings = (CPDSettings)s.merge(settings);
                return true;
            }

            public Void getResult() {
                return null;
            }
        };
        getNodeContext().runTransaction(tw);
        
        reconfigure();
    }

    @Override
    public EventManager<CPDLoginEvent> getLoginEventManager()
    {
        return this.loginEventLogger;
    }
    
    @Override
    public EventManager<BlockEvent> getBlockEventManager()
    {
        return this.blockEventLogger;
    }
    
    @Override
    public void incrementCount(BlingerType blingerType, long delta )
    {
        switch ( blingerType ) {
        case BLOCK:
            this.blockBlinger.increment(delta);
            break;
            
        case AUTHORIZE:
            this.authorizeBlinger.increment(delta);
            break;
        }
    }
    
    @Override
    public boolean authenticate( String address, String username, String password, String credentials )
    {
        boolean isAuthenticated = false;
        if ( this.getRunState() ==  NodeState.RUNNING ) {
            /* Enforcing this here so the user can't pick another username at login. */
            if ( this.settings.getBaseSettings().getAuthenticationType() == AuthenticationType.NONE) {
                username = this.DEFAULT_USERNAME;
            }
            /* This is split out for debugging */
            isAuthenticated = this.manager.authenticate(address, username, password, credentials);

            /* Update the CPD Phone Book cache */
            if (isAuthenticated) {
                try {
                    /* if no auth is required, don't count the "default user" as a user */
                    if ( this.settings.getBaseSettings().getAuthenticationType() != AuthenticationType.NONE )
                        assistant.addCache(InetAddress.getByName(address),username);
                } catch (UnknownHostException e) {
                    logger.warn("Add Cache failed",e);
                }
            }
        }
        return isAuthenticated;
    }
    
    @Override
    public boolean logout( String address )
    {
        boolean isLoggedOut = false;
        if ( this.getRunState() == NodeState.RUNNING ) {
            isLoggedOut = this.manager.logout( address );
        }

        /* Update the CPD Phone Book cache */
        if (isLoggedOut) {
            try {
                assistant.removeCache(InetAddress.getByName(address));
            } catch (UnknownHostException e) {
                logger.warn("Remove Cache failed",e);
            }
        }
        
        return isLoggedOut;
    }


    // AbstractNode methods ----------------------------------------------

    @Override
    protected PipeSpec[] getPipeSpecs() {
        return pipeSpecs;
    }

    // lifecycle --------------------------------------------------------------

    @Override
    protected void preStart() throws Exception
    {
        /* Check if there is at least one enabled capture rule */
        boolean hasCaptureRule = false;
        for ( CaptureRule rule : this.settings.getCaptureRules()) {
            if ( rule.isLive() && rule.getCapture()) {
                hasCaptureRule = true;
                break;
            }
        }
        
        if ( !hasCaptureRule ) {
            Map<String,String> i18nMap = LocalUvmContextFactory.context().languageManager().getTranslations("untangle-node-cpd");
            I18nUtil i18nUtil = new I18nUtil(i18nMap);
            throw new Exception( i18nUtil.tr( "You must create and enable at least one Capture Rule before turning on the Captive Portal" ));
        }
        reconfigure(true);
           
        LocalUvmContextFactory.context().uploadManager().registerHandler(this.uploadHandler);
        
        super.preStart();
     }

    @Override
    protected void preStop() throws Exception
    {
        try {
            /* Only stop if requested by the user (not during shutdown). */
            if ( LocalUvmContextFactory.context().state() != UvmState.DESTROYED ) {
                this.manager.clearHostDatabase();
                
                /* Flush all of the entries that are in the phonebook XXX */
                //LocalUvmContextFactory.context().localIpUsernameMap().flushEntries();
            }
        } catch (JSONException e) {
            logger.warn( "Unable to convert the JSON while clearing the host database, continuing.", e);
        } catch (ConnectionException e) {
            logger.warn( "Unable to clear host database settings, continuing.", e);
        }
        
        try {
            this.manager.setConfig(this.settings, false);
        } catch (JSONException e) {
            logger.warn( "Unable to convert the JSON while disabling the configuration, continuing.", e);
        } catch (IOException e) {
            logger.warn( "Unable to write settings, continuing.", e);
        }
        this.manager.stop();
    }
    
    @Override
    protected void postStop() throws Exception
    {
        super.postStop();

        this.assistant.destroy();
    }
    
    protected void postInit(final String[] args) {
        TransactionWork<Object> tw = new TransactionWork<Object>() {
            public boolean doWork(Session s) {
                Query q = s
                        .createQuery("from CPDSettings cs where cs.tid = :tid");
                q.setParameter("tid", getNodeId());

                CPDImpl.this.settings = (CPDSettings) q.uniqueResult();
                return true;
            }

            public Object getResult() {
                return null;
            }
        };
        
        getNodeContext().runTransaction(tw);
        
        LocalUvmContextFactory.context().uploadManager().registerHandler(this.uploadHandler);
        
        deployWebAppIfRequired(this.logger);
    }
    
    @Override
    protected void preDestroy() throws Exception
    {
        LocalUvmContextFactory.context().uploadManager().unregisterHandler(this.uploadHandler.getName());
                
        unDeployWebAppIfRequired(this.logger);

        this.assistant.stopDatabaseReader();
        
        super.preDestroy();
    }
    
    @Override
    protected void uninstall()
    {
        LocalUvmContextFactory.context().uploadManager().unregisterHandler(this.uploadHandler.getName());

        super.uninstall();
    }


    // private methods -------------------------------------------------------
    private void reconfigure() throws Exception
    {
        reconfigure(false);
    }
    
    private void reconfigure(boolean force) throws Exception
    {
        if ( force || this.getRunState() == NodeState.RUNNING) {
            try {
                this.manager.setConfig(this.settings, true);
            } catch (JSONException e) {
                throw new Exception( "Unable to convert the JSON while setting the configuration.", e);
            } catch (IOException e) {
                throw new Exception( "Unable to write settings.", e);
            }
                        
            try {
                this.manager.start();
            } catch ( Exception e ) {
                throw new Exception( "Unable to start CPD", e );
            }
        } else {
            try {
                this.manager.setConfig(this.settings, false);
            } catch (JSONException e) {
                logger.warn( "Unable to convert the JSON while disabling the configuration, continuing.", e);
            } catch (IOException e) {
                logger.warn( "Unable to write settings, continuing.", e);
            }

            this.manager.stop();
        }
    }
    
    private String getDefaultPageParameters() {

        try {
            RemoteBrandingManager brand = LocalUvmContextFactory.context().brandingManager();

            JSONObject parameters = new JSONObject();
            parameters.put( "basicLoginPageTitle", "Captive Portal");
            parameters.put( "basicLoginPageWelcome", "Welcome to the " + brand.getCompanyName() + " Captive Portal");
            parameters.put( "basicLoginUsername", "Username:");
            parameters.put( "basicLoginPassword", "Password:");
            parameters.put( "basicLoginMessageText", "Please enter your username and password to connect to the Internet.");
            parameters.put( "basicLoginFooter", "If you have any questions, please contact your network administrator.");
            parameters.put( "basicMessagePageTitle", "Captive Portal");
            parameters.put( "basicMessagePageWelcome", "Welcome to the " + brand.getCompanyName() + " Captive Portal");
            parameters.put( "basicMessageMessageText", "Click Continue to connect to the Internet.");
            parameters.put( "basicMessageAgreeText", "Clicking here means you agree to the terms above.");
            parameters.put( "basicMessageFooter", "If you have any questions, please contact your network administrator.");
            return parameters.toString();
        } catch ( JSONException e ) {
            logger.warn( "Unable to create default page parameters" );
        }

        return "{}";
    }
    
    private class CustomUploadHandler implements UploadHandler
    {
        @Override
        public String getName() {
            return "cpd-custom-page";
        }

        @Override
        public String handleFile(FileItem fileItem) throws Exception {
            File temp = File.createTempFile("untangle-cpd", ".zip");
            fileItem.write(temp);
            manager.loadCustomPage(temp.getAbsolutePath());
            return "Successfully uploaded a custom portal page";
            
        }
        
    }
    
    protected static synchronized void deployWebAppIfRequired(Logger logger)
    {
        if (0 != deployCount++) {
            return;
        }

        LocalUvmContext mctx = LocalUvmContextFactory.context();
        LocalAppServerManager asm = mctx.localAppServerManager();

        Valve v = new OutsideValve()
            {
                protected boolean isInsecureAccessAllowed()
                {
                    return true;
                }

                /* Unified way to determine which parameter to check */
                protected boolean isOutsideAccessAllowed()
                {
                    return false;
                }
            };

        if (null != asm.loadInsecureApp("/users", "users", v)) {
            logger.debug("Deployed authentication WebApp");
        } else {
            logger.error("Unable to deploy authentication WebApp");
        }
    }

    protected static synchronized void unDeployWebAppIfRequired(Logger logger) {
        if (0 != --deployCount) {
            return;
        }

        LocalUvmContext mctx = LocalUvmContextFactory.context();
        LocalAppServerManager asm = mctx.localAppServerManager();

        if (asm.unloadWebApp("/users")) {
            logger.debug("Unloaded authentication webapp");
        } else {
            logger.warn("Uanble to unload authentication WebApp");
        }
    }
}
