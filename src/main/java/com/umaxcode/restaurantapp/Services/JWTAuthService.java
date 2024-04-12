package com.umaxcode.restaurantapp.Services;


import com.umaxcode.restaurantapp.Entities.Token;
import com.umaxcode.restaurantapp.Repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTAuthService {


    private final TokenRepository tokenRepository;

    @Value("${spring.security.token.secretKey}")
    private String jwtSecret;

    @Value("${spring.security.token.expiredTime.int}")
    private int jwtExpirationTime;

    public String extractUserEmail(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + this.jwtExpirationTime))
                .signWith(getSignInKey())
                .compact();
    }


    public boolean isValidToken(String token, UserDetails userDetails) {

        var DBToken = this.tokenRepository.findByToken(token).orElse(null);
        if(DBToken == null)
           return false;

        if(isTokenExpired(token) || isTokenRevoked(DBToken)){
            DBToken.setIsExpired(true);
            this.tokenRepository.save(DBToken);

            return false;
        }

        return extractUserEmail(token).equals(userDetails.getUsername());
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private boolean isTokenRevoked(Token token){

        return Objects.requireNonNull(this.tokenRepository.findByToken(token.getToken()).orElseThrow(IllegalArgumentException::new)).getIsRevoked();
    }

    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)  {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception e){
            throw new IllegalArgumentException();
        }
    }
}
