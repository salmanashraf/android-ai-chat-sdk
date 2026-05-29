# Publishing

The repository includes a GitHub Actions release workflow at `.github/workflows/release.yml`.

## Required Secrets

Configure these repository secrets before publishing to Maven Central through the Central Portal Publisher API:

- `GPG_PRIVATE_KEY`: ASCII-armored private signing key.
- `GPG_PRIVATE_KEY_BASE64`: optional safer alternative to `GPG_PRIVATE_KEY`; base64 encoded ASCII-armored private signing key.
- `GPG_PASSPHRASE`: passphrase for the signing key.
- `CENTRAL_PORTAL_USERNAME`: Central Portal user token username.
- `CENTRAL_PORTAL_PASSWORD`: Central Portal user token password.

Use either `GPG_PRIVATE_KEY` or `GPG_PRIVATE_KEY_BASE64`. The base64 form avoids copy/paste issues with multiline GitHub secrets:

```bash
gpg --armor --export-secret-keys 15111AACFE960231 | base64 | tr -d '\n'
```

Copy the single-line output into `GPG_PRIVATE_KEY_BASE64`.

## Manual Release

1. Update `LIB_VERSION` in `gradle.properties`.
2. Update documentation dependency snippets if needed.
3. Push the version commit.
4. Open GitHub Actions.
5. Run the `Release` workflow manually.

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
- Uploads the signed bundle to the Central Portal Publisher API with `publishingType=AUTOMATIC`.
- Polls the Central Portal deployment until it reaches `PUBLISHED` or fails.
