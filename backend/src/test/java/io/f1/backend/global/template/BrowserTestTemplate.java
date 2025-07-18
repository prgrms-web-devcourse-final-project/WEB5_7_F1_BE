package io.f1.backend.global.template;


import com.github.database.rider.spring.api.DBRider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@DBRider
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BrowserTestTemplate {
	@Autowired
	protected MockMvc mockMvc;
}
