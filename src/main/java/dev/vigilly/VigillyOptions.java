package dev.vigilly;

import io.sentry.ITransportFactory;

/**
 * Configuration for {@link Vigilly#init(VigillyOptions)}.
 *
 * <p>The only required value is the {@link #getDsn() DSN}, which has the form
 * {@code https://<publicKey>@vigilly.dev/<projectId>}.
 */
public final class VigillyOptions {

    private String dsn;
    private String environment;
    private String release;
    private boolean debug = false;

    /**
     * Advanced/testing hook: a transport factory to delegate the actual HTTP send to. When set,
     * Vigilly still rewrites the ingest URL to the Observe path before handing the request to this
     * factory. Package-private on purpose — it is not part of the public branded surface.
     */
    private ITransportFactory transportFactory;

    public VigillyOptions() {}

    public VigillyOptions(final String dsn) {
        this.dsn = dsn;
    }

    public String getDsn() {
        return dsn;
    }

    public VigillyOptions setDsn(final String dsn) {
        this.dsn = dsn;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public VigillyOptions setEnvironment(final String environment) {
        this.environment = environment;
        return this;
    }

    public String getRelease() {
        return release;
    }

    public VigillyOptions setRelease(final String release) {
        this.release = release;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public VigillyOptions setDebug(final boolean debug) {
        this.debug = debug;
        return this;
    }

    ITransportFactory getTransportFactory() {
        return transportFactory;
    }

    VigillyOptions setTransportFactory(final ITransportFactory transportFactory) {
        this.transportFactory = transportFactory;
        return this;
    }
}
