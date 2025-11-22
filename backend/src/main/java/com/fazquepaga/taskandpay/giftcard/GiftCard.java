package com.fazquepaga.taskandpay.giftcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Represents a Gift Card available for purchase.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftCard {
    private String id;
    private String name;
    private String brand; // e.g., "Roblox", "iFood", "Uber"
    private BigDecimal value;
    private String imageUrl;
    private String description;
}
