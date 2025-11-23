package com.fazquepaga.taskandpay.identity;

import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class IdentityService {

    private final UserRepository userRepository;

    private final Map<String, String> onboardingCodes = new ConcurrentHashMap<>(); // code -> childId

    public IdentityService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public String generateOnboardingCode(String childId) {

        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        onboardingCodes.put(code, childId);

        // In a real application, you'd add a TTL to this code

        return code;
    }

    public User completeOnboarding(String code, String phoneNumber)
            throws ExecutionException, InterruptedException {

        String childId = onboardingCodes.get(code);

        if (childId == null) {

            throw new IllegalArgumentException("Invalid onboarding code.");
        }

        User child = userRepository.findByIdSync(childId);

        if (child == null) {

            throw new IllegalStateException("Child not found for onboarding code.");
        }

        child.setPhoneNumber(phoneNumber);

        userRepository.save(child).get();

        onboardingCodes.remove(code);

        return child;
    }

    public User registerParent(CreateParentRequest request)
            throws ExecutionException, InterruptedException {

        User parent = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(User.Role.PARENT)
                .subscriptionTier(User.SubscriptionTier.FREE) // Default to FREE tier
                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                .build();

        userRepository.save(parent).get();

        return parent;
    }

    public User createChild(CreateChildRequest request)
            throws ExecutionException, InterruptedException {

        String parentId = request.getParentId();

        if (parentId == null || parentId.isEmpty()) {

            throw new IllegalArgumentException("Parent ID is required to create a child.");
        }

        // Ensure parent exists

        User parent = userRepository.findByIdSync(parentId);

        if (parent == null || parent.getRole() != User.Role.PARENT) {

            throw new IllegalArgumentException("Parent with ID " + parentId + " not found.");
        }

        User child = User.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.CHILD)
                .parentId(parentId)
                .build();

        userRepository.save(child).get();

        return child;
    }

    public User getChild(String childId, String parentId)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null || child.getRole() != User.Role.CHILD) {
            throw new IllegalArgumentException("Child not found");
        }
        if (!child.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Child does not belong to this parent");
        }
        return child;
    }

    public User updateChildAllowance(String childId, java.math.BigDecimal allowance)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }
        child.setMonthlyAllowance(allowance);
        userRepository.save(child).get();
        return child;
    }
}
