package com.itekako.eszett.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler
import org.springframework.context.support.beans

@Configuration
class Config(@Suppress("UNUSED_PARAMETER") @Autowired employeeDetailsService: EmployeeDetailsService)
        : WebSecurityConfigurerAdapter() {


    val beans = beans {
        bean<BCryptPasswordEncoder>("encoder")
        bean("roleHierarchy") { RoleHierarchyImpl().apply { setHierarchy("ROLE_SUPERUSER > ROLE_ADMIN") } }
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .expressionHandler(DefaultWebSecurityExpressionHandler().apply { setRoleHierarchy(roleHierarchy()) })
                .antMatchers("/", "/explorer/**").hasRole("ADMIN") // SUPERUSER only - ADMIN juts for testing
                // only superuser creates and deletes companies
                .antMatchers(HttpMethod.POST, "/companies/**").hasRole("SUPERUSER")
                .antMatchers(HttpMethod.DELETE, "/companies/**").hasRole("SUPERUSER")
                // other custom API call are either for superuser or just for admin of the specified company
                .antMatchers("/employees/**", "/companies/**").hasRole("ADMIN")
                // everything else (except for login/logout defined in the following lines) - just for superuser
                .anyRequest().hasRole("SUPERUSER")
            .and().formLogin().permitAll()
            .and().logout().permitAll()
            .and().httpBasic()
            .and().csrf().disable()
                  .anonymous().disable()
    }
}