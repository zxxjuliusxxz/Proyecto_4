package com.nttdata.pe.Customer.Controllers;

import com.nttdata.pe.Customer.Application.Services.Customer.ICustomerService;
import com.nttdata.pe.Customer.Domain.Customer;
import com.nttdata.pe.Customer.Infraestructure.Redis.IRedisRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter(AccessLevel.PROTECTED)
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController implements IRedisRepository {

    private static final String KEY = "bankAccount";
    private RedisTemplate<String, Customer> redisTemplate;
    private HashOperations hashOperations;

    @Autowired
    private ICustomerService customerService;

    @GetMapping(value = "/getAll")
    public ResponseEntity<Flux<Customer>> getAllCustomer() {
        return ResponseEntity.ok(customerService.list());
    }

    @GetMapping(value = "/getById/{id}")
    public Mono<ResponseEntity<Customer>> getByIdCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(accountBank -> ResponseEntity.ok(accountBank))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/insert")
    public Mono<ResponseEntity<Customer>> addCustomer(@RequestBody Customer customer) {
        return customerService.register(customer)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @PutMapping(value = "/update/{id}")
    public Mono<ResponseEntity<Customer>> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.updater(id, customer)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/delete/{id}")
    public Mono<ResponseEntity<Void>> deleteByIdCustomer(@PathVariable Long id) {
        return customerService.delete(id)
                .map(accountBank -> ResponseEntity.ok(accountBank))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Map<String, Customer> findAll() {
        return hashOperations.entries(KEY);
    }

    @Override
    public Customer findById(String id) {
        return (Customer)hashOperations.get(KEY, id);
    }

    @Override
    public void save(Customer customer) {
        hashOperations.put(KEY, UUID.randomUUID().toString(), customer);
    }

    @Override
    public void delete(String id) {
        hashOperations.delete(KEY, UUID.randomUUID().toString(), id);
    }

}