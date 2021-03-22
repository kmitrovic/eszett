package com.itekako.eszett.repository

import com.itekako.eszett.model.Company
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : PagingAndSortingRepository<Company, Long>