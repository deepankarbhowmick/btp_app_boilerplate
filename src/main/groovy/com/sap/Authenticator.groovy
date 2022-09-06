package com.sap

import com.fasterxml.jackson.databind.ObjectMapper
import com.sap.cloud.security.jwt.JwtValidation
import com.sap.cloud.security.token.Token
import com.sap.cloud.security.token.TokenClaims
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpRequest
import io.micronaut.http.server.util.HttpHostResolver
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.pivotal.cfenv.core.CfEnv
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Publisher

@Singleton
class Authenticator implements AuthenticationProvider {

    HttpHostResolver hostResolver

    @Inject
    LandingController(HttpHostResolver hostResolver) {
        this.hostResolver = hostResolver
    }

    Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                   AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable<AuthenticationResponse>.create(new FlowableOnSubscribe<AuthenticationResponse>() {
            @Override
            void subscribe(@NonNull FlowableEmitter<AuthenticationResponse> emitter) throws Throwable {
                try {
                    //for the local testing take the dummy token and for the actual server use the JWT.
                    if (hostResolver.resolve(httpRequest).toUpperCase() == 'HTTP://LOCALHOST:8080') {
                        emitter.onNext(AuthenticationResponse.success(authenticationRequest.getIdentity() as String, ['ADMIN']))
                        emitter.onComplete()
                    } else {
                        //this means that the app is actually deployed in BTP.
                        String token = authenticationRequest.getIdentity()
                        Token tokenRead = Token.create(token) //read the token.
                        Map role = new ObjectMapper().readValue(tokenRead.getClaimAsJsonObject('xs.system.attributes')
                                .asJsonString(), Map.class) //read the roles.
                        String email = tokenRead.getClaimAsString(TokenClaims.EMAIL) //read the email.
                        String key = new CfEnv().findServiceByLabel('xsuaa')
                                .getCredentials()
                                .getMap()
                                .get('verificationkey') //Get the public key certificate.
                        key = key.replaceAll('-----BEGIN PUBLIC KEY-----', '')
                                .replaceAll('-----END PUBLIC KEY-----', '')
                                .replaceAll('\\n', '')
                        String validationResult = new JwtValidation().checkJwToken(token.substring(7), key)
                        emitter.onNext(AuthenticationResponse.success(email, role.get('xs.rolecollections') as Collection<String>))
                        emitter.onComplete()
                    }
                }
                catch (Exception exception) {
                    emitter.onNext(AuthenticationResponse.failure(exception.getMessage()))
                    emitter.onComplete()
                }
            }
        }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
    }
}