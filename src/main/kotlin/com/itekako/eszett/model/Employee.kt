package com.itekako.eszett.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.itekako.eszett.security.Config
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Table(indexes = [ Index(name = "employee_username_key", columnList = "username", unique = true) ])
@Entity
class Employee(@SequenceGenerator(name = "employee_id_seq", sequenceName = "employee_id_seq", allocationSize = 1)
               @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_id_seq")
               @Id val id: Long = 0,

               @Column(nullable = false, length = 255)
               @get: NotBlank(message="{name.required}")
               @get: Size(max=255, message="{name.long}")
               val name: String = "",

               @Column(nullable = false, length = 255)
               @get: NotBlank(message="{surname.required}")
               @get: Size(max=255, message="{surname.long}")
               val surname: String = "",

               @Column(nullable = false, length = 320)
               @get: NotBlank(message="{email.required}")
               @get: Size(max=320, message="{email.long}")
               @get: Email(message="{email.invalid}")
               val email: String = "",

               @Column(nullable = false)
               @get: NotNull(message="{salary.required}")
               val salary: Double,

               @Column(length = 500)
               @get: Size(max=500, message="{address.long}")
               val address: String = "",

               // Set username and password only for company admins
               @Column(length = 60)
               @get: Size(max=60, message="{username.long}")
               val username: String? = null,

               @Column(length = 60)
               @JsonProperty(access=JsonProperty.Access.WRITE_ONLY)
               @set: JsonDeserialize(using = Config.PasswordDeserializer::class)
               var password: String? = null,

               @JsonIgnore
               @Column
               val isSuperuser: Boolean = false,

               @ManyToOne(optional = false)
               @JoinColumn(name = "company_id", nullable = false)
               val company: Company)
