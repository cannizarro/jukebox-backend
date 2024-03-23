A spring application which can be used as a refrence for:

• Project structuring (parent and child project)

• Spring security 6 with Spring Webflux

• Non blocking DynamoDBEnhanced java client

• Spring Webflux

• Minimal size docker image DockerFile for SpringBoot applications

<br />
<br />

How to run:

• **BUILD**: ```docker buildx build --platform linux/amd64 -t jukebox:0.0.1 .```

• **RUN**: ```docker run --env-file dev.list -p 8080:8080 jukebox:0.0.1```

• **DEPLOY**:
```
    docker tag jukebox:0.0.1 774631798028.dkr.ecr.ap-south-1.amazonaws.com/jukebox:0.0.1

    docker push 774631798028.dkr.ecr.ap-south-1.amazonaws.com/jukebox:0.0.1
```
