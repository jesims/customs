#!/usr/bin/env bash
#shellcheck disable=2215
cd "$(realpath "$(dirname "$0")")" &&
source bindle/project.sh
if [ $? -ne 0 ];then
	exit 1
fi

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	-deps "$@"
}

## docs:
## Generate api documentation
docs () {
	lein-docs
}

## lint:
lint () {
	-lint
	docs
	require-committed docs
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

deploy-snapshot () {
	deploy-clojars
}

## outdated:
outdated(){
	-outdated
}

## test-shadow:
## args: [-r][-k|-b|-n]
## Runs the ClojureScript unit tests using shadow-cljs and
## [-k] Executes the tests targeting the browser running in karma (default)
## [-n] Executes the tests targeting Node.js
## [-b] Watches and compiles tests for execution within a browser
## [-r] Watches tests and source files for changes, and subsequently re-evaluates with node
test-shadow(){
	-test-shadow-cljs "$@"
}

script-invoke "$@"
