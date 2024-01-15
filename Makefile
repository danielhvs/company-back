repl:
	clj -A:dev
main:
	clj -M:runner
docker-build-and-run:
	docker build -t my-company-app .
	docker run -p 3000:3000 my-company-app
start-postgres:
	docker run -e POSTGRES_PASSWORD=postgres --rm -p 5432:5432 postgres:alpine
deps:
	clj -X:deps prep
