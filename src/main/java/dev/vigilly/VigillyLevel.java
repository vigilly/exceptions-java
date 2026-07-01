package dev.vigilly;

import io.sentry.SentryLevel;

/** Severity level attached to a captured message. */
public enum VigillyLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    FATAL;

    SentryLevel toSentryLevel() {
        switch (this) {
            case DEBUG:
                return SentryLevel.DEBUG;
            case INFO:
                return SentryLevel.INFO;
            case WARNING:
                return SentryLevel.WARNING;
            case FATAL:
                return SentryLevel.FATAL;
            case ERROR:
            default:
                return SentryLevel.ERROR;
        }
    }
}
