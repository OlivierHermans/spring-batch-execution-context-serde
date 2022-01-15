package com.example.springbatchexecutioncontextserde;

import lombok.Value;

import java.time.LocalDate;

@Value
public class Person {

    String firstName;
    String lastName;
    LocalDate birthDate;
}
