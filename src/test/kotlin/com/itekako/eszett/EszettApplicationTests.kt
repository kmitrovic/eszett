package com.itekako.eszett

import com.itekako.eszett.repository.CompanyRepository
import com.itekako.eszett.repository.EmployeeRepository
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.*
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

    val companiesCount = 3
    val adminsCompanyId = 1
    val superusersCompanyId = 2
    val emptyCompanyId = 3

    val employeesCount = 3
    val adminEmployeeId = 1  // within company 1
    val regularEmployeeId = 2  // within company 1
    val superuserEmployeeId = 3  // within company 2

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

        listOf(superuser, admin).forEach {user ->
            mvc.get("/") {
                with(user)
            }.andExpect {
                authenticated()
                status { isOk() }
                header { content { contentType("application/hal+json") } }
                jsonPath("${'$'}._links") { hasSize<Byte>(3) }
                content {
                    json(
                        """{
                        "_links": {
                            "employees": { "href": "http://localhost/employees" },
                            "companies": { "href": "http://localhost/companies" },
                            "profile": { "href": "http://localhost/profile" }
                        }
                    }"""
                    )
                }
            }
        }
    }

    @Test
    fun `HTTP PUT is blocked for everybody`() {
        listOf(superuser, admin).forEach {user ->
            mvc.put("/companies/{id}", emptyCompanyId) {
                with(user)
                content("""{ "name": "replaced" }""")
            }.andExpect {
                status {
                    isForbidden()
                }
            }

            mvc.put("/employees/{id}", regularEmployeeId) {
                with(user)
                content("""{ "name": "new", "surname": "surnew", "email": "new@example.com", "salary": 722.23,
                         "company": "/companies/$adminsCompanyId" }""")
            }.andExpect {
                status {
                    isForbidden()
                }
            }
        }
    }

    @Test
    fun `superuser allowed access to all companies`() {
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
                jsonPath("${'$'}._embedded.companies") { hasSize<Byte>(companiesCount + 1) }
                json("""{
                    "_embedded": {
                        "companies": [
                            {
                                "name": "EBF", "avgSalary": 1138.885,
                                "_links": { "self": { "href": "http://localhost/companies/$adminsCompanyId" } }
                            }, {
                                "name": "Itekako", "avgSalary": 1000.0,
                                "_links": { "self": { "href": "http://localhost/companies/$superusersCompanyId" } }
                            }, {
                                "name": "empty", "avgSalary": "NaN",
                                "_links": { "self": { "href": "http://localhost/companies/$emptyCompanyId" } }
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

        // logged in super user can fully accessing other company's data
        mvc.patch("/companies/{id}", newCompanyId) {
            with(superuser)
            content("""{ "name": "updated" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}", newCompanyId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "updated", "avgSalary": "NaN",
                    "_links": { "self": { "href": "http://localhost/companies/$newCompanyId" } }
                }""")
            }
        }

        // delete the company created within this test to actually test deletion, and revert database to the init state
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
    }

    @Test
    fun `single company admin forbidden access to other company`() {
        mvc.post("/companies/") {
            with(admin)
            content("""{ "name": "new" }""")
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/companies/{id}", emptyCompanyId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        mvc.patch("/companies/{id}", emptyCompanyId) {
            with(admin)
            content("""{ "name": "tried" }""")
        }.andExpect {
            status { isForbidden() }
        }

        mvc.delete("/companies/{id}", emptyCompanyId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `single company admin allowed access (except delete) to his own company`() {
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
                                "_links": { "self": { "href": "http://localhost/companies/$adminsCompanyId" } }
                            }
                        ]
                    }
                }""")
            }
        }

        // all access to their own company except delete (own) company - reserved just for superadmin
        // update back to the original name
        mvc.patch("/companies/{id}", adminsCompanyId) {
            with(admin)
            content("""{ "name": "updated EBF" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{id}", adminsCompanyId) {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "updated EBF", "avgSalary": 1138.885,
                    "_links": { "self": { "href": "http://localhost/companies/$adminsCompanyId" } }
                }""")
            }
        }

        // revert back the old name
        mvc.patch("/companies/{id}", adminsCompanyId) {
            with(admin)
            content("""{ "name": "EBF" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.delete("/companies/{id}", adminsCompanyId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `superuser allowed access to all companies' employees`() {
        mvc.get("/employees") {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.employees") { hasSize<Byte>(employeesCount) }
                json("""{
                    "_embedded": {
                        "employees": [
                            {
                                "name": "John", "surname": "Doe", "salary": 1500.0,
                                "_links": { "self": { "href": "http://localhost/employees/$adminEmployeeId" } }
                            }, {
                                "name": "Jane", "surname": "Doe", "salary": 777.77,
                                "_links": { "self": { "href": "http://localhost/employees/$regularEmployeeId" } }
                            }, {
                                "name": "Kristijan", "surname": "Mitrovic", "salary": 1000.0,
                                "_links": { "self": { "href": "http://localhost/employees/$superuserEmployeeId" } }
                            }
                        ]
                    }
                }""")
            }
        }

        mvc.post("/employees/") {
            with(superuser)
            content("""{ "name": "new", "surname": "surnew", "email": "new@example.com", "salary": 722.23,
                         "company": "/companies/$adminsCompanyId" }""")
        }.andExpect {
            status { isCreated() }
            content { emptyString() }
        }

        val result = mvc.get("/companies/{id}/employees", adminsCompanyId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.employees") { hasSize<Byte>(3) }
                json("""{
                    "_embedded": {
                        "employees": [
                            {
                                "name": "John", "surname": "Doe", "salary": 1500.0,
                                "_links": { "self": { "href": "http://localhost/employees/$adminEmployeeId" } }
                            }, {
                                "name": "Jane", "surname": "Doe", "salary": 777.77,
                                "_links": { "self": { "href": "http://localhost/employees/$regularEmployeeId" } }
                            }, {
                                "name": "new", "surname": "surnew", "salary": 722.23
                            }
                        ]
                    }
                }""")
                // total salary is 3000.0 for 3 employees - avg is 1000.0
            }
        }.andReturn()
        val link = JsonPath.read<List<String>>(result.response.contentAsString,
                                       """${'$'}._embedded.employees.[?(@.name=="new")]._links.self.href""")[0]
        val newEmployeeId = link.substringAfter("http://localhost/employees/", "fail").toLong()

        // check accessing company details within that new employee's endpoint. btw check the new average salary
        mvc.get("/employees/{id}/company", newEmployeeId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "EBF", "avgSalary": 1000.0,
                    "_links": { "self": { "href": "http://localhost/companies/$adminsCompanyId" } }
                }""")
            }
        }

        // use following update calls to move the employee around from one company to another
        mvc.patch("/employees/{id}", newEmployeeId) {
            with(superuser)
            content("""{ "name": "replaced", "surname": "replaced", "company": "/companies/$emptyCompanyId" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/employees/{id}", newEmployeeId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "replaced", "surname": "replaced",
                    "_links": { "self": { "href": "http://localhost/employees/$newEmployeeId" } }
                }""")
            }
        }

        mvc.get("/employees/{id}/company", newEmployeeId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{ "_links": { "self": { "href": "http://localhost/companies/$emptyCompanyId" } } }""")
            }
        }

        // check if average salary is updated back on the 1st company
        mvc.get("/companies/{id}", adminsCompanyId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            content { jsonPath("${'$'}.avgSalary") { lessThan(3000) } }
        }

        // Use patch test to revert the name of the 3rd company back
        mvc.patch("/employees/{id}", newEmployeeId) {
            with(superuser)
            content("""{ "surname": "updated" }""")
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/companies/{companyId}/employees/{employeeId}", emptyCompanyId, newEmployeeId) {
            with(superuser)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "replaced", "surname": "updated",
                    "_links": { "self": { "href": "http://localhost/employees/$newEmployeeId" } }
                }""")
            }
        }

        // delete the employee created within this test to actually test deletion, and revert database to the init state
        mvc.delete("/employees/{id}", newEmployeeId) {
            with(superuser)
        }.andExpect {
            status { isNoContent() }
            content { emptyString() }
        }

        mvc.get("/employees/{id}", newEmployeeId) {
            with(superuser)
        }.andExpect {
            status { isNotFound() }
            content { emptyString() }
        }
    }

    @Test
    fun `single company admin forbidden access to other company's employees`() {
        mvc.post("/employees") {
            with(admin)
            content(
                """{ "name": "new", "surname": "surnew", "email": "new@example.com", "salary": 0,
                         "company": "/companies/$superusersCompanyId" }"""
            )
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/employees/{id}", superuserEmployeeId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        // cannot access even his own company employees (not his own employee record's details) through following URI
        mvc.get("/employees/{id}", adminEmployeeId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        // update other company's employee with trying to move it into this admin's company
        mvc.patch("/employees/{id}", superuserEmployeeId) {
            with(admin)
            content("""{ "company": "/companies/$adminsCompanyId" }""")
        }.andExpect {
            status { isForbidden() }
        }

        // and the other way around - cannot move his own company's employee into some other company (not even self)
        mvc.patch("/employees/{id}", adminEmployeeId) {
            with(admin)
            content("""{ "company": "/companies/$superusersCompanyId" }""")
        }.andExpect {
            status { isForbidden() }
        }

        mvc.delete("/employees/{id}", superuserEmployeeId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/employees/{id}/company", superuserEmployeeId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/companies/{id}/employees", superusersCompanyId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }

        mvc.get("/companies/{companyid}/employees/{employeeId}", superusersCompanyId, superuserEmployeeId) {
            with(admin)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `single company admin allowed access to his own company's employee`() {
        mvc.post("/employees") {
            with(admin)
            content("""{ "name": "new", "surname": "admins-surnew", "email": "new@example.com", "salary": 0,
                         "company": "/companies/$adminsCompanyId" }""")
        }.andExpect {
            status { isCreated() }
        }

        val result = mvc.get("/employees") {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                jsonPath("${'$'}._embedded.employees") { hasSize<Byte>(3) }
                json("""{
                    "_embedded": {
                        "employees": [
                            {
                                "name": "John", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/$adminEmployeeId" } }
                            }, {
                                "name": "Jane", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/$regularEmployeeId" } }
                            }, {
                                "name": "new", "surname": "admins-surnew"
                            }
                        ]
                    }
                }""")
            }
        }.andReturn()
        val link = JsonPath.read<List<String>>(result.response.contentAsString,
                                       """${'$'}._embedded.employees.[?(@.name=="new")]._links.self.href""")[0]
        val newEmployeeId = link.substringAfter("http://localhost/employees/", "fail").toLong()

        // all access to their own company's employees
        mvc.patch("/employees/{id}", newEmployeeId) {
            with(admin)
            content("""{ "name": "updated", "surname": "surupdated" }""")
        }.andExpect {
            status { isNoContent() }
        }

        mvc.get("/companies/{companyId}/employees/{employeeId}", adminsCompanyId, newEmployeeId) {
            with(admin)
        }.andExpect {
            status { isOk() }
            header { content { contentType("application/hal+json") } }
            content {
                json("""{
                    "name": "updated", "surname": "surupdated",
                    "_links": { "self": { "href": "http://localhost/employees/$newEmployeeId" } }
                }""")
            }
        }

        mvc.delete("/employees/{id}", newEmployeeId) {
            with(admin)
        }.andExpect {
            status { isNoContent() }
        }

        mvc.get("/companies/{id}/employees", adminsCompanyId) {
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
                                "_links": { "self": { "href": "http://localhost/employees/$adminEmployeeId" } }
                            }, {
                                "name": "Jane", "surname": "Doe",
                                "_links": { "self": { "href": "http://localhost/employees/$regularEmployeeId" } }
                            }
                        ]
                    }
                }""")
            }
        }
    }
}
