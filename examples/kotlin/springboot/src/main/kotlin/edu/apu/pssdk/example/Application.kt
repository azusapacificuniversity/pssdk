package edu.apu.pssdk.example

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
class Application : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "/swagger-ui.html")
    }

    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("APU Advising Notes API")
            .version("1.0.0")
            .description(
                """
                Kotlin + [edu.apu.pssdk](https://github.com/azusapacificuniversity/pssdk) wrapper around the
                **SAA_ADV_NOTE** Component Interface.

                **Flow** (see `AdvisingNoteService.kt`):

                1. `AppServer.fromEnv()` — picks up `PS_APPSERVER_HOSTPORT` / `_USERNAME` / `_PASSWORD` from the environment.
                2. `appServer.ciFactory("SAA_ADV_NOTE")` — opens the JOA session.
                3. `ci.create({EMPLID, INSTITUTION, SAA_NOTE_ID})` — populates the CREATEKEYS.
                4. `ci.save(fullNote)` — sets the rest of the fields plus the `SAA_ADV_NOTEDTL` child rowset.
                5. `ci.close()` releases the JOA session on every path; on an exception
                   `ci.cancel()` runs first, which only invokes the CI's CANCEL operation.
                """.trimIndent()
            )
    )
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
