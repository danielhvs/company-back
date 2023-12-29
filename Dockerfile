FROM clojure:openjdk-11-tools-deps-slim-bullseye

WORKDIR /src

COPY ./deps.edn /src/deps.edn

RUN clojure -P
RUN clojure -A:build -P

COPY . /src

RUN clojure -T:build uber

RUN cp /src/target/*-standalone.jar /app.jar

EXPOSE 3000

CMD ["java", "-jar", "/app.jar"]
