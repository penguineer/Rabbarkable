package com.penguineering.rabbarkable;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "Rabbarkable",
                version = "0.1",
                description = "RabbitMQ (AMQP) connector for the reMarkable API",
                license = @License(name = "MIT", url = "https://github.com/penguineer/Rabbarkable/blob/main/LICENSE.txt"),
                contact = @Contact(name = "Stefan Haun", email = "mail@tuxathome.de")
        )
)
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
