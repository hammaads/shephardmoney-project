package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;


    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response

        // Create a new User entity from the payload
        User userToCreate = new User();
        userToCreate.setName(payload.getName());
        userToCreate.setEmail(payload.getEmail());

        // Store the user in the database
        User createdUser = this.userRepository.save(userToCreate);

        // Return the ID of the created user with 200 OK response
        return ResponseEntity.ok(createdUser.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        Optional<User> userToDeleteOptional =  this.userRepository.findById(userId);
        if (userToDeleteOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User with ID " + userId + " not found.");
        }

        User userToDelete = userToDeleteOptional.get();
        this.userRepository.delete(userToDelete);
        return ResponseEntity.ok("User with ID " + userId + " deleted successfully.");
    }
}
