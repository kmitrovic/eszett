package com.itekako.eszett.repository

import com.itekako.eszett.model.Employee
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource

@RepositoryRestResource
interface EmployeeRepository : PagingAndSortingRepository<Employee, Long> {
    fun findByUsername(username: String): Employee
}
