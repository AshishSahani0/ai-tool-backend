package com.example.backend.user.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
@CompoundIndex(name = "email_idx", def = "{'email': 1}", unique = true)
public class User {

    @Id
    private String id;

    private String firebaseUid;

    @Indexed(unique = true)
    private String email;
    private String name;

    @Builder.Default
    private Role role = Role.USER;
}