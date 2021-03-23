package com.itekako.eszett.repository

import com.itekako.eszett.model.Company
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.PathVariable

@Repository
interface CompanyRepository : PagingAndSortingRepository<Company, Long> {

    @RestResource(exported = false)
    @Query("SELECT AVG(salary) FROM employee WHERE company = :companyId")
    fun averageSalaryById(companyId: Long): Double
}
