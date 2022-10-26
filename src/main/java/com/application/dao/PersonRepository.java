package com.application.dao;
import org.springframework.data.repository.CrudRepository;

import com.application.entity.Person;
import org.springframework.stereotype.Repository;

public interface PersonRepository extends CrudRepository<Person,Integer> {

    Person findByEmail(String email);

}
