package com.itekako.eszett

import com.itekako.eszett.controller.CompanyController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest
class EszettApplicationTests(@Autowired val controller: CompanyController) {

	@Test
	fun `context loads`() = assertNotNull(controller)

}
