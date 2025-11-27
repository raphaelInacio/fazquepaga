package com.fazquepaga.taskandpay.ai;

import org.springframework.stereotype.Service;

@Service
public class AiInsightServiceImpl implements AiInsightService {

    @Override
    public String getInsights(String childId) {
        // TODO: Implement the actual AI logic here.
        // For now, return a mocked response.
        return "We noticed that your child is saving a lot! Keep up the good work!";
    }
}
