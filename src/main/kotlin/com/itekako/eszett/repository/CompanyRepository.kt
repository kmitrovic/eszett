package com.itekako.eszett.repository

import com.itekako.eszett.model.Company
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface CompanyRepository : CrudRepository<Company, Long>
