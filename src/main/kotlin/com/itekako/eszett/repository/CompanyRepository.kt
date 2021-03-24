package com.itekako.eszett.repository

import com.itekako.eszett.model.Company
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RestResource

@RestResource
interface CompanyRepository : PagingAndSortingRepository<Company, Long>
