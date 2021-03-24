package com.itekako.eszett.security

import com.itekako.eszett.repository.EmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class EmployeeDetailsService(@Autowired val employeeRepository: EmployeeRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username.isNullOrBlank()) throw IllegalArgumentException()
        val employee = employeeRepository.findByUsername(username)?: throw UsernameNotFoundException(username)
        return EmployeePrincipal(employee)
    }
}
