# exceptions-java

Vigilly exceptions client for Java — a thin, branded wrapper around the MIT-licensed
[`sentry-java`](https://github.com/getsentry/sentry-java) SDK. It presets the transport to
Vigilly's [Observe](https://vigilly.dev) ingest endpoint and exposes a focused `dev.vigilly` API
for capturing exceptions and messages, recording breadcrumbs, and attaching context.

It is the Java sibling of `vigilly/exceptions-js`.

## Why a wrapper

The Sentry client SDKs are MIT-licensed and already speak the exact envelope protocol Vigilly's
ingest accepts. Rather than fork them, this library **depends on `io.sentry:sentry`** and:

- presets the **transport** so events go to Vigilly's ingest path, not Sentry's;
- exposes a small, branded `dev.vigilly.Vigilly` surface (init / capture / breadcrumbs / context);
- ships **errors only** — performance tracing, profiling, metrics and logs are out of scope
  (use the OSS OTLP/Prometheus/Datadog exporters Observe already accepts for those).

The `io.sentry` runtime dependency stays visible. Full white-labeling (a rebranded fork) is
possible later — MIT permits it — if required.

## Installation

Coordinates (group `dev.vigilly`):

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation("dev.vigilly:exceptions-java:0.1.0")
}
```

**Gradle (Groovy DSL)**

```groovy
dependencies {
    implementation 'dev.vigilly:exceptions-java:0.1.0'
}
```

**Maven**

```xml
<dependency>
  <groupId>dev.vigilly</groupId>
  <artifactId>exceptions-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

```java
import dev.vigilly.Vigilly;
import dev.vigilly.VigillyLevel;

public class App {
    public static void main(String[] args) {
        Vigilly.init("https://<publicKey>@<project>.vigilly.dev/<projectId>");

        Vigilly.setTag("region", "eu-west-1");
        Vigilly.addBreadcrumb("starting work", "lifecycle");

        try {
            riskyWork();
        } catch (Exception e) {
            String eventId = Vigilly.captureException(e);
            System.out.println("reported as " + eventId);
        }

        Vigilly.captureMessage("checkpoint reached", VigillyLevel.INFO);

        // On shutdown, flush any buffered events.
        Vigilly.close();
    }
}
```

Or configure via `VigillyOptions`:

```java
import dev.vigilly.Vigilly;
import dev.vigilly.VigillyOptions;

Vigilly.init(new VigillyOptions("https://<publicKey>@<project>.vigilly.dev/<projectId>")
        .setEnvironment("production")
        .setRelease("app@1.4.2")
        .setDebug(false));
```

## DSN format

```
https://<publicKey>@<project>.vigilly.dev/<projectId>
```

- `<publicKey>` — the project's public key, sent as DSN auth in the `X-Sentry-Auth` header.
- `<project>` — the project subdomain on `vigilly.dev`.
- `<projectId>` — the numeric project id.

The underlying SDK would derive the ingest URL `https://<project>.vigilly.dev/api/<projectId>/envelope/`.
Vigilly's ingest lives one segment deeper, so this client rewrites every request to:

```
https://<project>.vigilly.dev/api/observe/<projectId>/envelope/
```

DSN public-key authentication (the `X-Sentry-Auth` header) is preserved unchanged.

## Supported surface

| Capability | Method |
| --- | --- |
| Initialize | `Vigilly.init(dsn)` / `Vigilly.init(VigillyOptions)` |
| Capture an exception | `Vigilly.captureException(Throwable)` |
| Capture a message | `Vigilly.captureMessage(String[, VigillyLevel])` |
| Breadcrumbs | `Vigilly.addBreadcrumb(message[, category])` |
| Tags / extras | `Vigilly.setTag(k, v)`, `Vigilly.setExtra(k, v)` |
| User context | `Vigilly.setUser(id, email, username)`, `Vigilly.clearUser()` |
| Flush / shutdown | `Vigilly.flush(timeoutMillis)`, `Vigilly.close()` |
| Status | `Vigilly.isEnabled()` |

**Out of scope:** performance tracing, profiling, metrics and logs. Use the OSS OTLP / Prometheus /
Datadog exporters (already accepted by Vigilly Observe) for those signals.

## Building

```bash
./gradlew build      # compile + test + jars
./gradlew test       # run unit tests only
```

Requires a JDK 17 or newer to run Gradle; the library is compiled to Java 17 bytecode.

Publishing to Maven Central is configured but **operator-gated** — it requires Sonatype credentials
and a signing key supplied at publish time, and is not part of the normal build.

## License & attribution

Released under the [MIT License](LICENSE).

This library wraps the MIT-licensed Sentry Java SDK (`io.sentry:sentry`). See [NOTICE](NOTICE) for
attribution. Vigilly is not affiliated with or endorsed by Sentry.
