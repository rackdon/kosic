package rackdon.kosic.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Configuration
@Validated
@ConfigurationProperties(prefix = "spring.data.mongodb")
@EnableReactiveMongoRepositories
class MongoDbReactiveConfig : AbstractReactiveMongoConfiguration() {
    @NotBlank
    private lateinit var database: String

    public override fun getDatabaseName(): String = database

    override fun reactiveMongoClient(): MongoClient = mongoClient()

    @Bean
    fun mongoClient(): MongoClient = MongoClients.create()

    @Bean
    override fun reactiveMongoTemplate(): ReactiveMongoTemplate = ReactiveMongoTemplate(mongoClient(), databaseName)
}
