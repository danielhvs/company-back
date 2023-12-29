Clojure Backend Application
===========
  - Uses [tools.deps](https://clojure.org/guides/deps_and_cli) as a build tool.
  - Uses [clj-ring](https://github.com/ring-clojure/ring) lib to abstract the details of HTTP.
  - Uses [compojure](https://github.com/weavejester/compojure) lib for webservices http routing.
  - Uses [clj-pdf](https://github.com/clj-pdf/clj-pdf) lib to generate the pdf files.
  - Uses [dali](https://github.com/stathissideris/dali) lib to generate svg images.
  - Uses [docker](https://www.docker.com/) to dockerize the uberjar so it is easy to deploy anywhere.
  - Usage: `make docker-build-and-run` or `docker compose up`
