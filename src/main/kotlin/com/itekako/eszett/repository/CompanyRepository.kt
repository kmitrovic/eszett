package com.itekako.eszett.repository

import com.itekako.eszett.model.Company
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.security.access.prepost.PostFilter

@RepositoryRestResource
interface CompanyRepository : CrudRepository<Company, Long> {
    @PostFilter("hasRole('SUPERUSER') or filterObject.id == principal.getCompanyId()")
    override fun findAll(): MutableIterable<Company>
}
