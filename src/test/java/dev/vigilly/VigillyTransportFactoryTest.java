package dev.vigilly;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VigillyTransportFactoryTest {

    @Test
    void rewritesSentryApiPathToVigillyObservePath() {
        assertEquals(
                "https://vigilly.dev/api/observe/77/envelope/",
                VigillyTransportFactory.rewriteUrl("https://vigilly.dev/api/77/envelope/"));
    }

    @Test
    void isIdempotentForAlreadyRewrittenUrls() {
        final String already = "https://vigilly.dev/api/observe/77/envelope/";
        assertEquals(already, VigillyTransportFactory.rewriteUrl(already));
    }

    @Test
    void preservesTheDsnHostAsIs() {
        // Works against any observe env host (prod / staging / local), no per-service subdomain.
        assertEquals(
                "https://staging.vigilly.dev/api/observe/42/envelope/",
                VigillyTransportFactory.rewriteUrl("https://staging.vigilly.dev/api/42/envelope/"));
    }

    @Test
    void leavesUrlsWithoutApiSegmentUnchanged() {
        final String url = "https://vigilly.dev/healthz";
        assertEquals(url, VigillyTransportFactory.rewriteUrl(url));
    }
}
