package barter.barter_it_api.domain.user

import barter.barter_it_api.api.ValidationException
import barter.barter_it_api.api.security.JwtTokenProvider
import barter.barter_it_api.infrastructure.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service

@Service
class AuthService(
        private val jwtTokenProvider: JwtTokenProvider,
        private val authenticationManager: AuthenticationManager,
        private val userRepository: UserRepository
) {
    fun login(email: String, password: String): String? {
        return try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
            jwtTokenProvider.createToken(email, password)
        } catch (ex: AuthenticationException) {
            throw ValidationException("Bad credentials")
        }
    }

    fun register(userAuthRequest: UserAuthRequest): String? {
        userAuthRequest.let {
            if (userRepository.existsByEmail(it.email)) {
                throw ValidationException("User: ${it.email} already exists")
            }
            userRepository.save(it.toUser())
            return "User: ${it.email} registered"
        }
    }
}