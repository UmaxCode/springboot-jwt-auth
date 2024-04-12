package com.umaxcode.restaurantapp.Mapper;

import com.umaxcode.restaurantapp.Entities.User;
import com.umaxcode.restaurantapp.Entities.userDetail.UserDetailsImp;
import org.springframework.security.core.userdetails.UserDetails;

public class UserMapper {

    public static UserDetails toUserDetails(User user) {

        return UserDetailsImp.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();


    }
}
