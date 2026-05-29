# Publishing

The repository includes a GitHub Actions release workflow at `.github/workflows/release.yml`.

## Required Secrets

Configure these repository secrets before publishing to Sonatype or Maven Central:

- `GPG_PRIVATE_KEY`: ASCII-armored private signing key.
- `GPG_PASSPHRASE`: passphrase for the signing key.
- `OSSRH_USERNAME`: Sonatype/OSSRH token username.
- `OSSRH_PASSWORD`: Sonatype/OSSRH token password.

## Manual Release

1. Update `LIB_VERSION` in `gradle.properties`.
2. Update documentation dependency snippets if needed.
3. Push the version commit.
4. Open GitHub Actions.
5. Run the `Release` workflow manually.
6. Set `publish_to_sonatype` to `true` when you want the workflow to publish.

You can also provide a `version` input to override `LIB_VERSION` for that workflow run.

## Tag Release

Pushing a tag like `v1.0.3` runs the workflow automatically:

```bash
git tag v1.0.3
git push origin v1.0.3
```

For tag runs, the workflow uses the tag name as the publication version.

## What The Workflow Does

- Runs `:ai-chat-lib:testDebugUnitTest`.
- Compiles the sample app with `:sampleapp:compileDebugKotlin`.
- Builds the release AAR.
- Imports the GPG signing key.
- Builds the signed Central Portal component zip.
- Uploads the AAR and Central Portal zip as workflow artifacts.
- Publishes to the configured Sonatype/OSSRH repository.
