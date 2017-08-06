package fi.eis.httptests;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MyPoolingNHttpClientConnectionManager extends PoolingNHttpClientConnectionManager {

    public MyPoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry) {
        super(ioreactor, iosessionFactoryRegistry);
    }
    @Override
    public Future<NHttpClientConnection> requestConnection(final HttpRoute route, final Object state, final long connectTimeout, final long leaseTimeout, final TimeUnit tunit,
                                                           final FutureCallback<NHttpClientConnection> callback) {
        System.out.println("requestConnection() with state " + state);
        return super.requestConnection(route, state, connectTimeout, leaseTimeout, tunit, callback);
    }

    @Override
    public void releaseConnection(final NHttpClientConnection managedConn, final Object state, final long keepalive, final TimeUnit tunit) {
        System.out.println("releaseConnection() with state " + state);
        super.releaseConnection(managedConn, state, keepalive, tunit);
    }

}