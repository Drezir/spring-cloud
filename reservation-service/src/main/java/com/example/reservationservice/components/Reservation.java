package com.example.reservationservice.components;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Document
public class Reservation {

    @Id
    private UUID id = UUID.randomUUID();
    private String name;

    @LastModifiedDate
    private Instant update;

    public Reservation(String name) {
        this.name = name;
    }
}
