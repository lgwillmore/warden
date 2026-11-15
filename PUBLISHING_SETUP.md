# Publishing

The library modules (`warden-core`, `warden-atts`, `warden-ktor3`, `warden-http4k6`, `warden-springboot4`,
`warden-springboot4-webflux`) apply the `buildsrc.convention.publish` convention,
which publishes them to Maven Central under `codes.laurence.warden` with the
[vanniktech Maven Publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/): jar, sources, a
javadoc jar holding the Dokka API reference, Gradle module metadata and POM, all signed. Test code and the guide
snippets are never published.

## Versions

The version comes from git. A commit carrying a release tag (`X.Y.Z`, no `v` prefix) builds that version; any other
commit builds a `-SNAPSHOT` such as `0.5.0-3-gabc1234-SNAPSHOT`, which can never become a Maven Central release. Only
the Release workflow creates release tags, so the tags in this repository are exactly the versions Maven Central holds.

Releases up to `0.4.0` (2023) came from the project's earlier life and published `warden-core`, `warden-core-jvm`,
`warden-atts` and `warden-ktor`. Public maintenance resumed with `0.4.1` and `0.5.0` on 2026-11-14, which differ only
in the README: both publish `warden-core`, `warden-atts` and `warden-ktor3`, JVM only and targeting Ktor 3.

Choose the bump by what changed for consumers: `patch` for fixes, `minor` for additions such as a new adapter
artifact, `major` for a change to the public API of an existing artifact, since any such change breaks every
consumer.

## Releasing

Go to Actions → Release → Run workflow on `main`, and choose `patch`, `minor` or `major`. The workflow:

1. runs the Check workflow (ktfmt and the full test suite);
2. tags the commit locally with the next version, bumped from the highest `X.Y.Z` tag;
3. runs `./gradlew publishAndReleaseToMavenCentral`, which uploads all modules as one deployment, releases it, and
   waits for Central to validate it;
4. pushes the tag and creates a GitHub release with generated notes;
5. runs the Docs workflow, which publishes the guides and API reference for the new version to GitHub Pages.

A failed check or publish pushes no tag, so the workflow can simply be run again. The workflow refuses to release a
commit that already carries a release tag, so releasing the same code under a new version needs a new commit on
`main` first. A Maven Central release is permanent: a version can never be replaced or deleted. It is usually
downloadable within 30 minutes and searchable on central.sonatype.com a little later. If publishing succeeded but the
tag push failed, push the tag by hand (`git tag -a X.Y.Z <commit> -m "Release X.Y.Z" && git push origin X.Y.Z`);
re-running would fail because Central rejects an existing version.

## Repository setup

Everything below is in place. It is recorded for rotating a credential or setting up a fork.

- The `codes.laurence` namespace is verified on the [Central Portal](https://central.sonatype.com/) and covers
  `codes.laurence.warden`. The publishing credentials are a user token (Account → Generate User Token), which is per
  account rather than per project, so one token serves every `codes.laurence` library.
- Releases are signed with a GPG key whose public half is on the keyservers, which Central uses to check the
  signatures. To rotate it, create and publish a new key and replace the two signing secrets:

  ```shell
  gpg --quick-gen-key "Your Name <you@example.com>" ed25519 sign never
  gpg --list-secret-keys --keyid-format long          # the key ID follows "ed25519/"
  gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
  gpg --keyserver keys.openpgp.org --send-keys KEY_ID
  gpg --armor --export-secret-keys KEY_ID             # the value of SONATYPE_SIGNING_KEY
  ```

- The Release workflow reads these repository secrets (Settings → Secrets and variables → Actions):

  | Secret | Value |
  | --- | --- |
  | `MAVEN_CENTRAL_PORTAL_USERNAME` | user token username |
  | `MAVEN_CENTRAL_PORTAL_PASSWORD` | user token password |
  | `SONATYPE_SIGNING_KEY` | ASCII-armored private key, including the `BEGIN`/`END` lines |
  | `SONATYPE_SIGNING_PASSWORD` | the key's passphrase |

- Settings → Pages → Source is "GitHub Actions", which the Docs workflow deploys through. GitHub Pages needs the
  repository to be public.

## Publishing from a machine

`./gradlew publishToMavenLocal` installs the current snapshot in `~/.m2` for trying it in another project; snapshots
need no signing key.

To rehearse a release without making it public, tag a commit locally with a version that will never be real
(`git tag 99.0.0`, never pushed), provide the credentials, and upload a deployment that waits for a manual decision:

```shell
export ORG_GRADLE_PROJECT_mavenCentralUsername=...
export ORG_GRADLE_PROJECT_mavenCentralPassword=...
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --armor --export-secret-keys KEY_ID)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=...
./gradlew publishToMavenCentral
```

Inspect the deployment under Publishing → Deployments in the Central Portal, then drop it there and delete the local
tag (`git tag -d 99.0.0`). Pressing Publish there instead makes it a permanent release that has no pushed tag.

## Documentation site

The Docs workflow builds the Writerside guides in `docs/guides` and the Dokka API reference, and deploys them to
https://lgwillmore.github.io/warden/ (API reference under `/api/`). The guides show the latest release tag as the
version to depend on. The workflow runs after each release, on demand from the Actions tab, and without deploying on
every pull request.
