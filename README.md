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
