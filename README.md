
# Baajaa Backend

This is the backend of a project which is responsible for sharing a user's Spotify song queue to a different user without a spotify account. Basically a user without a spotify account can easily go and queue a song up in another user's Spotify play queue who is registered on this app.




## Technical Architecture

The technical architecture for this backend is fully [reactive (non-blocking)](https://spring.io/reactive).

- ### Authentication

    - For authentication Spring Security 6 is used with its fully non blocking features.
    - The auth mechanism smartly uses Spotify provided tokens to generate and save custom tokens so that real spotify tokens are not shared. 
    - This generated token is sent encoded in Jwt standards as cookies to the user.
    - The addition and verification of token per request is done with recommended [SpringBoot AOP](https://docs.spring.io/spring-framework/reference/core/aop.html) fashion with various filters applied as the request trickles throught the system.

- ### Parent POM with modularised structure

    - The structure of the project is semantically modularised. Generic dependecies and security defintions are all kept in the [parent folder](https://github.com/cannizarro/jukebox-backend/tree/main/parent) whereas only the actual buisness logic is kept in the [backend folder](https://github.com/cannizarro/jukebox-backend/tree/main/backend).
    - The project structure is maintained as such the [Dockerfile](https://github.com/cannizarro/jukebox-backend/blob/main/Dockerfile) takes minimal time in the docker image build. The [parent folder](https://github.com/cannizarro/jukebox-backend/tree/main/parent) changes way less frequently than its [child](https://github.com/cannizarro/jukebox-backend/tree/main/backend) and hence parent image layers are skipped during the build of a new image for the project.

- ### AWS related decesions
    - DynamoDB is used for database related needs and the client for it is also developed in fully [reactive (non-blocking)](https://spring.io/reactive) fashion using latest AWS SDKs.
    - The project is made fully reactive so that it utilises the full potential of the pod (machine) assigned for running our docker image.
    - The [Dockerfile](https://github.com/cannizarro/jukebox-backend/blob/main/Dockerfile) is also optimised such that it results in minimal iamge size for a SpringBoot backend with reactive capabilities. The size currently is just 140.99 MB.
## Build, Run and Deploy

- BUILD: docker buildx build --platform linux/amd64 -t jukebox:0.0.1 .

- RUN: docker run --env-file dev.list -p 8080:8080 jukebox:0.0.1

- DEPLOY:
```
    docker tag jukebox:0.0.1 774631798028.dkr.ecr.ap-south-1.amazonaws.com/jukebox:0.0.1

    docker push 774631798028.dkr.ecr.ap-south-1.amazonaws.com/jukebox:0.0.1

```
