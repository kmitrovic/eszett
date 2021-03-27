package com.itekako.eszett

import com.itekako.eszett.repository.CompanyRepository
import com.itekako.eszett.repository.EmployeeRepository
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import javax.transaction.Transactional
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest
class EszettApplicationTests @Autowired constructor(val companyRepository: CompanyRepository,
                                                    val employeeRepository: EmployeeRepository) {

    @Test
    @Transactional
    fun `execute all migrations`() = run {
        assertEquals(3, companyRepository.count())
        assertEquals(3, employeeRepository.count())
    }

}
