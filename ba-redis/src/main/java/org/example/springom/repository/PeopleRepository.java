package org.example.springom.repository;

import com.redis.om.spring.repository.RedisDocumentRepository;
import org.example.springom.Person;


public interface PeopleRepository extends RedisDocumentRepository<Person,String> {

    // Find people by age range
    Iterable<Person> findByAgeBetween(int minAge, int maxAge);

}