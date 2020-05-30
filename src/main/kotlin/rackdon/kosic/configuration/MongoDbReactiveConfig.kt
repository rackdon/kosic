package rackdon.kosic.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.validation.annotation.Validated
import rackdon.kosic.controller.dto.ValidationMessage
import rackdon.kosic.repository.GroupMongo
import javax.validation.constraints.NotBlank

@Profile("test", "mongo", "local-mongo")
@Configuration
@Validated
@ConfigurationProperties(prefix = "spring.data.mongodb")
@EnableReactiveMongoRepositories(basePackageClasses = [GroupMongo::class])
class MongoDbReactiveConfig : AbstractReactiveMongoConfiguration() {
    @NotBlank(message = ValidationMessage.NOT_BLANCK)
    var database: String = ""

    override fun getDatabaseName(): String = database

    @Bean
    fun mongoClient(): MongoClient = MongoClients.create()
}
