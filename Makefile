repl:
	clj -X:deps prep
	clj -A:dev
main:
	clj -M:runner
docker-build-and-run:
	docker build -t my-company-app .
	docker run -p 3000:3000 my-company-app
