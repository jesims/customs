#!/usr/bin/env bash
cd $(realpath $(dirname $0))
# TODO: Source and load from common repository
source ./project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

shadow-cljs () {
	lein trampoline run -m shadow.cljs.devtools.cli $@
}

## clean:
## Cleans up the compiled and generated sources
clean () {
	stop
	lein clean
	rm -rf .shadow-cljs/
}

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	echo_message 'Installing dependencies'
	lein deps
	abort_on_error
	dry install
	abort_on_error
}

## docs:
## Generate api documentation
docs () {
	echo_message 'Generating API documentation'
	# commented out until https://github.com/weavejester/codox/issues/166 is fixed
#	lein codox
#	abort_on_error
}

## stop:
## Stops shadow-cljs and karma
stop () {
	shadow-cljs stop &>/dev/null
	pkill -f 'karma ' &>/dev/null
}

_unit-test () {
	refresh=$1
	clean
	echo_message 'In the animal kingdom, the rule is, eat or be eaten.'
	if [ "${refresh}" = true ];then
		lein auto test ${@:2}
	else
		lein test
	fi
	abort_on_error 'Clojure tests failed'
}

## unit-test:
## args: [-r]
## Runs the Clojure unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
unit-test () {
	case $1 in
		-r)
			_unit-test true ${@:2};;
		*)
			_unit-test;;
	esac
}

unit-test-node () {
	shadow-cljs compile node \
	&& node target/node/test.js
	abort_on_error 'node tests failed'
}

unit-test-karma () {
	shadow-cljs compile karma \
	&& npx karma start --single-run
	abort_on_error 'kamra tests failed'
}

unit-test-browser-refresh () {
	clean
	trap stop EXIT
	open http://localhost:8091/
	shadow-cljs watch browser
	abort_on_error
}

unit-test-cljs-refresh () {
	clean
	echo_message 'In a few special places, these clojure changes create some of the greatest transformation spectacles on earth'
	shadow-cljs compile karma
	abort_on_error
	trap stop EXIT
	npx karma start --no-single-run &
	shadow-cljs watch karma
}

## unit-test-cljs:
## args: [-k|-b|-n|-r] [test-ns-regex]
## Runs the ClojureScript unit tests
## [-k] Watches and compiles tests for execution with karma (Default)
## [-b] Watches and compiles tests for execution within a browser
## [-n] Executes the tests targeting Node.js
## [-r] Watches tests and source files for changes, and subsequently re-evaluates with karma
## [test-ns-regex] Watches tests and source files for changes, and subsequently re-evaluates
unit-test-cljs () {
	export TEST_NS_REGEXP=${2:-'-test$'}
	echo ${TEST_NS_REGEXP}
	case $1 in
		-r)
			unit-test-cljs-refresh;;
		-b)
			unit-test-browser-refresh;;
		-n)
			unit-test-node;;
		-k)
			unit-test-karma;;
		*)
			unit-test-karma;;
	esac
}

is-snapshot () {
	version=$(cat VERSION)
	[[ "$version" == *SNAPSHOT ]]
}

deploy () {
	if [[ -n "$CIRCLECI" ]];then
		lein with-profile install deploy clojars &>/dev/null
		abort_on_error
	else
		lein with-profile install deploy clojars
		abort_on_error
	fi
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot () {
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		version=$(cat VERSION)
		snapshot="$version-SNAPSHOT"
		echo ${snapshot} > VERSION
		echo_message "Snapshotting $snapshot"
		case $1 in
			-l)
				lein with-profile install install
				abort_on_error;;
			*)
				deploy;;
		esac
		echo "$version" > VERSION
	fi
}

## release:
## Pushes a release to Clojars
release () {
	version=$(cat VERSION)
	if ! is-snapshot;then
		version=$(cat VERSION)
		echo_message "Releasing $version"
		deploy
	else
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	fi
}

## test-docs:
## Checks that the committed api documentation is up to date with the latest code
test-docs () {
	docs
	echo_message 'Verifying animal facts...'
	if ! git diff --quiet --exit-code docs;then
		echo_error 'Uncommitted changes to docs'
		exit 1
	fi
}

script-invoke $@
