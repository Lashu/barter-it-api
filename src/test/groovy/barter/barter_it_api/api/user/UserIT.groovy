package barter.barter_it_api.api.user

import barter.barter_it_api.api.IntegrationSpec
import barter.barter_it_api.api.Problem
import barter.barter_it_api.domain.user.AccessToken
import barter.barter_it_api.domain.user.AuthService
import barter.barter_it_api.domain.user.UserInfoResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import static barter.barter_it_api.Fixtures.*
import static org.awaitility.Awaitility.await
import static org.springframework.http.HttpStatus.*

class UserIT extends IntegrationSpec {

    @Autowired
    AuthService authService

    def 'should perform login request and authenticate user'() {
        given:
            authService.register(authRequest("email@example.com", "password123"))

        and:
            def userLoginRequest = httpRequest(authRequest('email@example.com', 'password123'))

        when:
            def response = http.postForEntity(url("login"), userLoginRequest, AccessToken.class)

        then: 'expecting token'
            response.statusCode == OK
            response.body.value instanceof String
            response.body.expirationDate instanceof LocalDateTime
    }

    def 'should not authenticate non existing user'() {
        given:
            def userLoginRequest = httpRequest(authRequest('test', 'test'))

        when:
            def response = http.postForEntity(url("login"), userLoginRequest, Problem.class)

        then:
            response.statusCode == BAD_REQUEST

        and:
            response.getBody().getCodes().contains("Bad credentials")
    }

    def 'should register new user'() {
        given:
            def userRegistrationRequest = httpRequest(authRequest('newUser', 'newPassword'))

        when:
            def response = http.postForEntity(url("register"), userRegistrationRequest, String.class)

        then:
            response.statusCode == OK
            response.body == 'User: newUser registered'
    }

    def 'should not perform user registration when user has been registered'() {
        given:
            authService.register(authRequest("email@example.com", "password123"))

        and:
            def userRegistrationRequest = httpRequest(authRequest('email@example.com', 'password123'))

        when:
            def response = http.postForEntity(url("register"), userRegistrationRequest, Problem.class)

        then:
            response.statusCode == BAD_REQUEST

        and:
            response.getBody().getCodes().contains("User: email@example.com already exists")
    }

    def 'should refresh authentication token'() {
        given:
            authService.register(authRequest('email@example.com', 'password123'))
            def userLoginRequest = httpRequest(authRequest('email@example.com', 'password123'))
        when:
            def response = http.postForEntity(url('login'), userLoginRequest, AccessToken.class)
            def token = response.body
        and:
            await()
                .pollDelay(1000, TimeUnit.MILLISECONDS)
                .until({ -> true })

            def refreshedToken = http.exchange(url('refresh'),
                HttpMethod.GET,
                httpRequest(null, token.value),
                AccessToken.class).body
        then:
            token.expirationDate != refreshedToken.expirationDate
            token.value != refreshedToken.value
    }

    def 'should not allow to refresh token when user is not authenticated'() {
        when:
            def response = http.getForEntity(url('refresh'), Problem.class)
        then:
            response.statusCode == BAD_REQUEST
            response.body.codes.contains('User must be authenticated')
    }

    def 'should return info about authenticated user'() {
        given:
            authService.register(authRequest('email@example.com', 'password123'))
            def userLoginRequest = httpRequest(authRequest('email@example.com', 'password123'))
        when:
            def response = http.postForEntity(url('login'), userLoginRequest, AccessToken.class)
            def token = response.body
        and:
            def userInfo = http.exchange(
                url('info'),
                HttpMethod.GET,
                httpRequest(null, token.value),
                UserInfoResponse.class
            )
        then:
            userInfo.statusCode.'2xxSuccessful'
            userInfo.body.email == 'email@example.com'
            userInfo.body.id instanceof String
    }
}
