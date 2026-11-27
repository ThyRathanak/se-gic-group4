package edu.itc.shop.web;

import edu.itc.shop.domain.Role;
import edu.itc.shop.domain.UserAccount;
import edu.itc.shop.service.AccountService;
import edu.itc.shop.web.dto.RegisterRequest;
import edu.itc.shop.web.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final AccountService accounts;

    public RegisterController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        UserAccount account = accounts.register(request.username(), request.password(), Set.of(Role.CUSTOMER));
        return new RegisterResponse(account.getUsername());
    }
}
