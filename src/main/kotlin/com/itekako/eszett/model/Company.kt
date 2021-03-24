package com.itekako.eszett.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
import javax.validation.constraints.NotBlank


// Just use name as unique for company here as we do not have any other unique registration number
@Table(indexes = [ Index(name = "company_name_key", columnList = "name", unique = true) ])
@Entity
class Company(@SequenceGenerator(name = "company_id_seq", sequenceName = "company_id_seq", allocationSize = 1)
              @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
              @Id val id: Long = 0,

              @Column(unique = true, nullable = false, length = 500)
              @get: NotBlank(message="{name.required}")
              val name: String = "",

              @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = [ CascadeType.ALL ],
                         orphanRemoval = true)
              var employees: MutableSet<Employee> = HashSet()) {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    fun getAvgSalary(): Double = employees.asSequence().map { it.salary }.average()
}
