# QA Automation Portfolio

[![CI](https://github.com/tj-guruvelli/qa-automation-portfolio/actions/workflows/ci.yml/badge.svg)](https://github.com/tj-guruvelli/qa-automation-portfolio/actions/workflows/ci.yml)

Two small, real, runnable UI/API test suites against the public demo app
[the-internet.herokuapp.com](https://the-internet.herokuapp.com), wired into
GitHub Actions and a Jenkins declarative pipeline.

| Suite | Stack | Tests | Runs in |
|---|---|---|---|
| `selenium-java/` | Java 21, Selenium 4.50.0, JUnit 5.14.4, Maven Wrapper (Maven 3.9.16), Surefire 3.6.0 | 5 | ~45 s |
| `playwright-ts/` | Node 20+, TypeScript, @playwright/test 1.63.0, Chromium only | 6 (4 UI + 2 API) | ~20 s |

## What it tests

All against `https://the-internet.herokuapp.com` (override with `BASE_URL`):

| Scenario | Page | Selenium | Playwright |
|---|---|---|---|
| Valid login lands on `/secure` with success banner | `/login` | `LoginTest` | `login.spec.ts` |
| Wrong password shows error banner, stays on `/login` | `/login` | `LoginTest` | `login.spec.ts` |
| Select an option by visible text | `/dropdown` | `DropdownTest` | `dropdown.spec.ts` |
| Element rendered after an async delay is waited for explicitly | `/dynamic_loading/2` | `DynamicLoadingTest` | `dynamic-loading.spec.ts` |
| Negative: 404 status page explains the status | `/status_codes/404` | `StatusCodeTest` (UI) | `status-codes.api.spec.ts` (`request`, no browser) |
| 200 status endpoint responds OK | `/status_codes/200` | - | `status-codes.api.spec.ts` |

The login credentials used (`tomsmith` / `SuperSecretPassword!`) are the demo
app's published test credentials, not secrets.


## System overview

```mermaid
flowchart LR
    subgraph Dev["Developer"]
        push["git push / pull request"]
    end
    subgraph CI["Continuous integration"]
        gha["GitHub Actions<br/>ci.yml"]
        jenkins["Jenkins<br/>Jenkinsfile"]
    end
    subgraph Suites["Test suites"]
        sel["selenium-java<br/>JUnit 5 + Selenium 4<br/>Page Object Model"]
        pw["playwright-ts<br/>@playwright/test<br/>UI + request API"]
    end
    subgraph SUT["System under test"]
        app["the-internet.herokuapp.com"]
    end
    subgraph Out["Evidence"]
        junit["JUnit XML reports"]
        shots["Screenshots on failure"]
        trace["Playwright traces + HTML report"]
    end
    push --> gha
    push --> jenkins
    gha --> sel
    gha --> pw
    jenkins --> sel
    jenkins --> pw
    sel -->|Chrome headless| app
    pw -->|Chromium headless| app
    pw -->|HTTP request| app
    sel --> junit
    sel --> shots
    pw --> junit
    pw --> trace
```

### Layered test architecture

```mermaid
flowchart TB
    cfg["Config layer<br/>TestConfig.java / playwright.config.ts<br/>BASE_URL, BROWSER, HEADLESS, retries"]
    drv["Driver layer<br/>DriverFactory.java (Selenium Manager)<br/>Playwright fixtures (page, request)"]
    pages["Page layer<br/>BasePage, LoginPage, DropdownPage,<br/>DynamicLoadingPage, StatusCodePage"]
    tests["Test layer<br/>LoginTest, DropdownTest, DynamicLoadingTest, StatusCodeTest<br/>login.spec, dropdown.spec, dynamic-loading.spec, status-codes.api.spec"]
    support["Support layer<br/>BaseTest, ScreenshotOnFailure, quarantine tag"]
    ci["CI layer<br/>GitHub Actions jobs, Jenkins parallel stages"]
    cfg --> drv --> pages --> tests
    support --> tests
    tests --> ci
```

### A test run, end to end

```mermaid
sequenceDiagram
    participant CI as CI runner
    participant T as Test
    participant F as DriverFactory
    participant P as Page object
    participant B as Browser
    participant A as App under test
    CI->>T: run suite (headless)
    T->>F: create(BROWSER, HEADLESS)
    F-->>T: WebDriver / Page
    T->>P: login("tomsmith", "SuperSecretPassword!")
    P->>B: locate by id / role, explicit wait
    B->>A: HTTP GET /login, POST /authenticate
    A-->>B: /secure with success banner
    B-->>P: element visible
    P-->>T: result
    alt assertion passes
        T-->>CI: JUnit pass
    else assertion fails
        T->>B: capture screenshot / trace
        T-->>CI: JUnit fail + artifact
    end
    T->>F: quit()
```

## Design decisions and consequences

Each decision is recorded as context, decision, and consequences so the trade off is visible to the next maintainer.

### 1. Page Object Model over inline locators
- **Context.** Five scenarios share the same pages, and locator churn is the most common cause of UI suite breakage.
- **Decision.** Every page gets one class (`pages/`) that owns its locators and user actions. Tests only call page methods and assert outcomes.
- **Consequences.** A selector change is a one file fix. Tests read as user intent, not DOM. Cost is more files for a small suite, accepted because the pattern is what scales.

### 2. Explicit waits only, no `Thread.sleep`
- **Context.** Fixed sleeps either waste CI minutes or flake when the runner is slower than a laptop.
- **Decision.** Selenium uses `WebDriverWait` with expected conditions. Playwright relies on its built in auto wait and web first assertions.
- **Consequences.** Suite runtime tracks real page readiness. A genuine slowdown fails with a clear timeout instead of a random assertion error.

### 3. Selenium Manager instead of hand managed drivers
- **Context.** Driver and browser version drift is a recurring CI failure.
- **Decision.** Selenium 4 resolves the matching ChromeDriver at runtime. `DriverFactory` only reads `BROWSER` and `HEADLESS`.
- **Consequences.** No driver binaries in the repo and no version pinning chores. Requires network access to resolve drivers on a cold runner, which is acceptable in CI.

### 4. Role and id based locators, never positional CSS
- **Context.** `nth-child` and layout coupled selectors break on any CSS refactor.
- **Decision.** Prefer `getByRole`, `getByLabel`, and stable ids. Positional selectors are rejected in review.
- **Consequences.** Tests survive styling changes. If the app lacks accessible names, the right fix is to add them to the app, not to the test.

### 5. Both Selenium Java and Playwright TypeScript
- **Context.** Enterprise estates run Selenium with Java. New web work defaults to Playwright. A QA engineer has to operate in both.
- **Decision.** Keep the two suites independent with the same scenarios, so either can be dropped or extended without touching the other.
- **Consequences.** Some duplication across suites, accepted on purpose. Playwright carries the API tests because its `request` fixture needs no browser.

### 6. API checks live beside UI checks
- **Context.** Business rules in HTTP are cheaper to test than through a browser.
- **Decision.** Status code behaviour is asserted once through the UI (Selenium) and once at the HTTP layer (Playwright `request`).
- **Consequences.** Shows the cheapest layer principle in a small repo. New rules should be added at the API layer first and only promoted to UI when composition matters.

### 7. Screenshots and traces on failure, not on every run
- **Context.** A red test without evidence is an incomplete bug report, but artifacts on every pass bloat CI storage.
- **Decision.** Selenium captures a screenshot through a JUnit extension only when a test fails. Playwright keeps a trace on first retry.
- **Consequences.** Failures are diagnosable from the CI artifact alone. Storage stays small.

### 8. One infra retry, then quarantine
- **Context.** Retrying until green hides product defects and teaches the team to ignore red builds.
- **Decision.** Playwright retries once in CI only. Tests that fail twice in seven days without a product change are tagged `quarantine`, excluded from the gate, and given an owner and a 14 day expiry (see Flake policy).
- **Consequences.** The main gate stays trustworthy. Quarantine is a work queue, not a junk drawer, and expired quarantine fails the build.

### 9. Two CI definitions for the same jobs
- **Context.** The target environment may run GitHub Actions or Jenkins.
- **Decision.** `ci.yml` and `Jenkinsfile` mirror each other with two parallel jobs, JUnit publishing, and artifact upload.
- **Consequences.** Portable across CI platforms. Any change to the test command has to be made in both files.

### 10. Public demo app as the system under test
- **Context.** The portfolio needs a stable target without credentials or infrastructure.
- **Decision.** Test `the-internet.herokuapp.com` using its published demo credentials. `BASE_URL` overrides the target.
- **Consequences.** Anyone can clone and run. The suite depends on a third party site being up, so a nightly schedule, not every PR, is the right cadence for the full run in a real project.

## Architecture

### Selenium Java (`selenium-java/`)

```
src/main/java/io/github/tejaguruvelli/qa/
  config/TestConfig.java        env-driven config: BASE_URL, BROWSER, HEADLESS, EXPLICIT_WAIT_SECONDS
  driver/DriverFactory.java     builds Chrome / Edge / Firefox from BROWSER + HEADLESS (default: chrome, headless)
  pages/BasePage.java           open(), explicit-wait helpers (visible / clickable / invisibility / url)
  pages/LoginPage.java
  pages/DropdownPage.java
  pages/DynamicLoadingPage.java
  pages/StatusCodePage.java
src/test/java/io/github/tejaguruvelli/qa/
  support/BaseTest.java         one fresh browser per test, quit in @AfterEach
  support/ScreenshotOnFailure.java  JUnit 5 extension: PNG to target/screenshots/ on failure
  tests/*Test.java              the 5 tests
```

- **Page Object Model**: every page owns its locators and the waits it needs.
  Tests read like the scenario, not like the DOM.
- **Driver factory**: no WebDriverManager and nothing on `PATH`. Selenium 4's
  built-in Selenium Manager downloads the chromedriver that matches the
  installed Chrome.
- **Explicit waits only**: implicit wait is pinned to zero in the factory, and
  there is no `Thread.sleep` anywhere. Each synchronisation names the condition
  it needs (`visibilityOfElementLocated`, `invisibilityOfElementLocated`,
  `urlContains`).
- **Screenshots on failure**: `ScreenshotOnFailure` runs as an
  `AfterTestExecutionCallback`, which fires before `@AfterEach` quits the driver,
  so the browser is still alive to capture. Files land in `target/screenshots/`
  and are uploaded by CI.
- **Surefire**: JUnit Platform provider, full stack traces, XML to
  `target/surefire-reports/`. Tests tagged `quarantine` are excluded from the
  default run.

### Playwright TypeScript (`playwright-ts/`)

```
playwright.config.ts   baseURL from BASE_URL, retries 1 in CI only, trace on-first-retry,
                       html + list + junit reporters, screenshot only-on-failure
pages/*.ts             page objects exposing Locators (role/label based) and actions
tests/*.spec.ts        4 UI specs + 1 API spec using the `request` fixture
```

- Locators are `getByRole` / `getByRole('heading', { level })` / `selectOption({ label })`
  wherever the markup has semantics. The login inputs have no `<label>`, so
  they use their stable `id`s. No `nth-child` CSS anywhere.
- Web-first assertions (`toHaveURL`, `toBeHidden`, `toHaveText`) do the
  waiting; there is no `waitForTimeout`.
- The API spec never launches a browser.

## Run locally

Prerequisites: Java 21, Node 20+, Google Chrome. Nothing else is installed
system-wide; Maven comes from the wrapper and chromedriver from Selenium Manager.

### Selenium Java

```bash
cd selenium-java
./mvnw -B test                         # Linux / macOS / Git Bash
.\mvnw.cmd -B test                     # Windows cmd / PowerShell
```

Options (all environment variables):

| Variable | Default | Notes |
|---|---|---|
| `BROWSER` | `chrome` | `chrome`, `edge`, `firefox` |
| `HEADLESS` | `true` | set `false` to watch the browser |
| `BASE_URL` | `https://the-internet.herokuapp.com` | |
| `EXPLICIT_WAIT_SECONDS` | `15` | upper bound for every explicit wait |

Run one class: `./mvnw test -Dtest=LoginTest`. Run quarantined tests on their
own: `./mvnw test -Dgroups=quarantine -DexcludedGroups=`.

**Windows notes (observed on the machine this was built on)**

- `mvnw.cmd` requires `JAVA_HOME`. Set it for the shell only, no system change:
  `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'` (PowerShell) or
  `JAVA_HOME="C:\Program Files\Java\jdk-21" ./mvnw test` (Git Bash).
- Behind HTTPS-inspecting security software, the JVM rejects Maven Central's
  certificate (`PKIX path building failed`) even though `curl` works. Point the
  JVM at the Windows certificate store instead of its own truststore:
  `MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"`. This applies to
  both the wrapper download and dependency resolution.
- The Oracle JDK 21 found at `C:\Program Files\Java\jdk-21` was missing
  `lib\tzdb.dat`, which makes Maven fail on startup with
  `FileNotFoundException: ...\lib\tzdb.dat`. `java -version` still works, so
  this is easy to miss. The suite was verified with a portable Temurin
  21.0.12.1 unzipped into a scratch folder and passed as `JAVA_HOME`. Fix
  properly by reinstalling the JDK (or install Temurin 21 and point
  `JAVA_HOME` at it).

Full Git Bash command that was used to produce the passing run on Windows:

```bash
cd selenium-java
JAVA_HOME="<path to a complete JDK 21>" \
MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT" \
cmd //c ".\mvnw.cmd -B -ntp test"
```

### Playwright TypeScript

```bash
cd playwright-ts
npm ci
npx playwright install chromium        # add --with-deps on a fresh Linux box
npm test                               # headless
npm run test:headed                    # watch it
npm run test:smoke                     # @smoke only
npm run test:api                       # @api only, no browser
npm run report                         # open the HTML report
```

`BASE_URL` and `HEADLESS=false` are honoured the same way as the Java suite.
Set `CI=true` to get the CI behaviour (1 retry, `forbidOnly`, 2 workers).

## CI

### GitHub Actions (`.github/workflows/ci.yml`)

Runs on every `push` and `pull_request`, two independent jobs on `ubuntu-latest`:

| Job | Setup | Artifacts (always uploaded) |
|---|---|---|
| `selenium-java` | `actions/setup-java` Temurin 21 with Maven cache, `browser-actions/setup-chrome` stable | `target/surefire-reports/`, `target/screenshots/` |
| `playwright-ts` | `actions/setup-node` 20 with npm cache, `npx playwright install --with-deps chromium`, typecheck | `playwright-report/`, `test-results/` (traces, JUnit XML) |

### Jenkins (`Jenkinsfile`)

Declarative pipeline with one `parallel` stage holding the same two suites.
Each branch publishes JUnit XML (`junit`) and archives its report directory
(`archiveArtifacts`). Expects a Linux agent with tools named `jdk21` and
`node20` configured in Global Tool Configuration and a Chrome binary on `PATH`.

#### Run it on a local Jenkins controller (`jenkins/`)

`jenkins/` holds a reproducible controller so the pipeline is executed, not just
written. First green run: 2026-10-17, build #1, 59 s, 5 Selenium tests and 6
Playwright tests passed.

| File | What it does |
|---|---|
| `jenkins/Dockerfile` | `jenkins/jenkins:lts-jdk21` plus Chromium (symlinked as `google-chrome` for Selenium Manager), `sudo` for `playwright install --with-deps`, and the plugin set below. Setup wizard disabled. |
| `jenkins/plugins.txt` | `workflow-aggregator`, `git`, `github`, `nodejs`, `junit`, `timestamper`, `configuration-as-code`, `job-dsl`, `pipeline-stage-view` |
| `jenkins/jenkins.yaml` | JCasC: admin user from `JENKINS_ADMIN_PASSWORD`, tool `jdk21` pointing at the image JDK, tool `node20` via the NodeJS installer, and a Job DSL script that creates the `qa-automation-portfolio` pipeline job from this repo's `Jenkinsfile`. |

```bash
cd jenkins
docker build -t tj-jenkins-qa:lts-jdk21 .
docker run -d --name tj-jenkins -p 8085:8080 -e JENKINS_ADMIN_PASSWORD=change-me tj-jenkins-qa:lts-jdk21
# open http://localhost:8085/job/qa-automation-portfolio/ and click Build Now,
# or trigger over the API with a crumb and the session cookie:
curl -s -c cj.txt -u admin:change-me http://localhost:8085/crumbIssuer/api/json
```

Lesson from the first attempt: `options { timestamps() }` is provided by the
`timestamper` plugin, not core. Without it the Jenkinsfile fails at parse time
with `Invalid option type "timestamps"`. It is in `plugins.txt` for that reason.

## Flake policy

A test is **flaky** when it has both passed and failed on the same commit.

1. **One infra retry, never more.** Playwright `retries: 1` in CI only; locally
   it is 0 so failures are loud. The Java suite has no retry at all: a UI test
   that needs a retry is a test with a missing wait, and the fix is the wait.
2. **Quarantine instead of delete.** A test confirmed flaky for a product or
   environment reason it cannot control is tagged `quarantine`
   (`@Tag("quarantine")` in JUnit, `{ tag: '@quarantine' }` in Playwright) and
   excluded from the PR gate (`excludedGroups` in `pom.xml`,
   `--grep-invert @quarantine` in the nightly job). It keeps running in the
   nightly lane so the signal is not lost.
3. **Quarantine has an owner and an expiry.** The tag is added in a commit that
   links the issue and names a date. Past the date, the test is fixed or deleted;
   it does not sit in quarantine forever.
4. **No sleeps, no `nth-child`, no shared browser.** These are the three
   root causes of almost every flaky UI test; the suites forbid them by
   construction (explicit waits, semantic locators, fresh driver per test).
5. **Retries are evidence, not a fix.** A retried-then-passed run is recorded
   (Playwright marks it `flaky` in the report) and triaged like a failure.

## Test strategy (one pager)

**Risk.** The app under test is a public demo with no business logic of its
own, so the risk this portfolio demonstrates mitigation for is the risk every
web product has: authentication (the gate to everything), forms and controls
(the most common user action), asynchronous rendering (the most common source
of false failures), and error handling (what the user sees when the server
says no). Each suite covers one scenario per risk.

**Layers.**

| Layer | Tool | What it proves | Cost |
|---|---|---|---|
| API | Playwright `request` | status codes and payload contracts, independent of the UI | milliseconds |
| UI, headless Chromium | Playwright | user journeys with web-first assertions, traces on retry | seconds |
| UI, real Chrome via WebDriver | Selenium + JUnit | the same journeys through the W3C WebDriver protocol, the stack most enterprise teams still run | tens of seconds |

Two UI stacks on purpose: it shows the same page-object discipline survives a
change of tooling, and it makes the two frameworks' trade-offs concrete
(auto-waiting assertions versus explicit `WebDriverWait`, trace viewer versus
screenshots).

**PR gate versus nightly.**

| Lane | Runs | Scope | Budget |
|---|---|---|---|
| PR gate | every push and pull request (GitHub Actions) | `@smoke` and `@api`, `quarantine` excluded, Chromium/Chrome only | under 5 minutes, zero retries locally, one retry in CI |
| Nightly | scheduled (add `schedule:` to the workflow or a Jenkins cron trigger) | everything including `quarantine` and `@negative`, optionally `BROWSER=firefox` / `edge` | may take longer; failures create tickets, not blocked merges |

**Exit criteria for a PR.** Both jobs green, no new `quarantine` tag without a
linked issue, and no test in the diff that uses a sleep or a positional CSS
selector.

## After cloning / pushing

1. Create a GitHub repository and push `main`.
2. The CI badge at the top points at `tj-guruvelli/qa-automation-portfolio`.
3. In Jenkins, configure JDK 21 as tool `jdk21` and Node 20 as `node20`, then
   create a Multibranch Pipeline pointing at the repo.
