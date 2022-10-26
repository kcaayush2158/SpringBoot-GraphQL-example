package com.application.api.controller;

import com.application.dao.PersonRepository;
import com.application.entity.Person;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;


@RestController
public class PersonController {
    @Autowired
    private PersonRepository repository;
    @Value("classpath:person.graphqls")
    private Resource resource;
    private GraphQL graphQL;


    // Parsing Schema
    @PostConstruct
    public void loadSchema() throws IOException {
        File schemaFile = resource.getFile();
        TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);
        RuntimeWiring wiring = buildWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    private RuntimeWiring buildWiring() {
        DataFetcher<List<Person>> fetcher = data -> {
            return (List<Person>) repository.findAll();
        };

        DataFetcher<Person> findByEmail = data -> {
            return repository.findByEmail(data.getArgument("email"));
        };

        return RuntimeWiring.newRuntimeWiring().type("Query",
                typeWriting -> typeWriting.dataFetcher("getAllPerson", fetcher)
                        .dataFetcher("findPerson", findByEmail)).build();
    }


    @PostMapping("/addPerson")
    public String addPerson(List<Person> persons) {
        repository.save(persons.get(0));
        return "record inserted" + persons.size();
    }

    @GetMapping("/findAllPerson")
    public List<Person> getPersons() {
        return (List<Person>) this.repository.findAll();
    }


    @PostMapping("/getAll")
    public ResponseEntity<Object> addPerson(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @PostMapping("/getPersonByEmail")
    public ResponseEntity<Object> getPersonByEmail(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

}
