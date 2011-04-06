/* $HeadURL: svn://chef/work/src/http-casing/localapi/com/untangle/node/http/BlockPageUtil.java */
package com.untangle.node.http;

import java.io.IOException;
import java.util.Map;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.untangle.uvm.RemoteBrandingManager;
import com.untangle.uvm.LocalUvmContext;
import com.untangle.uvm.LocalUvmContextFactory;
import com.untangle.uvm.UvmException;
import com.untangle.uvm.util.I18nUtil;

public class BlockPageUtil
{
    private static final BlockPageUtil INSTANCE = new BlockPageUtil();
    
    private final Logger logger = Logger.getLogger(this.getClass());

    private BlockPageUtil()
    {
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, HttpServlet servlet, BlockPageParameters params)
        throws ServletException
    {
        LocalUvmContext uvm = LocalUvmContextFactory.context();
        RemoteBrandingManager bm = uvm.brandingManager();

        String module = params.getI18n();
        Map<String,String> i18n_map = uvm.languageManager().getTranslations(module);
        request.setAttribute( "i18n_map", i18n_map );

        /* These have to be registered against the request, otherwise
         * the included template cannot see them. */
        request.setAttribute( "ss", uvm.skinManager().getSkinSettings());
        request.setAttribute( "pageTitle", params.getPageTitle( bm, i18n_map ));
        request.setAttribute( "title", params.getTitle( bm, i18n_map ));
        request.setAttribute( "footer", params.getFooter( bm, i18n_map ));
        
        if ( request.getAttribute( "untangle_plus") == null ) {
            boolean untanglePlus = false;
            try {
                untanglePlus = uvm.licenseManager().hasPremiumLicense();
            } catch ( Exception e ) {
                logger.warn( "Unable to load license manager.", e );
                untanglePlus = false;
            }

            request.setAttribute( "untangle_plus", untanglePlus );
        }

        String value = params.getScriptFile();
        if ( value != null ) request.setAttribute( "javascript_file", value );
        value = params.getAdditionalFields( i18n_map );
        if ( value != null ) request.setAttribute( "additional_fields", value );
        request.setAttribute( "description", params.getDescription( bm, i18n_map ));

        /* Register the block detail with the page */
        BlockDetails bd = params.getBlockDetails();
        request.setAttribute( "bd", bd );

        String contactHtml = I18nUtil.marktr("your network administrator");
        if (bm.getContactEmail() != null) {
            String emailSubject = "";
            String emailBody = "";
            try {
                emailSubject = URLEncoder.encode( "site:" + bd.getHost(), "UTF-8" );
                emailBody = URLEncoder.encode("URL:http://" + bd.getHost() + bd.getUri(), "UTF-8");
            } catch (java.io.UnsupportedEncodingException exc) {
                logger.warn("unsupported encoding", exc);
            }
            contactHtml = "<a href='mailto:" + bm.getContactEmail() + "?subject=" + emailSubject + "&body=" + emailBody + "'>" + bm.getContactName() + "</a>";
        }

        request.setAttribute( "contact", I18nUtil.tr("If you have any questions, Please contact {0}.", contactHtml, i18n_map));

        UserWhitelistMode mode = params.getUserWhitelistMode();
        if (( UserWhitelistMode.NONE != mode ) && ( null != bd ) && ( null != bd.getWhitelistHost())) {
            request.setAttribute( "showUnblockNow", true );
            if (UserWhitelistMode.USER_AND_GLOBAL == mode) {
                request.setAttribute( "showUnblockGlobal", true );
            }
        }

        try {
            servlet.getServletConfig().getServletContext()
                .getContext("/blockpage")
                .getRequestDispatcher("/blockpage_template.jspx")
                .forward(request, response);
        } catch ( IOException e ) {
            throw new ServletException( "Unable to render blockpage template.", e );
        }
    }

    public interface BlockPageParameters
    {
        /* An array of modules to load into the i18n array.  For example, it may be
         * webfilter + sitefilter. */
        public String getI18n();

        /* Retrieve the page title (in the window bar) of the page */
        public String getPageTitle( RemoteBrandingManager bm, Map<String,String> i18n_map );

        /* Retrieve the title (top of the pae) of the page */
        public String getTitle( RemoteBrandingManager bm, Map<String,String> i18n_map );

        public String getFooter( RemoteBrandingManager bm, Map<String,String> i18n_map );

        /* Return the name of the script file to load, or null if there is not a script. */
        public String getScriptFile();

        /* Return any additional fields that should go on the page. */
        public String getAdditionalFields( Map<String,String> i18n_map );

        /* Retrieve the description of why this page was blocked. */
        public String getDescription( RemoteBrandingManager bm, Map<String,String> i18n_map );

        public BlockDetails getBlockDetails();

        public UserWhitelistMode getUserWhitelistMode();
    }

    public static BlockPageUtil getInstance()
    {
        return INSTANCE;
    }
}
