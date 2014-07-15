/**
 * $Id$
 */
package com.untangle.node.ips;

import com.untangle.node.http.HttpStateMachine;
import com.untangle.node.http.RequestLineToken;
import com.untangle.node.http.StatusLine;
import com.untangle.node.token.Chunk;
import com.untangle.node.token.Header;
import com.untangle.uvm.vnet.NodeTCPSession;

class IpsHttpHandler extends HttpStateMachine {

    private IpsDetectionEngine engine;

    protected IpsHttpHandler( IpsNodeImpl node )
    {
        engine = node.getEngine();
    }

    protected RequestLineToken doRequestLine( NodeTCPSession session, RequestLineToken requestLine)
    {
        IpsSessionInfo info = engine.getSessionInfo( session );
        if (info != null) {
            // Null is no longer unusual, it happens whenever we've released the
            // session from the byte pipe.
            String path = requestLine.getRequestUri().normalize().getPath();
            info.setUriPath(path);
        }
        releaseRequest( session );
        return requestLine;
    }

    protected Header doRequestHeader( NodeTCPSession session, Header requestHeader )
    {
        return requestHeader;
    }

    protected void doRequestBodyEnd( NodeTCPSession session ) { }

    protected void doResponseBodyEnd( NodeTCPSession session ) { }

    protected Chunk doResponseBody( NodeTCPSession session, Chunk chunk )
    {
        return chunk;
    }

    protected Header doResponseHeader( NodeTCPSession session, Header header )
    {
        return header;
    }

    protected Chunk doRequestBody( NodeTCPSession session, Chunk chunk )
    {
        return chunk;
    }

    protected StatusLine doStatusLine( NodeTCPSession session, StatusLine statusLine )
    {
        releaseResponse( session );
        return statusLine;
    }
}
