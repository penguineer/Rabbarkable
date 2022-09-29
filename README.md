# Rabbarkable

> [RabbitMQ](https://www.rabbitmq.com/) (AMQP) connector for the [reMarkable](https://remarkable.com/) [API](https://akeil.de/posts/remarkable-cloud-api/)

## Configuration

The connector is configured with environment variables:

* `AMQP_HOST`: RabbitMQ host
* `AMQP_USER`: RabbitMQ user
* `AMQP_PASS`: RabbitMQ password
* `AMQP_VHOST`: RabbitMQ virtual host, defaults to '/'
* `RMAPI_DEVICETOKEN`: rmapi device token
* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)

To obtain the reMarkable device token, you need to register an application with [rmapi](https://github.com/juruen/rmapi) and extract it from your `~/.config/rmapi.conf` file.

## Deployment

### Docker

The connector is intended to be run as a Docker container:
```bash
docker run --rm \
  -p 8080:8080 \
  --env-file .env \
  mrtux/rabbarkable
```


### Development

This project uses the [Micronaut Framework](https://micronaut.io/).

Version numbers are determined with [jgitver](https://jgitver.github.io/).
Please check your [IDE settings](https://jgitver.github.io/#_ides_usage) to avoid problems, as there are still some unresolved issues.
If you encounter a project version `0` there is an issue with the jgitver generator.

To use the configuration from a `.env` file, run using [dotenv](https://github.com/therootcompany/dotenv):
```bash
dotenv ./mvnw mn:run
```

## Build

The build is split into two stages:
1. Packaging with [Maven](https://maven.apache.org/)
2. Building the Docker container

This means that the [Dockerfile](Dockerfile) expects one (and only one) JAR file in the target directory.
Build as follows:

```bash
mvn --batch-mode --update-snapshots clean package
docker build .
```

The process is coded in the [docker-publish workflow](.github/workflows/docker-publish.yml) and only needs to be
executed manually for local builds.


## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))


## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this issue, so that the PRs move the code towards the intended feature.


## License

[MIT](LICENSE.txt) © 2022 Stefan Haun and contributors
