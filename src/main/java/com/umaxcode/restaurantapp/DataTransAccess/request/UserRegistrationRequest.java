package com.umaxcode.restaurantapp.DataTransAccess.request;

public record UserRegistrationRequest(
        String firstname, String lastname, String email, String password, String conPassword
) {
}
