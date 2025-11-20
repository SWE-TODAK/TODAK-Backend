package com.todak.api.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/test")
public class TestController {

    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @GetMapping("/db")
    public String testDB() {
        try {
            long count = testRepository.count(); // DB에 실제 쿼리 날림
            return "DB 연결 성공! 데이터 개수: " + count;
        } catch (Exception e) {
            return "DB 연결 실패: " + e.getMessage();
        }
    }
}

