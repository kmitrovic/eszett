package com.itekako.eszett.controller

import com.itekako.eszett.repository.CompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/companies")
class CompanyController(@Autowired val companyRepository: CompanyRepository) {

    @GetMapping("/{id}/avgSalary")
    fun avgSalaryInCompany(@PathVariable id: Long) = companyRepository.averageSalaryById(id)
}