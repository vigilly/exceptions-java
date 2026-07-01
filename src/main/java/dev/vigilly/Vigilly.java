package dev.vigilly;

import io.sentry.AsyncHttpTransportFactory;
import io.sentry.ITransportFactory;
import io.sentry.Sentry;
import io.sentry.protocol.SentryId;
import io.sentry.protocol.User;

/**
 * Vigilly exceptions client for Java.
 *
 * <p>A thin, branded wrapper around the MIT-licensed
 * <a href="https://github.com/getsentry/sentry-java">sentry-java</a> SDK. It presets the transport
 * to Vigilly's Observe ingest endpoint and exposes a focused {@code dev.vigilly} surface for
 * capturing exceptions and messages, recording breadcrumbs, and attaching context.
 *
 * <pre>{@code
 * Vigilly.init("https://<publicKey>@vigilly.dev/<projectId>");
 * try {
 *     doWork();
 * } catch (Exception e) {
 *     Vigilly.captureException(e);
 * }
 * }</pre>
 */
public final class Vigilly {

    /** Client version, reported to the ingest endpoint via the {@code sentry_client} auth field. */
    static final String VERSION = "0.1.0";

    private Vigilly() {}

    /**
     * Initializes the client with a Vigilly DSN of the form
     * {@code https://<publicKey>@vigilly.dev/<projectId>}.
     */
    public static void init(final String dsn) {
        init(new VigillyOptions(dsn));
    }

    /** Initializes the client from a {@link VigillyOptions} instance. */
    public static void init(final VigillyOptions vigillyOptions) {
        if (vigillyOptions == null) {
            throw new IllegalArgumentException("vigillyOptions is required");
        }
        final String dsn = vigillyOptions.getDsn();
        if (dsn == null || dsn.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A Vigilly DSN is required, e.g. "
                            + "https://<publicKey>@vigilly.dev/<projectId>");
        }

        final ITransportFactory override = vigillyOptions.getTransportFactory();
        final ITransportFactory delegate =
                override != null ? override : new AsyncHttpTransportFactory();

        Sentry.init(options -> {
            options.setDsn(dsn);
            // Route every envelope to Vigilly's Observe ingest path (.../api/observe/<id>/envelope/).
            options.setTransportFactory(new VigillyTransportFactory(delegate));
            options.setSentryClientName("vigilly-java/" + VERSION);
            options.setDebug(vigillyOptions.isDebug());
            if (vigillyOptions.getEnvironment() != null) {
                options.setEnvironment(vigillyOptions.getEnvironment());
            }
            if (vigillyOptions.getRelease() != null) {
                options.setRelease(vigillyOptions.getRelease());
            }
            // The Vigilly exceptions client ships errors only. Performance tracing is left
            // disabled (sample rate stays null) and is not part of the supported surface.
        });
    }

    /** Captures an exception/throwable. Returns the event id, or all-zeros if disabled. */
    public static String captureException(final Throwable throwable) {
        final SentryId id = Sentry.captureException(throwable);
        return id.toString();
    }

    /** Captures a message at {@link VigillyLevel#INFO}. Returns the event id. */
    public static String captureMessage(final String message) {
        return Sentry.captureMessage(message).toString();
    }

    /** Captures a message at the given level. Returns the event id. */
    public static String captureMessage(final String message, final VigillyLevel level) {
        return Sentry.captureMessage(message, level.toSentryLevel()).toString();
    }

    /** Records a breadcrumb (trail of events leading up to an error). */
    public static void addBreadcrumb(final String message) {
        Sentry.addBreadcrumb(message);
    }

    /** Records a breadcrumb with a category. */
    public static void addBreadcrumb(final String message, final String category) {
        Sentry.addBreadcrumb(message, category);
    }

    /** Sets a string tag on the current scope (indexed, filterable). */
    public static void setTag(final String key, final String value) {
        Sentry.setTag(key, value);
    }

    /** Sets an arbitrary extra value on the current scope (not indexed). */
    public static void setExtra(final String key, final String value) {
        Sentry.setExtra(key, value);
    }

    /** Associates the current scope with a user. Any argument may be {@code null}. */
    public static void setUser(final String id, final String email, final String username) {
        final User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        Sentry.setUser(user);
    }

    /** Clears the user from the current scope. */
    public static void clearUser() {
        Sentry.setUser(null);
    }

    /** Blocks up to {@code timeoutMillis} for queued events to be flushed to Vigilly. */
    public static void flush(final long timeoutMillis) {
        Sentry.flush(timeoutMillis);
    }

    /** Whether the client has been initialized and is active. */
    public static boolean isEnabled() {
        return Sentry.isEnabled();
    }

    /** Flushes and shuts the client down. */
    public static void close() {
        Sentry.close();
    }
}
