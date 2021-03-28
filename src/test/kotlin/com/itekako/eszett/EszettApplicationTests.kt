package com.itekako.eszett

import com.itekako.eszett.repository.CompanyRepository
import com.itekako.eszett.repository.EmployeeRepository
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.RequestPostProcessor

@SpringBootTest
@AutoConfigureMockMvc
class EszettApplicationTests @Autowired constructor(val mvc: MockMvc,
                                                    val companyRepository: CompanyRepository,
                                                    val employeeRepository: EmployeeRepository) {
    val superuser: RequestPostProcessor = httpBasic("kris", "kris01")
    val admin: RequestPostProcessor = httpBasic("john", "john01")

    private fun MockHttpServletRequestDsl.content(str: String) = str.also { content = it }

    @Test
    fun `basic authorization`() {
        mvc.get("/")
            .andExpect {
                status { isUnauthorized() }
                unauthenticated()
            }

        mvc.perform(formLogin()
            .user("kris")
            .password("kris01"))
            .andExpect {
                authenticated().withUsername("kris")
            }

        mvc.get("/") {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            jsonPath("${'$'}._links") { hasSize<Byte>(3) }
            content {
                json("""{
                    "_links": {
                        "employees": { "href": "http://localhost/employees" },
                        "companies": { "href": "http://localhost/companies" },
                        "profile": { "href": "http://localhost/profile" }
                    }
                }""")
            }
        }
    }

    @Test
    fun `superuser access companies endpoints`() {
        mvc.post("/companies/") {
            with(superuser)
            content("""{ "name": "new" }""")
        }.andExpect {
            status { isCreated() }
            content { emptyString() }
        }

        val result = mvc.get("/companies") {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.companies") { hasSize<Byte>(4) }
                json("""{
                    "_embedded": {
                        "companies": [
                            {
                                "name": "EBF", "avgSalary": 1138.885,
                                "_links": { "self": { "href": "http://localhost/companies/1" } }
                            }, {
                                "name": "Itekako", "avgSalary": 1000.0,
                                "_links": { "self": { "href": "http://localhost/companies/2" } }
                            }, {
                                "name": "empty", "avgSalary": "NaN",
                                "_links": { "self": { "href": "http://localhost/companies/3" } }
                            }, {
                                "name": "new", "avgSalary": "NaN"
                            }
                        ]
                    }
                }""")
            }
        }.andReturn()

        val link = JsonPath.read<List<String>>(result.response.contentAsString,
                                               """${'$'}._embedded.companies.[?(@.name=="new")]._links.self.href""")[0]
        val newCompanyId = link.substringAfter("http://localhost/companies/", "fail").toLong()

        // logged in super user is from company 2, and fully accessing other company's (ID = 3) data
        mvc.put("/companies/{id}", 3) {
            with(superuser)
            content("""{ "name": "replaced" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}", 3) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "replaced", "avgSalary": "NaN",
                    "_links": { "self": { "href": "http://localhost/companies/3" } }
                }""")
            }
        }

        // Use patch test to revert the name of the 3rd company back
        mvc.patch("/companies/{id}", 3) {
            with(superuser)
            content("""{ "name": "empty" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}", 3) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "empty", "avgSalary": "NaN",
                    "_links": { "self": { "href": "http://localhost/companies/3" } }
                }""")
            }
        }

        // delete the company create within this test to actually test deletion, and revert database to the init state
        mvc.delete("/companies/{id}", newCompanyId) {
            with(superuser)
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}", newCompanyId) {
            with(superuser)
        }.andExpect {
            status { isNotFound() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}/employees", 1) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.employees") { hasSize<Byte>(2) }
                json("""{
                    "_embedded": {
                        "employees": [
                            {
                                "name": "John", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/1" } }
                            }, {
                                "name": "Jane", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/2" } }
                            }
                        ]
                    }
                }""")
            }
        }
    }

    @Test
    fun `single company admin access companies endpoints`() {
        mvc.post("/companies/") {
            with(admin)
            content("""{ "name": "new" }""")
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/companies") {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.companies") { hasSize<Byte>(1) }
                json("""{
                    "_embedded": {
                        "companies": [
                            {
                                "name": "EBF", "avgSalary": 1138.885,
                                "_links": { "self": { "href": "http://localhost/companies/1" } }
                            }
                        ]
                    }
                }""")
            }
        }

        // logged in admin is from company 1 - no access to other companys
        mvc.get("/companies/{id}", 3) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }
        mvc.put("/companies/{id}", 3) {
            with(admin)
            content("""{ "name": "tried" }""")
        }.andExpect {
            status { isForbidden() }
        }
        mvc.patch("/companies/{id}", 3) {
            with(admin)
            content("""{ "name": "tried" }""")
        }.andExpect {
            status { isForbidden() }
        }
        mvc.delete("/companies/{id}", 3) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }
        mvc.get("/companies/{id}/employees", 3) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        // all access to their own company except delete (own) company - reserved just for superadmin
        mvc.put("/companies/{id}", 1) {
            with(admin)
            content("""{ "name": "replaced EBF" }""")
        }.andExpect {
            status { isNoContent() }
        }

        mvc.get("/companies/{id}", 1) {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "replaced EBF", "avgSalary": 1138.885,
                    "_links": { "self": { "href": "http://localhost/companies/1" } }
                }""")
            }
        }

        // update back to the original name
        mvc.patch("/companies/{id}", 1) {
            with(admin)
            content("""{ "name": "EBF" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}", 1) {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "EBF", "avgSalary": 1138.885,
                    "_links": { "self": { "href": "http://localhost/companies/1" } }
                }""")
            }
        }

        mvc.delete("/companies/{id}", 1) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/companies/{id}/employees", 1) {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.employees") { hasSize<Byte>(2) }
                json("""{
                    "_embedded": {
                        "employees": [
                            {
                                "name": "John", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/1" } }
                            }, {
                                "name": "Jane", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/2" } }
                            }
                        ]
                    }
                }""")
            }
        }
    }
}
