# 1.3.1

Fixed

* `io.jesi.customs.spy/peek` and `ppeek`  not passing the value through when spy is disabled

# 1.3.0

Added

* `io.jesi.customs.spy/msg`

Fixed:

* `io.jesi.customs.spy/peek` and `ppeek` evaluating the form twice

Removed:

* `js/console.clear` from `io.jesi.customs.runner.browser/start`. \
  If you want the console cleared after reload,
  use [shadow-cljs `^:dev/after-load` lifecycle hook metadata](https://shadow-cljs.github.io/docs/UsersGuide.html#\_metadata):

```clojure
(defn ^:dev/after-load clear-console []
  (js/console.clear))
```

# 1.2.0

Added

* `io.jesi.customs.kaocha/reset-output`

# 1.1.2

Fix

* is-slim-jar not including resource dirs

# 1.1.1

Fix

* StackOverflowError by not using `env/transform`

Added

* Tests in [shadow-cljs](https://github.com/thheller/shadow-cljs)

# 1.1.0

Added

* `io.jesi.customs.leiningen/deps` returns a vector of leiningen project dependencies

# 1.0.0

* First release. Most code taken from [backpack](https://github.com/jesims/backpack)
