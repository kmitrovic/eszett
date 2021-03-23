package com.itekako.eszett

import com.itekako.eszett.model.Company
import com.itekako.eszett.model.Employee
import com.itekako.eszett.repository.CompanyRepository
import com.itekako.eszett.repository.EmployeeRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class EszettApplication

fun main(args: Array<String>) {
    runApplication<EszettApplication>(*args)
}
