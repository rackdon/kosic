package rackdon.kosic.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Value("\${info.app.name}")
    private var appName: String? = "app name"

    @Value("\${info.app.description}")
    private var appDescription: String? = "app description"

    @Bean
    fun customOpenAPI(): OpenAPI? {
        return OpenAPI()
                .components(Components())
                .info(Info().title(appName)
                        .description(appDescription))
    }
}
