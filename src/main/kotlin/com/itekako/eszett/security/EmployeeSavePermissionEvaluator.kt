package com.itekako.eszett.security

import com.itekako.eszett.model.Employee
import com.itekako.eszett.repository.EmployeeRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.io.Serializable

@Service
class EmployeeSavePermissionEvaluator(val employeeRepository: EmployeeRepository) : PermissionEvaluator {

    override fun hasPermission(auth: Authentication, entity: Any?, permission: Any?): Boolean {
        if (permission != "save") return false

        if (!auth.isAuthenticated) return false

        val authorities = auth.authorities.map { it.authority }.toList()
        if (authorities.contains("ROLE_SUPERUSER")) return true
        if (!authorities.contains("ROLE_ADMIN")) return false

        if (entity !is Employee) return true
        if (entity.company.id != (auth.principal as EmployeePrincipal).getCompanyId()) return false

        if (entity.id == 0L) return true
        return employeeRepository.findCompanyIdById(entity.id) == entity.company.id
    }

    override fun hasPermission(a: Authentication?, targetId: Serializable?, targetType: String?, p: Any?): Boolean {
        TODO("Not yet implemented - not needed")
    }

}