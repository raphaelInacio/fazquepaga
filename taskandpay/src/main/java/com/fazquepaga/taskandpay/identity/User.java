package com.fazquepaga.taskandpay.identity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @DocumentId
    private String id;

    private String name;
    private String email; // Used for parents
    private Role role;
    private String parentId; // Links a child to a parent
    private String phoneNumber; // Used for children for WhatsApp identification
    private java.math.BigDecimal monthlyAllowance;

    public enum Role {
        PARENT,
        CHILD
    }
}