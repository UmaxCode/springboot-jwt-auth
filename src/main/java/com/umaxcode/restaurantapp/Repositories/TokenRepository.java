package com.umaxcode.restaurantapp.Repositories;

import com.umaxcode.restaurantapp.Entities.Token;
import com.umaxcode.restaurantapp.Entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Integer> {

   @Query(
       value = "select t from Token t inner join User u on t.user.id = u.userId where u.userId = :userId and (t.isExpired = false or t.isRevoked = false)"
   )
    List<Token> findAllValidTokenByUserId(Integer userId);

    Optional<Token> findByToken(String token);
}
