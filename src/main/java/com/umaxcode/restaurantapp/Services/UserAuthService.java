package com.umaxcode.restaurantapp.Services;


import com.umaxcode.restaurantapp.DataTransAccess.request.UserAuthenticationRequest;
import com.umaxcode.restaurantapp.DataTransAccess.request.UserRegistrationRequest;
import com.umaxcode.restaurantapp.DataTransAccess.response.UserRegAuthResponse;
import com.umaxcode.restaurantapp.Entities.Role;
import com.umaxcode.restaurantapp.Entities.Token;
import com.umaxcode.restaurantapp.Entities.TokenType;
import com.umaxcode.restaurantapp.Entities.User;
import com.umaxcode.restaurantapp.Repositories.TokenRepository;
import com.umaxcode.restaurantapp.Repositories.UserRepository;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Data
@Service
public class UserAuthService {


    private static int unique = 1;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTAuthService jwtAuthService;
    private final TokenRepository tokenRepository;

    public UserRegAuthResponse create_user(UserRegistrationRequest userData) throws IllegalArgumentException {

        // check whether the user already exist
        var user = this.userRepository.findByEmail(userData.email());
        if (user.isPresent()) {
            throw new IllegalArgumentException("User already exist");
        }

        // check for matching password
        if (!userData.password().equals(userData.conPassword())) {
            throw new IllegalArgumentException("the passwords don't match");
        }

        // encode password
        var hashedPassword = this.passwordEncoder.encode(userData.password());

        // generate a unique username
        var createdUsername = userData.firstname().charAt(0) + userData.lastname().substring(0, 1) + (unique++);


        User newUser = User.builder()
                .firstname(userData.firstname())
                .lastname(userData.lastname())
                .username((createdUsername))
                .email(userData.email())
                .role(Role.User)
                .password(hashedPassword)
                .build();

        User createdUser = this.userRepository.save(newUser);

        String authToken = jwtAuthService.generateToken(createdUser.getEmail());

        saveGeneratedToken(createdUser, authToken);

        return new UserRegAuthResponse("User was created successfully", authToken);

    }

    private void saveGeneratedToken(User user, String generatedToken) {
        var token = Token.builder()
                .user(user)
                .token(generatedToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .build();

        this.tokenRepository.save(token);
    }

    private void revokeAllValidTokens(User user){

        var userValidTokens = this.tokenRepository.findAllValidTokenByUserId(user.getUserId());

        if(userValidTokens.isEmpty())
            return;

        userValidTokens.forEach(token -> {
            token.setIsRevoked(true);
            token.setIsExpired(true);
        });

        this.tokenRepository.saveAll(userValidTokens);
    }

    public UserRegAuthResponse login_user(
            @RequestBody UserAuthenticationRequest userData
    ) {


        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userData.email(),
                            userData.password()
                    )
            );

            String authToken = this.jwtAuthService.generateToken(userData.email());

            var optionalUser = this.userRepository.findByEmail(userData.email());

            if(optionalUser.isEmpty())
                return null;


            var user = optionalUser.get();

            revokeAllValidTokens(user);
            saveGeneratedToken(user, authToken);

            return new UserRegAuthResponse("User is logged in successfully", authToken);
        } catch (Exception e){
             throw new IllegalArgumentException("Invalid email or password");
        }


    }
}
