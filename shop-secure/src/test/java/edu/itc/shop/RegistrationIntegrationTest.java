package edu.itc.shop;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.itc.shop.domain.UserAccount;
import edu.itc.shop.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationIntegrationTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserAccountRepository users;

	@Autowired
	private PasswordEncoder encoder;

	@Test
	void developmentAdminSeedHasBcryptHash() {
		UserAccount admin = users.findByUsername("admin").orElseThrow();
		assertTrue(admin.getPasswordHash().startsWith("{bcrypt}$2"));
		assertFalse(admin.toString().contains(admin.getPasswordHash()));
	}

	@Test
	void registrationStoresBcryptAndEnablesHttpBasic() throws Exception {
		String username = "secure_user_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		String password = "Correct-Horse-9";

		mvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.password").doesNotExist());

		UserAccount saved = users.findByUsername(username).orElseThrow();
		assertTrue(saved.getPasswordHash().startsWith("{bcrypt}$2"));
		assertTrue(encoder.matches(password, saved.getPasswordHash()));

		mvc.perform(get("/api/me").with(SecurityMockMvcRequestPostProcessors.httpBasic(username, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(username))
				.andExpect(jsonPath("$.roles", hasItem("ROLE_CUSTOMER")));
	}

	@Test
	void invalidRequestReturnsProblemJsonWithFieldErrors() throws Exception {
		mvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"bob","password":"short"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[*].field", hasItem("password")))
				.andExpect(jsonPath("$.errors[*].message", hasItems(
						"size must be between 12 and 72",
						"must contain an upper-case letter, a lower-case letter and a digit")));
	}

	@Test
	void duplicateUsernameReturnsConflict() throws Exception {
		String username = "duplicate_user_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		String request = "{\"username\":\"" + username + "\",\"password\":\"Correct-Horse-9\"}";

		mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isCreated());
		mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isConflict());
	}
}
