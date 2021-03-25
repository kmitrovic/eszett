package com.itekako.eszett.security

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


@Configuration
class Config(private val employeeDetailsService: EmployeeDetailsService) : WebSecurityConfigurerAdapter() {

    @Bean
    fun daoAuthenticationProvider(): AuthenticationProvider = DaoAuthenticationProvider().apply {
            setPasswordEncoder(passwordEncoder())
            setUserDetailsService(employeeDetailsService)
        }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    class PasswordDeserializer : JsonDeserializer<String>() {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): String? {
            val node: JsonNode = p?.codec?.readTree(p) ?: return null
            return BCryptPasswordEncoder().encode(node.asText())
        }
    }


    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                // only a superuser can delete a company, and it can delete any company (even his own!)
                .antMatchers(HttpMethod.DELETE,"/companies/*").hasRole("SUPERUSER")
                .antMatchers("/companies/{id}/**")
                    .access("hasRole('SUPERUSER')" +
                            " or (hasRole('ADMIN') and principal.getCompanyId().toString() == #id)")
                .anyRequest().hasRole("SUPERUSER")
            .and().formLogin().permitAll()
            .and().logout().permitAll()
            .and().httpBasic()
            .and().csrf().disable()
                  .anonymous().disable()
    }
}