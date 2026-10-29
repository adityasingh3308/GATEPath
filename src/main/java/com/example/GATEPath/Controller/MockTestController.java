
package com.example.GATEPath.Controller;

import com.example.GATEPath.Entity.MockTest;
import com.example.GATEPath.Entity.MockTestAttempt;
import com.example.GATEPath.Repository.MockTestRepository;
import com.example.GATEPath.Service.MockTestService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mock-tests")
public class MockTestController {

    private final MockTestRepository mockTestRepository;
    private final MockTestService mockTestService;

    public MockTestController(
            MockTestRepository mockTestRepository,
            MockTestService mockTestService
    ) {
        this.mockTestRepository = mockTestRepository;
        this.mockTestService = mockTestService;
    }

    @GetMapping
    public String showMockTests(Model model) {
        List<MockTest> mockTests =
                mockTestRepository.findByActiveTrue();

        model.addAttribute("mockTests", mockTests);

        return "mock-tests";
    }

    @GetMapping("/{mockTestId}/instructions")
    public String showInstructions(
            @PathVariable Long mockTestId,
            Model model
    ) {
        MockTest mockTest = mockTestRepository.findById(mockTestId)
                .filter(test -> Boolean.TRUE.equals(test.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mock test not found."
                        ));

        model.addAttribute("mockTest", mockTest);

        return "instructions";
    }

    @PostMapping("/{mockTestId}/start")
    public String startMockTest(
            @PathVariable Long mockTestId,
            Authentication authentication
    ) {
        MockTestAttempt attempt =
                mockTestService.startOrResumeMockTest(
                        mockTestId,
                        authentication.getName()
                );

        return "redirect:/mock-tests/attempt/" + attempt.getId();
    }
}
