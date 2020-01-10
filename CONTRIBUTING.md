# Contributing Guidelines

**DO** include tests for all changes (where practical).

**DO** ensure the CI checks pass.

**DO** update the [CHANGELOG](CHANGELOG.md) to reflect changes.

**DO** update the [CONTRIBUTORS](CONTRIBUTORS.md) to append your name (if not already present).

**DO** notify the project maintainers about any PR that has become stale. We're human and sometimes miss notifications.

**Don't** include more than one feature or fix in a single PR.

**Don't** include changes unrelated to the purpose of the PR.

**Don't** open a new PR if changes are requested. Just push to the same branch and the PR will be updated.

## Testing

All utilities should be written to support both Clojure and ClojureScript with unit tests written in **CLJC** where practical.
Unit tests can then be run by invoking `customs.sh test` or `customs.sh test-cljs` respectively.

### Theme: FIXME NEED A THEME

We like to have a theme when testing code that requires data. This projects theme is FIXME NEED A THEME

## Helper Utilities

Many additional automation and helper utilities are provided within [customs.sh](customs.sh). Invoke `customs.sh help` to find out more.

## Versioning

> Customs versions should follow the **Semver** convention.

With version number of `MAJOR.MINOR.PATCH`, increment:

* MAJOR version when you make incompatible API changes,
* MINOR version when you add functionality in a backwards-compatible manner, and
* PATCH version when you make backwards-compatible bug fixes.

Active branches will automatically be suffixed with `-SNAPSHOT` and deployed to Clojars. When starting a change, please bump the [VERSION](VERSION) as applicable.
