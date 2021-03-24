package com.itekako.eszett.security

import com.itekako.eszett.model.Employee
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class EmployeePrincipal(val employee: Employee) : UserDetails {

    override fun getUsername(): String = employee.username?: ""
    override fun getPassword(): String = employee.password?: ""

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_ADMIN"))
    override fun isEnabled(): Boolean = !employee.username.isNullOrBlank() and !employee.password.isNullOrBlank()

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true

}