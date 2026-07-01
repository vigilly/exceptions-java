package dev.vigilly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sentry.Hint;
import io.sentry.ITransportFactory;
import io.sentry.RequestDetails;
import io.sentry.SentryEnvelope;
import io.sentry.SentryOptions;
import io.sentry.transport.ITransport;
import io.sentry.transport.RateLimiter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class VigillyTest {

    private static final String DSN = "https://abc123def456@vigilly.dev/77";
    private static final String EXPECTED_URL =
            "https://vigilly.dev/api/observe/77/envelope/";

    /** Records the URL it is created with and every envelope it is asked to send. */
    private static final class RecordingTransportFactory implements ITransportFactory {
        volatile String url;
        final List<SentryEnvelope> envelopes = new ArrayList<>();

        @Override
        public ITransport create(final SentryOptions options, final RequestDetails requestDetails) {
            this.url = requestDetails.getUrl().toString();
            return new ITransport() {
                @Override
                public void send(final SentryEnvelope envelope, final Hint hint) {
                    envelopes.add(envelope);
                }

                @Override
                public void flush(final long timeoutMillis) {}

                @Override
                public RateLimiter getRateLimiter() {
                    return null;
                }

                @Override
                public void close(final boolean isRestarting) {}

                @Override
                public void close() {}
            };
        }
    }

    @AfterEach
    void tearDown() {
        Vigilly.close();
    }

    @Test
    void initRoutesTransportToVigillyObserveEnvelopeUrl() {
        final RecordingTransportFactory recording = new RecordingTransportFactory();
        Vigilly.init(new VigillyOptions(DSN).setTransportFactory(recording));

        assertEquals(
                EXPECTED_URL,
                recording.url,
                "init must point the transport at the Vigilly Observe ingest path on the DSN host");
    }

    @Test
    void captureExceptionSendsEnvelopeThroughVigillyObserveUrl() {
        final RecordingTransportFactory recording = new RecordingTransportFactory();
        Vigilly.init(new VigillyOptions(DSN).setTransportFactory(recording));

        Vigilly.captureException(new RuntimeException("boom"));
        Vigilly.flush(2000);

        assertFalse(
                recording.envelopes.isEmpty(),
                "captureException must enqueue an envelope to the Vigilly transport");
        assertTrue(
                recording.url.endsWith("/api/observe/77/envelope/"),
                "the envelope must be routed to the Vigilly Observe ingest path");
    }
}
