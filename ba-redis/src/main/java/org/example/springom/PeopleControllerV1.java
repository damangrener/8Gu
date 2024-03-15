package org.example.springom;


import org.example.springom.repository.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/people")
public class PeopleControllerV1 {
    @Autowired
    PeopleRepository repo;

    @GetMapping("all")
    Iterable<Person> all() {
        return repo.findAll();
    }

    @GetMapping("{id}")
    Optional<Person> byId(@PathVariable String id) {
        return repo.findById(id);
    }

    @GetMapping("age_between")
    Iterable<Person> byAgeBetween( //
                                   @RequestParam("min") int min, //
                                   @RequestParam("max") int max) {
        return repo.findByAgeBetween(min, max);
    }
}