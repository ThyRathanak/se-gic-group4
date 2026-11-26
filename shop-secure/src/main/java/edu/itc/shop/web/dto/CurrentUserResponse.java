package edu.itc.shop.web.dto;

import java.util.List;

public record CurrentUserResponse(String name, List<String> roles) {
}
