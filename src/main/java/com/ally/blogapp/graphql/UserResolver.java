package com.ally.blogapp.graphql;

import com.ally.blogapp.model.User;
import com.ally.blogapp.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserResolver {

    private final UserService userService;

    public UserResolver(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.findById(id);
    }

    @QueryMapping
    public List<User> users() {
        return userService.findAll();
    }

    @MutationMapping
    public User createUser(@Argument String username, @Argument String email,
                           @Argument String password, @Argument String role) {
        return userService.register(username, email, password, role);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument String bio,
                           @Argument String profileImage) {
        return userService.update(id, bio, profileImage);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.delete(id);
        return true;
    }
}
