package com.itekako.eszett.repository

import com.itekako.eszett.model.Employee
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize

@RepositoryRestResource
interface EmployeeRepository : CrudRepository<Employee, Long> {

    @RestResource(exported = false)
    fun findByUsername(username: String): Employee?

    @PreAuthorize("hasRole('SUPERUSER') or #entity.company.id == principal.getCompanyId()")
    override fun <S : Employee?> save(entity: S): S

    @PostFilter("hasRole('SUPERUSER') or filterObject.company.id == principal.getCompanyId()")
    override fun findAll(): MutableIterable<Employee>

    @PreAuthorize("hasRole('SUPERUSER') or #entity.company.id == principal.getCompanyId()")
    override fun delete(entity: Employee)
}
