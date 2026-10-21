
package com.example.GATEPath.Controller;

import com.example.GATEPath.Entity.SyllabusSubject;
import com.example.GATEPath.Entity.SyllabusTopic;
import com.example.GATEPath.Entity.User;
import com.example.GATEPath.Entity.UserTopicProgress;
import com.example.GATEPath.Repository.SyllabusSubjectRepository;
import com.example.GATEPath.Repository.SyllabusTopicRepository;
import com.example.GATEPath.Repository.UserRepository;
import com.example.GATEPath.Repository.UserTopicProgressRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class MyProgressController {

    private final UserRepository userRepository;
    private final SyllabusSubjectRepository subjectRepository;
    private final SyllabusTopicRepository topicRepository;
    private final UserTopicProgressRepository progressRepository;

    public MyProgressController(
            UserRepository userRepository,
            SyllabusSubjectRepository subjectRepository,
            SyllabusTopicRepository topicRepository,
            UserTopicProgressRepository progressRepository) {

        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.progressRepository = progressRepository;
    }

    @GetMapping("/my-progress")
    public String showProgress(
            Authentication authentication,
            Model model) {

        User user = getLoggedInUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        List<SyllabusSubject> subjects =
                subjectRepository
                        .findByBranchOrderByDisplayOrderAsc("CS");

        List<UserTopicProgress> records =
                progressRepository.findByUser_Id(user.getId());

        Set<Long> completedTopicIds = new HashSet<>();

        for (UserTopicProgress record : records) {
            if (record.isCompleted()) {
                completedTopicIds.add(record.getTopic().getId());
            }
        }

        List<SubjectProgressView> subjectViews =
                new ArrayList<>();

        int totalTopics = 0;
        int completedTopics = 0;

        for (SyllabusSubject subject : subjects) {

            List<SyllabusTopic> topics =
                    topicRepository
                            .findBySubject_IdOrderByDisplayOrderAsc(
                                    subject.getId());

            List<TopicProgressView> topicViews =
                    new ArrayList<>();

            int subjectCompleted = 0;

            for (SyllabusTopic topic : topics) {

                boolean completed =
                        completedTopicIds.contains(topic.getId());

                if (completed) {
                    subjectCompleted++;
                }

                topicViews.add(
                        new TopicProgressView(topic, completed));
            }

            int subjectTotal = topics.size();

            int percentage = subjectTotal == 0
                    ? 0
                    : (int) Math.round(
                            subjectCompleted * 100.0 / subjectTotal);

            subjectViews.add(
                    new SubjectProgressView(
                            subject,
                            topicViews,
                            subjectCompleted,
                            subjectTotal,
                            percentage));

            totalTopics += subjectTotal;
            completedTopics += subjectCompleted;
        }

        int overallPercentage = totalTopics == 0
                ? 0
                : (int) Math.round(
                        completedTopics * 100.0 / totalTopics);

        model.addAttribute("studentName", user.getFullName());
        model.addAttribute("subjectProgress", subjectViews);
        model.addAttribute("totalTopics", totalTopics);
        model.addAttribute("completedTopics", completedTopics);
        model.addAttribute("remainingTopics",
                totalTopics - completedTopics);
        model.addAttribute("overallPercentage", overallPercentage);

        return "progress/my-progress";
    }

    @PostMapping("/my-progress/topic/{topicId}/toggle")
    public String toggleTopic(
            @PathVariable Long topicId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        Optional<SyllabusTopic> topicOptional =
                topicRepository.findById(topicId);

        if (topicOptional.isEmpty()
                || topicOptional.get().getSubject() == null
                || !"CS".equals(
                        topicOptional.get().getSubject().getBranch())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Invalid syllabus topic.");

            return "redirect:/my-progress";
        }

        SyllabusTopic topic = topicOptional.get();

        UserTopicProgress progress =
                progressRepository
                        .findByUser_IdAndTopic_Id(
                                user.getId(), topicId)
                        .orElseGet(() -> {
                            UserTopicProgress newProgress =
                                    new UserTopicProgress();

                            newProgress.setUser(user);
                            newProgress.setTopic(topic);

                            return newProgress;
                        });

        progress.setCompleted(!progress.isCompleted());
        progressRepository.save(progress);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Your progress has been updated.");

        return "redirect:/my-progress";
    }

    private User getLoggedInUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElse(null);
    }

    public static class TopicProgressView {

        private final SyllabusTopic topic;
        private final boolean completed;

        public TopicProgressView(
                SyllabusTopic topic,
                boolean completed) {
            this.topic = topic;
            this.completed = completed;
        }

        public SyllabusTopic getTopic() {
            return topic;
        }

        public boolean isCompleted() {
            return completed;
        }
    }

    public static class SubjectProgressView {

        private final SyllabusSubject subject;
        private final List<TopicProgressView> topics;
        private final int completedCount;
        private final int totalCount;
        private final int percentage;

        public SubjectProgressView(
                SyllabusSubject subject,
                List<TopicProgressView> topics,
                int completedCount,
                int totalCount,
                int percentage) {

            this.subject = subject;
            this.topics = topics;
            this.completedCount = completedCount;
            this.totalCount = totalCount;
            this.percentage = percentage;
        }

        public SyllabusSubject getSubject() {
            return subject;
        }

        public List<TopicProgressView> getTopics() {
            return topics;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getPercentage() {
            return percentage;
        }
    }
}
