# codacy-stylelint

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/80607506ff8c4a7f826bbe0b643ba16d)](https://www.codacy.com/gh/codacy/codacy-stylelint?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-stylelint&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/80607506ff8c4a7f826bbe0b643ba16d)](https://www.codacy.com/gh/codacy/codacy-stylelint?utm_source=github.com&utm_medium=referral&utm_content=codacy/codacy-stylelint&utm_campaign=Badge_Coverage)
[![CircleCI](https://circleci.com/gh/codacy/codacy-stylelint.svg?style=svg)](https://circleci.com/gh/codacy/codacy-stylelint)
[![Docker Version](https://images.microbadger.com/badges/version/codacy/codacy-stylelint.svg)](https://microbadger.com/images/codacy/codacy-stylelint "Get your own version badge on microbadger.com")

Docker engine to allow Codacy to have [stylelint](https://github.com/stylelint/stylelint) support.

## Usage

You can create the docker by following these steps:

1) Generate base image:
```
docker build -t codacy-stylelint-base .
```

2) Generate tool image:
```
sbt docker:publishLocal
```

The docker is ran with the following command:

```
docker run -it -v $srcDir:/src  <DOCKER_NAME>:<DOCKER_VERSION>
```

## Docs

[Tool Developer Guide](https://support.codacy.com/hc/en-us/articles/207994725-Tool-Developer-Guide)

[Tool Developer Guide - Using Scala](https://support.codacy.com/hc/en-us/articles/207280379-Tool-Developer-Guide-Using-Scala)

## Test

We use the [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test) to test our external tools integration.
You can follow the instructions there to make sure your tool is working as expected.

## Update tool version

The tool is dependabot friendly, so it is automatically updated.

## Generate Docs

```sh
sbt "doc-generator/run"
```

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
