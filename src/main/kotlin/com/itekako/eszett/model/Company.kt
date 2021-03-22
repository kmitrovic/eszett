package com.itekako.eszett.model

import org.springframework.data.annotation.Id
import javax.validation.constraints.NotBlank

data class Company(@Id val id: Long,
                   @get: NotBlank(message="{name.required}") val name: String,
                   var employees: MutableSet<Employee> = HashSet())
