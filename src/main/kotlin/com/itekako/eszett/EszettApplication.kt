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
class EszettApplication {
    private val log = LoggerFactory.getLogger(EszettApplication::class.java)

    @Bean
    fun initDatabase(companyRepository: CompanyRepository, employeeRepository: EmployeeRepository) = CommandLineRunner {
        log.info("Inserting some test data into the database")

        val itekako = Company(0,"Itekako").apply {
            employees = mutableSetOf(
                Employee(0, "Kristijan", "Mitrovic", "km@example.com", 123.23),
                Employee(0, "Djordje", "Kovacevic", "djk@example.com", 321.32))
        }
        val ebf = companyRepository.save(Company(0, "EBF")).apply {
            employees = mutableSetOf(
                Employee(0, "Vladimir", "Spasic", "vs@example.com", 222.22),
                Employee(0, "Nenad", "Nikolic", "nn@example.com", 333.33))
        }

        companyRepository.save(itekako)
        companyRepository.save(ebf)

        log.info("Done with inserting test data")
    }
}

fun main(args: Array<String>) {
    runApplication<EszettApplication>(*args)
}
