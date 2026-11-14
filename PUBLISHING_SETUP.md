# Publishing

The library modules (`warden-core`, `warden-atts`, `warden-ktor`) apply the `buildsrc.convention.publish` convention,
which publishes them to Maven Central under `codes.laurence.warden` with the
[vanniktech Maven Publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/): jar, sources, a
javadoc jar holding the Dokka API reference, Gradle module metadata and POM, all signed. The guide snippets are never
published.

## Versions

The version comes from git. A commit carrying a release tag (`X.Y.Z`, no `v` prefix) builds that version; any other
commit builds a `-SNAPSHOT` such as `0.5.0-3-gabc1234-SNAPSHOT`, which can never become a Maven Central release. Only
the Release workflow creates release tags.

Maven Central holds `codes.laurence.warden` releases up to `0.4.0`, and this repository carries the matching release
tags from its earlier life, so the bump counts from `0.4.0`. A `1.0.0` tag that was never published was removed, since
a tag ahead of Central would make the workflow skip versions. The first new release is the first since `0.4.0` in
2023: it drops the multiplatform `warden-core-jvm` artifact and targets Ktor 3. Choose the bump accordingly: `minor`
gives `0.5.0`, `major` gives `1.0.0`.

## Releasing

Go to Actions → Release → Run workflow on `main`, and choose `patch`, `minor` or `major`. The workflow:

1. runs the Check workflow (ktfmt and the full test suite);
2. tags the commit locally with the next version, bumped from the highest `X.Y.Z` tag;
3. runs `./gradlew publishAndReleaseToMavenCentral`, which uploads all modules as one deployment, releases it, and
   waits for Central to validate it;
4. pushes the tag and creates a GitHub release with generated notes;
5. runs the Docs workflow, which publishes the guides and API reference for the new version to GitHub Pages.

A failed check or publish pushes no tag, so the workflow can simply be run again. A Maven Central release is
permanent: a version can never be replaced or deleted. It is usually downloadable within 30 minutes and searchable
on central.sonatype.com a little later. If publishing succeeded but the tag push failed, push the tag by hand
(`git tag -a X.Y.Z <commit> -m "Release X.Y.Z" && git push origin X.Y.Z`); re-running would fail because Central
rejects an existing version.

## One-time setup

1. Sign in to the [Central Portal](https://central.sonatype.com/). The `codes.laurence` namespace is already
   verified for Akashic; `codes.laurence.warden` falls under it, so no new namespace is needed.
2. Generate a user token: Account → Generate User Token. The Akashic token works here too, since tokens are per
   account, not per project.
3. Create a signing key and publish its public half, which Central uses to check the signatures, or reuse the
   Akashic key:

   ```shell
   gpg --quick-gen-key "Your Name <you@example.com>" ed25519 sign never
   gpg --list-secret-keys --keyid-format long          # the key ID follows "ed25519/"
   gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
   gpg --keyserver keys.openpgp.org --send-keys KEY_ID
   gpg --armor --export-secret-keys KEY_ID             # the value of SONATYPE_SIGNING_KEY
   ```

4. Add the repository secrets under Settings → Secrets and variables → Actions:

   | Secret | Value |
   | --- | --- |
   | `MAVEN_CENTRAL_PORTAL_USERNAME` | user token username |
   | `MAVEN_CENTRAL_PORTAL_PASSWORD` | user token password |
   | `SONATYPE_SIGNING_KEY` | ASCII-armored private key, including the `BEGIN`/`END` lines |
   | `SONATYPE_SIGNING_PASSWORD` | the key's passphrase |

5. Set Settings → Pages → Source to "GitHub Actions" so the Docs workflow can deploy. GitHub Pages needs the
   repository to be public.
6. Retire the Netlify site that served the previous documentation, and archive the private interim repository
   (`Plurex/warden`) so there is one home for issues and pull requests.

## Publishing from a machine

`./gradlew publishToMavenLocal` installs the current snapshot in `~/.m2` for trying it in another project; snapshots
need no signing key.

To rehearse a release without making it public, tag a commit locally (`git tag 1.0.0`, never pushed), provide the
credentials, and upload a deployment that waits for a manual decision:

```shell
export ORG_GRADLE_PROJECT_mavenCentralUsername=...
export ORG_GRADLE_PROJECT_mavenCentralPassword=...
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --armor --export-secret-keys KEY_ID)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=...
./gradlew publishToMavenCentral
```

Inspect the deployment under Publishing → Deployments in the Central Portal, then drop it there and delete the local
tag (`git tag -d 1.0.0`). Pressing Publish there instead makes it a permanent release that has no pushed tag.

## Documentation site

The Docs workflow builds the Writerside guides in `docs/guides` and the Dokka API reference, and deploys them to
https://lgwillmore.github.io/warden/ (API reference under `/api/`). It runs after each release, on demand from the
Actions tab, and without deploying on every pull request.
