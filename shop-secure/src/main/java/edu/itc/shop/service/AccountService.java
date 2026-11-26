package edu.itc.shop.service;

import edu.itc.shop.domain.Role;
import edu.itc.shop.domain.UserAccount;
import edu.itc.shop.repository.UserAccountRepository;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
	private final UserAccountRepository users;
	private final PasswordEncoder encoder;

	public AccountService(UserAccountRepository users, PasswordEncoder encoder) {
		this.users = users;
		this.encoder = encoder;
	}

	@Transactional
	public UserAccount register(String username, String rawPassword, Set<Role> roles) {
		if (users.existsByUsername(username)) {
			throw new UsernameTakenException(username);
		}
		return users.save(new UserAccount(username, encoder.encode(rawPassword), true, roles));
	}

	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return users.existsByUsername(username);
	}
}
