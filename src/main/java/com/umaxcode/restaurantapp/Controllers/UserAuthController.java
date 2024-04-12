package com.umaxcode.restaurantapp.Controllers;

import com.umaxcode.restaurantapp.DataTransAccess.request.UserAuthenticationRequest;
import com.umaxcode.restaurantapp.DataTransAccess.request.UserRegistrationRequest;
import com.umaxcode.restaurantapp.DataTransAccess.response.UserRegAuthResponse;
import com.umaxcode.restaurantapp.Services.UserAuthService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/authentication")
@Data
@RestController
public class UserAuthController {

   private final UserAuthService userCreation;

    @PostMapping("/create")
    public ResponseEntity<UserRegAuthResponse> register(
            @RequestBody UserRegistrationRequest userData
            ){

          try {
              UserRegAuthResponse message = this.userCreation.create_user(userData);
              return ResponseEntity.ok(message);

          } catch (Exception e){
              return ResponseEntity.status(400).body(new UserRegAuthResponse(e.getMessage(), null));
          }
    }

    @PostMapping("/login")
    public ResponseEntity<UserRegAuthResponse> login(
            @RequestBody UserAuthenticationRequest userData
            ){
           try {
               UserRegAuthResponse message = this.userCreation.login_user(userData);
               return ResponseEntity.ok(message);
           }catch (Exception e){
               return ResponseEntity.status(400).body(new UserRegAuthResponse(e.getMessage(), null));
           }

    }

    @GetMapping("/home")
    public ResponseEntity<String> homePage(){


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(403).body("Invalid token");
            }

            String email = authentication.getName();

            return ResponseEntity.ok(String.format("Welcome back %s", email));

        }

    }


