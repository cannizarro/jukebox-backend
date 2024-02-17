FROM eclipse-temurin:21.0.2_13-jdk-jammy as builder
WORKDIR parent/
COPY backend/.mvn ./.mvn
COPY parent/mvnw parent/pom.xml ./
COPY parent/jukebox-config/pom.xml ./jukebox-config/pom.xml
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY parent/jukebox-config ./jukebox-config
RUN ./mvnw clean install
WORKDIR ../backend
COPY backend/.mvn ./.mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY backend/src ./src
RUN ./mvnw clean install

FROM eclipse-temurin:21.0.2_13-jre-jammy
RUN addgroup usergroup; adduser  --ingroup usergroup --disabled-password user
USER user
WORKDIR .
EXPOSE 8080
COPY --from=builder backend/target/*.jar app/*.jar
ENTRYPOINT ["java", "-jar", "app/*.jar" ]
