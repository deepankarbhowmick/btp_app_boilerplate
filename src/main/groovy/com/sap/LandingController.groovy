package com.sap

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.server.util.HttpHostResolver
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.views.View
import io.pivotal.cfenv.core.CfEnv
import jakarta.inject.Inject

@Controller('/')
class LandingController {

    HttpHostResolver hostResolver
    ApplicationContext applicationContext

    @Inject
    LandingController(HttpHostResolver hostResolver, ApplicationContext applicationContext) {
        this.hostResolver = hostResolver
    }

    @Get('/')
    @Secured(SecurityRule.IS_ANONYMOUS)
    @View('landing')
    Map landingZone(HttpRequest httpRequest) {
        String uri
        if (this.hostResolver.resolve(httpRequest).toUpperCase() == 'HTTP://LOCALHOST:8080') {
            return ['token': 'DUMMY', 'uri': this.hostResolver.resolve(httpRequest)]
        } else {
            uri = "https://" + new CfEnv().getApp().getApplicationUris()[0] //actual URL when deployed in the cloud.
            return ['token': httpRequest.getHeaders().getAuthorization().get(), 'uri': uri]
        }
    }
}
