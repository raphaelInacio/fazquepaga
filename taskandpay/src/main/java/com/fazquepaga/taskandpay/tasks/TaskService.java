package com.fazquepaga.taskandpay.tasks;

import com.fazquepaga.taskandpay.identity.User;

import com.fazquepaga.taskandpay.identity.UserRepository;

import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;

import com.google.cloud.firestore.QueryDocumentSnapshot;

import org.springframework.stereotype.Service;



import java.time.Instant;

import java.util.List;

import java.util.concurrent.ExecutionException;

import java.util.stream.Collectors;



@Service

public class TaskService {



    private final TaskRepository taskRepository;

    private final UserRepository userRepository;





    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {

        this.taskRepository = taskRepository;

        this.userRepository = userRepository;

    }



    public Task createTask(String userId, CreateTaskRequest request) throws ExecutionException, InterruptedException {

        User child = userRepository.findByIdSync(userId);

        if (child == null || child.getRole() != User.Role.CHILD) {

            throw new IllegalArgumentException("Child with ID " + userId + " not found.");

        }



        Task task = Task.builder()

                .description(request.getDescription())

                .type(request.getType())

                .weight(request.getWeight())

                .requiresProof(request.isRequiresProof())

                .createdAt(Instant.now())
                .dayOfWeek(request.getDayOfWeek())
                .scheduledDate(request.getScheduledDate())
                .status(Task.TaskStatus.PENDING)

                .build();

        taskRepository.save(userId, task).get();

        return task;

    }



    public List<Task> getTasksByUserId(String userId) throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documents = taskRepository.findTasksByUserId(userId).get().getDocuments();

        return documents.stream()

                .map(doc -> doc.toObject(Task.class))

                .collect(Collectors.toList());

    }

}


