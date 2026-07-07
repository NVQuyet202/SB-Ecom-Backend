package com.ecommerce.sb_ecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must at least 5 characters")
    private String building;

    @NotBlank
    @Size(min = 5, message = "City name must at least 5 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State name must at least 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must at least 2 characters")
    private String country;

    @NotBlank
    @Size(min = 6, message = "Pincode must at least 6 characters")
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();
}
