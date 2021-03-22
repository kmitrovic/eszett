package com.itekako.eszett.repository

import com.itekako.eszett.model.Employee
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeRepository : PagingAndSortingRepository<Employee, Long>