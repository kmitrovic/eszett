package com.itekako.eszett.security

import com.itekako.eszett.model.Employee
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class EmployeePrincipal(val employee: Employee) : UserDetails {

    override fun getUsername() = employee.username?: ""
    override fun getPassword() = employee.password?: ""

    override fun getAuthorities() = mutableListOf(SimpleGrantedAuthority("ROLE_ADMIN"))
    override fun isEnabled() = !employee.username.isNullOrBlank() and !employee.password.isNullOrBlank()

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
}
