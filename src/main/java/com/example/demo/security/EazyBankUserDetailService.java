package com.example.demo.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.repo.CustomerRepository;

@Service
public class EazyBankUserDetailService implements UserDetailsService{

    @Autowired
    CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Custom UserDetailService called buddddddddddddddddddddddddddddddddddddddyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        Customer customer = customerRepository.findByEmail(username).orElseThrow(()->{
            throw new UsernameNotFoundException("User with given email id not present in the DB");
        });
        List<GrantedAuthority> authorities = customer.getAuthorities().stream().map(auth -> new SimpleGrantedAuthority(auth.getName())).collect(Collectors.toList());
        return new User(customer.getEmail(), customer.getPwd(), authorities);
    }
    
}
