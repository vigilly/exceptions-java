package dev.vigilly;

import io.sentry.AsyncHttpTransportFactory;
import io.sentry.ITransportFactory;
import io.sentry.RequestDetails;
import io.sentry.SentryOptions;
import io.sentry.transport.ITransport;

/**
 * Routes Sentry-protocol envelopes to Vigilly's Observe ingest endpoint.
 *
 * <p>sentry-java resolves the envelope endpoint from the DSN as
 * {@code https://<host>/api/<projectId>/envelope/}, but Vigilly's Observe ingest lives one
 * segment deeper at {@code https://<host>/api/observe/<projectId>/envelope/}.
 *
 * <p>This factory intercepts the resolved {@link RequestDetails}, injects the {@code observe/}
 * path segment, and delegates the actual HTTP work to sentry-java's default transport. The DSN
 * public-key auth ({@code X-Sentry-Auth}) and all other headers are preserved unchanged.
 */
final class VigillyTransportFactory implements ITransportFactory {

    /** The path prefix sentry-java derives from a DSN. */
    static final String SENTRY_API_PREFIX = "/api/";

    /** Vigilly's Observe ingest path prefix. */
    static final String VIGILLY_API_PREFIX = "/api/observe/";

    private final ITransportFactory delegate;

    VigillyTransportFactory() {
        this(new AsyncHttpTransportFactory());
    }

    VigillyTransportFactory(final ITransportFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public ITransport create(final SentryOptions options, final RequestDetails requestDetails) {
        return delegate.create(options, rewrite(requestDetails));
    }

    /** Returns a copy of {@code original} whose URL targets the Vigilly Observe ingest path. */
    static RequestDetails rewrite(final RequestDetails original) {
        final String rewritten = rewriteUrl(original.getUrl().toString());
        return new RequestDetails(rewritten, original.getHeaders());
    }

    /**
     * Inserts the {@code observe/} segment after {@code /api/}. Idempotent: a URL already pointing
     * at {@code /api/observe/} is returned unchanged, and a URL without {@code /api/} is left as-is.
     */
    static String rewriteUrl(final String url) {
        final int idx = url.indexOf(SENTRY_API_PREFIX);
        if (idx < 0) {
            return url;
        }
        if (url.startsWith(VIGILLY_API_PREFIX, idx)) {
            return url;
        }
        return url.substring(0, idx)
                + VIGILLY_API_PREFIX
                + url.substring(idx + SENTRY_API_PREFIX.length());
    }
}
