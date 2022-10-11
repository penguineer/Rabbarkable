# Rabbarkable

> [RabbitMQ](https://www.rabbitmq.com/) (AMQP) connector for the [reMarkable](https://remarkable.com/) API

## Configuration

The connector is configured with environment variables:

* `AMQP_HOST`: RabbitMQ host
* `AMQP_USER`: RabbitMQ user
* `AMQP_PASS`: RabbitMQ password
* `AMQP_VHOST`: RabbitMQ virtual host, defaults to '/'
* `AMQP_EXCHANGE`: RabbitMQ binding key to choose the exchange, defaults to `rabbarkable`
* `RMAPI_DEVICETOKEN`: reMarkable device token
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


## API

### reMarkable

A more detailed description on the reMarkable API will follow. The development has heavily benefited from the following
documents:

* https://docs.google.com/document/d/1peZh79C2BThlp2AC3sITzinAQKJccQ1gn9ppdCIWLl8
* https://akeil.de/posts/remarkable-cloud-api/

Please note that I currently have only one account and device to test.
There seem to have been massive changes to the API recently, so please let me know if this implementation does not work.
I am happy to sort things out and, on the way, improve this project.

### RabbitMQ / AMQP

If the reMarkable API sends a SyncComplete event on the WebSocket (this seems to be the only event left), a similar
event is sent to the `AMQP_EXCHANGE` with the following JSON form:

```json
{
  "timestamp": "<ISO8601 timestamp>",
  "root": "<GCS id of root document>",
  "device-id": "<ID of the device that initiated the sync>",
  "type": "sync complete"
}
```

The fields `type` and `device-id` are added as headers (`x-type`, `x-device-id`) to allow value-based binding in a header exchange.

Please note that the `type` is a literal value that signifies a synchronization event.

If the root GCS id is used immediately it may be used to look up the root index. 
Since there are already lots of API calls invoked, it may be safer to trigger the lookup in conjunction with an index 
download.
The root GCS id is mainly intended for cache validation.

**Regarding Reliability:**
Experiments have shown that the WebSocket implementation cannot detect link failures
and will happily go on until an external timeout is sent (see [#15](https://github.com/penguineer/Rabbarkable/issues/15)).
During this period, no notifications are received.
In this sense SyncComplete events should rather be used to improve latency in a polling observation mode, 
and not as the lone source of updates.  

## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))


## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this issue, so that the PRs move the code towards the intended feature.


## License

[MIT](LICENSE.txt) © 2022 Stefan Haun and contributors
