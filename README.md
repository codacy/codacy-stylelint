# codacy-stylelint

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/80607506ff8c4a7f826bbe0b643ba16d)](https://app.codacy.com/gh/codacy/codacy-stylelint?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-stylelint&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/codacy/codacy-stylelint.svg?style=svg)](https://circleci.com/gh/codacy/codacy-stylelint)

Docker engine to allow Codacy to have [stylelint](https://github.com/stylelint/stylelint) support.

## Usage

Create docker:

1.  Generate tool binary

```sh
sbt stage
```

2.  Generate tool docker

```sh
docker build -t codacy-stylelint .
```

Run docker:

```sh
docker run -it -v $srcDir:/src  <DOCKER_NAME>:<DOCKER_VERSION>
```

## Add a new plugin
In DocGenerator, listOfPlugins method, add a new plugin with all the necessary details and run npm install {plugin} and doc generator.

## Generate Docs

```sh
sbt "doc-generator/run"
```

## Test

We use the [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test) to test our external tools integration.
You can follow the instructions there to make sure your tool is working as expected.

## Docs

[Tool Developer Guide](https://support.codacy.com/hc/en-us/articles/207994725-Tool-Developer-Guide)

[Tool Developer Guide - Using Scala](https://support.codacy.com/hc/en-us/articles/207280379-Tool-Developer-Guide-Using-Scala)

## Agent Playbook: Updating This Repository End-to-End

This section is written for an AI coding agent (or a human) tasked with updating this repo — most commonly bumping the wrapped [stylelint](https://github.com/stylelint/stylelint) version (or one of its plugins), but also base image / orb / dependency bumps. Follow it top to bottom; it tells you what to change, how to regenerate derived files, how to test locally, and how to interpret CI so you can iterate on failures without guessing.

### 1. What this repository is

This is a **Codacy engine**: a hybrid Scala + Node.js wrapper. The runtime engine (`src/main/scala/codacy/Engine.scala` and `src/main/scala/codacy/stylelint/Stylelint.scala`, built on `codacy-engine-scala-seed`) shells out to the real [stylelint](https://github.com/stylelint/stylelint) npm package (plus the `stylelint-scss` and `stylelint-a11y` plugins, and a long list of `stylelint-config-*`/postcss packages used only as runtime config/parsers) and packages everything as a Docker image Codacy's platform can run against a customer's source code. The `docs/` directory is not just documentation — it is **machine-consumed configuration**:

- `docs/patterns.json` — the full list of stylelint (+ plugin) rules ("patterns") Codacy knows about, their parameters/defaults, and which are enabled out of the box. Generated file, do not hand-edit.
- `docs/description/description.json` + `docs/description/*.md` — human-readable titles/descriptions per pattern, used in the Codacy UI. Generated file, do not hand-edit.
- `docs/multiple-tests/*` — fixtures used by `codacy-plugins-test` to validate the engine actually produces the results it claims to for real code samples.
- `docs/tool-description.md` — short blurb about the tool, hand-maintained.

All three generated artifacts above come from **`DocGenerator`** (`doc-generator/src/main/scala/codacy/stylelint/DocGenerator.scala`), a separate sbt subproject (`doc-generator`) that, for each plugin listed in `listOfPlugins()` (currently `stylelint`, `stylelint-scss`, `stylelint-a11y`), reads that plugin's pinned version straight out of `package.json`'s `dependencies`, `git clone`s the plugin's upstream GitHub repo, `git reset --hard`s it to that version, walks its rules directory, and parses each rule's `README.md` (via the flexmark-java Markdown parser, a normal JVM library dependency declared in `build.sbt` — no external `pandoc`/network CLI tool needed beyond `git`) to build the pattern list and descriptions. This means the generator needs **network access** and **git** locally, but no extra system packages.

### 2. Files that encode versions — check all of these on every update

| File | What it controls | What to check |
|---|---|---|
| `package.json` → `dependencies.stylelint` | The actual stylelint version shipped in the image, and the git tag `DocGenerator` checks out to regenerate docs for it | Bump the `^x.y.z` value. Confirm a matching tag exists in [stylelint/stylelint](https://github.com/stylelint/stylelint/tags). |
| `package.json` → `dependencies["stylelint-scss"]` / `dependencies["stylelint-a11y"]` | Versions of the two plugins that also contribute patterns via `DocGenerator` | Bump/check the same way as stylelint itself if the task scopes these plugins. |
| `package.json` → other `stylelint-config-*`/postcss/prettier dependencies | Config presets and syntax parsers available at runtime (not doc-generated) | Bump as scoped by the task; these don't affect `docs/patterns.json`. |
| `package-lock.json` | Locked resolution of the above | Regenerate via `npm install` after editing `package.json` — do not hand-edit. |
| `build.sbt` → `codacy-engine-scala-seed` version, `scalaVersion` | Codacy's engine SDK/base library and Scala compiler version | Only touch if explicitly asked to bump the SDK; unrelated to stylelint version bumps. |
| `Dockerfile` → base image (`node:lts-alpine3.23`) | Node.js runtime the packaged app runs on | Only bump if asked explicitly, or if the new stylelint version raises its minimum Node requirement (check stylelint's release notes/engines field) — don't bump opportunistically. |
| `Dockerfile` → `apk add openjdk11-jre-headless` | JRE used to run the Scala engine binary inside the image | Rarely needs touching; check only if the Scala/sbt toolchain bump requires a newer JRE. |
| `.circleci/config.yml` → `codacy/base` orb | Shared CircleCI steps (checkout, versioning, sbt build, docker publish, tagging) | Check the latest published version (CircleCI orb registry, or `git log -p .circleci/config.yml` for prior bump history as a fallback reference). |
| `.circleci/config.yml` → `codacy/plugins-test` orb | Runs `codacy-plugins-test` in CI after the image is built | Same as above. |
| `project/build.properties` / `project/plugins.sbt` | sbt version / sbt plugins | Rarely needs touching; check only if the build itself fails to load. |

Note: as of this writing, `docs/patterns.json` shows `"version": "17.13.0"` while `package.json` pins `stylelint` at `^16.26.1` — a leftover mismatch from a prior in-flight bump that was partially reverted. Always regenerate `docs/patterns.json` (step 3.2) after any `package.json` change so the two stay consistent; don't assume the checked-in file already reflects the pinned version.

### 3. Step-by-step update procedure

1. **Bump the version(s)** in `package.json` (and run `npm install` to refresh `package-lock.json`), and any CI orb versions in `.circleci/config.yml`, as scoped by the task.
2. **Regenerate the docs.** Requires `git` and network access: `sbt "doc-generator/run"`. Review the diff for new/removed/renamed patterns in `docs/patterns.json` and `docs/description/*` and any stale `docs/multiple-tests` fixture references.
3. **Compile, format and stage the Scala binary:** `sbt "scalafmtCheckAll; scalafmtSbtCheck; doc-generator/run; stage"` (this is exactly what `npm test` and the CI `publish_docker_local` job run).
4. **Build the Docker image**: `docker build -t codacy-stylelint .`
5. **Run `codacy-plugins-test` locally** before pushing — clone https://github.com/codacy/codacy-plugins-test and run the relevant DockerTest commands (this repo's CI runs `run_multiple_tests: true`, i.e. the `multiple` test suite against `docs/multiple-tests/*`) against your local image tag.
6. **Iterate on failures**, re-running only the relevant DockerTest command after each fix.
7. **Commit** the version bump(s) together with the regenerated `docs/` files (and `package-lock.json`) in one change.
8. **Push and open a PR.** CI runs `checkout_and_version` -> `publish_docker_local` (scalafmt + doc-generator + stage + docker build) -> `plugins_test` -> `publish_docker` (master only) -> `tag_version`.
9. **Poll the PR's real CI checks until they all pass — local validation is NOT the finish line.** After every push, run `gh pr checks <pr-url>` and keep re-polling (short sleep while any check is `pending`) until all checks finish. If a check fails, fetch its actual log (CircleCI API/UI for the failing job — don't guess), find the true root cause, fix it, push again (never `--no-verify`, never force-push), and re-poll. Repeat until every check is green. **The CI environment's toolchain can differ from your local one**, so a clean local run does not guarantee CI passes. Only stop iterating when every check passes, or you hit a genuine product/infra decision that needs a human — in which case explain it in the PR rather than guessing.

### 4. Common failure modes and fixes

| Symptom | Likely cause | Fix |
|---|---|---|
| `scalafmtCheckAll`/`scalafmtSbtCheck` fails in CI/locally | Generated or hand-edited Scala file not formatted | Run `sbt scalafmt` then re-run the check command |
| `doc-generator/run` fails to clone a plugin repo | Wrong/nonexistent version tag for `stylelint`, `stylelint-scss`, or `stylelint-a11y` in `package.json` | Verify the tag exists upstream in that plugin's GitHub repo |
| `docs/patterns.json` version field doesn't match `package.json` after regenerating | Regeneration wasn't actually run, or was run before the `package.json` edit | Re-run `sbt "doc-generator/run"` after bumping `package.json` and re-check the diff (see the note in section 2) |
| `multiple` DockerTest fails on a `docs/multiple-tests/*` fixture | Rule renamed/removed/added or behavior changed upstream between versions | Regenerate docs, then update the expected results in the fixture to match the new (verified correct) output |

### 5. Definition of done

- Version bump(s) reflected in all files that encode them (`package.json`, `package-lock.json`, and CI orbs if applicable).
- `docs/patterns.json` and `docs/description/*` regenerated via `sbt "doc-generator/run"` and committed, with fixture inconsistencies in `docs/multiple-tests/*` resolved.
- Local `scalafmtCheckAll`/`scalafmtSbtCheck`/`stage` commands pass.
- Docker image builds successfully.
- `codacy-plugins-test` (multiple-tests) passes locally against the freshly built image.
- **After pushing and opening/updating the PR, every CI check on it is green.** Poll `gh pr checks <pr-url>` and iterate on any failure (fetch the real CI log, fix, push, re-poll) until all pass — a passing local build is not sufficient, because the CI toolchain can differ from your local one (see step 9).

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

-   Identify new Static Analysis issues
-   Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
-   Auto-comments on Commits and Pull Requests
-   Integrations with Slack, HipChat, Jira, YouTrack
-   Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
