#!/usr/bin/env bash
# shellcheck disable=2215
cd "$(realpath "$(dirname "$0")")" || exit 1
if ! source bindle/project.sh; then
	exit 1
fi

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	-deps
}

## docs:
## Generate api documentation
docs () {
	lein-docs
}

## lint:
lint () {
	-lint &&
	docs &&
	require-committed docs
	abort-on-error 'linting'
}

## clean:
clean () {
	lein-clean
}

## test:
## args: [-r]
test () {
	-test-clj "$@"
}

## test-cljs:
## args: [-b|-r|-n]
## [-n] run tests in NodeJS (default)
## [-r] test refresh in browser
## [-b] run tests in browser
test-cljs () {
	-test-cljs "$@"
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot () {
	-snapshot "$@"
}

## release:
## Pushes a release to Clojars
release () {
	-release
}

deploy () {
	deploy-clojars
}

script-invoke "$@"
