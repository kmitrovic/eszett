package com.itekako.eszett.model

import org.springframework.data.annotation.Id
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class Employee(@Id val id: Long,
                    @get: NotBlank(message="{name.required}") val name: String,
                    @get: NotBlank(message="{surname.required}") val surname: String,
                    @get: NotBlank(message="{email.required}") @get: Email(message="{email.invalid}") val email: String,
                    @get: NotBlank(message="{salary.required}") val salary: Double,
                    val address: String = "")
