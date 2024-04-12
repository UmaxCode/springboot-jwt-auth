package com.umaxcode.restaurantapp.DataTransAccess.request;

public record UserAuthenticationRequest(
        String email, String password
) {
}
