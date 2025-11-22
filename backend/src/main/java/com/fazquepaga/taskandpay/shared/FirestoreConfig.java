package com.fazquepaga.taskandpay.shared;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirestoreConfig {

    @Bean
    public Firestore firestore(FirestoreOptions firestoreOptions) {
        return firestoreOptions.getService();
    }
}
