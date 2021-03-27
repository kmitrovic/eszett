package com.itekako.eszett

import com.itekako.eszett.repository.CompanyRepository
import com.itekako.eszett.repository.EmployeeRepository
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class EszettApplicationTests @Autowired constructor(val mvc: MockMvc,
                                                    val companyRepository: CompanyRepository,
                                                    val employeeRepository: EmployeeRepository) {

    @Test
    fun `execute all migrations`() {
        assumeTrue(companyRepository.count().toInt() == 3)
        assumeTrue(employeeRepository.count().toInt() == 3)

        mvc.get("/").andExpect { status { isUnauthorized() } }

        mvc.perform(formLogin().user("kris").password("kris01"))
           .andExpect(authenticated().withUsername("kris"))

        mvc.get("/") { with(httpBasic("kris", "kris01")) }.andExpect { status { isOk() } }
    }
}
