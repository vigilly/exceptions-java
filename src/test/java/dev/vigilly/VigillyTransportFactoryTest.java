package dev.vigilly;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VigillyTransportFactoryTest {

    @Test
    void rewritesSentryApiPathToVigillyObservePath() {
        assertEquals(
                "https://my-project.vigilly.dev/api/observe/77/envelope/",
                VigillyTransportFactory.rewriteUrl(
                        "https://my-project.vigilly.dev/api/77/envelope/"));
    }

    @Test
    void isIdempotentForAlreadyRewrittenUrls() {
        final String already = "https://my-project.vigilly.dev/api/observe/77/envelope/";
        assertEquals(already, VigillyTransportFactory.rewriteUrl(already));
    }

    @Test
    void leavesUrlsWithoutApiSegmentUnchanged() {
        final String url = "https://my-project.vigilly.dev/healthz";
        assertEquals(url, VigillyTransportFactory.rewriteUrl(url));
    }
}
