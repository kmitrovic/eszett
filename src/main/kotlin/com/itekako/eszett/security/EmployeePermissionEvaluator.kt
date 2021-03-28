package com.itekako.eszett.security

import com.itekako.eszett.model.Employee
import com.itekako.eszett.repository.EmployeeRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.io.Serializable

@Service
class EmployeePermissionEvaluator(val employeeRepository: EmployeeRepository) : PermissionEvaluator {

    override fun hasPermission(auth: Authentication?, employee: Any?, permission: Any?): Boolean {
        auth?: return false
        employee?: return false
        permission?: return false

        if (!auth.isAuthenticated || employee !is Employee
                || permission !in listOf("findAll", "findById", "save", "delete")) {
            return false
        }

        val authorities = auth.authorities.map { it.authority }.toList()

        if (authorities.contains("ROLE_SUPERUSER")) return true

        if (!authorities.contains("ROLE_ADMIN")) return false

        if (employee.company.id != (auth.principal as EmployeePrincipal).getCompanyId()) return false

        if (permission != "save" || employee.id == 0L) return true

        // else, check if the update doesn't include update of the company that contains the employee entry
        return employeeRepository.findCompanyIdById(employee.id) == employee.company.id
    }

    override fun hasPermission(a: Authentication?, targetId: Serializable?, targetType: String?, p: Any?): Boolean {
        throw NotImplementedError()  // intentionally - not used, at least for now
    }
}
