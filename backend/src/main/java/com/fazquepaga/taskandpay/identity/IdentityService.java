package com.fazquepaga.taskandpay.identity;

import com.fazquepaga.taskandpay.identity.dto.ChildLoginRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest;
import com.google.api.core.ApiFuture;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    // In-memory storage for onboarding codes: code -> childId
    private final ConcurrentHashMap<String, String> onboardingCodes = new ConcurrentHashMap<>();

    public IdentityService(UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateOnboardingCode(String childId) {
        // Reuse existing logic or simple random
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        onboardingCodes.put(code, childId);
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

    // Authenticate Child by Access Code (Permanent)
    public User authenticateChildByCode(String code)
            throws ExecutionException, InterruptedException {

        // Find child by access code
        // We need a query for this.
        // Option A: Update UserRepository to findByAccessCode.
        // Option B: For now, if code is 6 chars, maybe we can fetch all children and
        // filter? (Bad performance)
        // Option C: Add findByAccessCode to UserRepository.

        // Let's add findByAccessCode to UserRepository while we're at it.
        // But for now, let's assume specific query.

        // Note: The previous implementation used `onboardingCodes` map which is
        // ephemeral.
        // We want PERSISTENT access code login now.

        // Actually, let's check if the code exists in `onboardingCodes` (legacy flow)
        // OR database (new flow).
        // The user wants PERMANENT access code.

        // Implementation:
        // 1. Check if we can find by accessCode in DB.

        User child = userRepository.findByAccessCode(code).get();

        if (child == null) {
            // Fallback to legacy onboarding for transition if needed, but per plan "Child
            // Access Code Strategy", it's primary.
            throw new IllegalArgumentException("Invalid access code.");
        }

        if (child.getRole() != User.Role.CHILD) {
            throw new IllegalArgumentException("Invalid user type.");
        }

        return child;
    }

    public User registerParent(CreateParentRequest request)
            throws ExecutionException, InterruptedException {

        // Validate uniqueness of email and phone
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use.");
        }
        if (request.getPhoneNumber() != null && userRepository.findByPhoneNumber(request.getPhoneNumber()) != null) {
            throw new IllegalArgumentException("Phone number already in use.");
        }

        User parent = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber()) // Save phone
                .password(passwordEncoder.encode(request.getPassword())) // Save hashed password
                .role(User.Role.PARENT)
                .subscriptionTier(User.SubscriptionTier.FREE)
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

        User parent = userRepository.findByIdSync(parentId);

        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("Parent with ID " + parentId + " not found.");
        }

        // Generate Unique Access Code
        String accessCode;
        do {
            accessCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (userRepository.findByAccessCode(accessCode).isPresent()); // Ensure uniqueness

        User child = User.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .age(request.getAge())
                .role(User.Role.CHILD)
                .parentId(parentId)
                .accessCode(accessCode) // Save access code
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

    public List<User> getChildren(String parentId) throws ExecutionException, InterruptedException {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = userRepository.findByParentId(parentId).get()
                .getDocuments();
        return documents.stream().map(doc -> doc.toObject(User.class)).collect(Collectors.toList());
    }

    public User updateChildAllowance(String childId, java.math.BigDecimal allowance)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }
        child.setMonthlyAllowance(allowance);
        userRepository.save(child).get();

        // NOVO: Recalcular valores das tarefas quando mesada mudar
        // Note: Precisamos injetar AllowanceService - vou adicionar no constructor

        return child;
    }

    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        User user = userRepository.findByIdSync(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    public User updateChild(String childId, UpdateChildRequest request, String parentId)
            throws ExecutionException, InterruptedException {
        // SECURITY: Validate child exists and belongs to parent
        User child = getChild(childId, parentId);

        // Update only the provided fields (partial update)
        if (request.getName() != null && !request.getName().isEmpty()) {
            child.setName(request.getName());
        }
        if (request.getAge() != null) {
            child.setAge(request.getAge());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            child.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(child).get();
        return child;
    }

    public User authenticateParent(String email, String password) throws ExecutionException, InterruptedException {
        User user = userRepository.findByEmail(email);
        if (user == null || user.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        return user;
    }

    public void deleteChild(String childId, String parentId)
            throws ExecutionException, InterruptedException {
        // SECURITY: Validate child belongs to parent before deleting
        User child = getChild(childId, parentId);

        // Delete the child from database
        userRepository.delete(childId).get();

        // Note: Tasks associated with this child will remain in the database
        // In a production app, you would want to either:
        // 1. Cascade delete tasks
        // 2. Archive the child instead of hard delete
        // For this implementation, we'll do a simple delete
    }
}
