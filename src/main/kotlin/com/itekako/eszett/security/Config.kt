package com.itekako.eszett.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler

@Configuration
class Config(@Suppress("UNUSED_PARAMETER") @Autowired employeeDetailsService: EmployeeDetailsService)
        : WebSecurityConfigurerAdapter() {

    @Bean
    fun encoder() = BCryptPasswordEncoder()

    @Bean
    fun roleHierarchy() = RoleHierarchyImpl().apply { setHierarchy("ROLE_SUPERUSER > ROLE_ADMIN") }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .expressionHandler(DefaultWebSecurityExpressionHandler().apply { setRoleHierarchy(roleHierarchy()) })
                .antMatchers("/", "/employees/**", "/companies/**", "/explorer/**").hasRole("ADMIN")
                .anyRequest().hasRole("SUPERUSER")
            .and().formLogin().permitAll()
            .and().logout().permitAll()
            .and().httpBasic()
            .and().csrf().disable()
                  .anonymous().disable()
    }
}