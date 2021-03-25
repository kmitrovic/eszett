package com.itekako.eszett.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.context.support.beans

@Configuration
class Config(@Suppress("UNUSED_PARAMETER") private val employeeDetailsService: EmployeeDetailsService)
        : WebSecurityConfigurerAdapter() {


    val beans = beans {
        bean<BCryptPasswordEncoder>("encoder")
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