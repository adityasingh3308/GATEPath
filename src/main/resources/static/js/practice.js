
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    // ==========================================
    // 1. SETUP CARD SELECTION
    // ==========================================

    document.querySelectorAll(
        ".gp-choice-card input, .gp-limit-card input"
    ).forEach((input) => {
        const updateSelection = () => {
            const group = input.closest(
                ".gp-choice-grid, .gp-limit-grid"
            );

            if (!group) return;

            group.querySelectorAll(
                ".gp-choice-card, .gp-limit-card"
            ).forEach((card) => {
                const control = card.querySelector("input");

                card.classList.toggle(
                    "is-selected",
                    Boolean(control && control.checked)
                );
            });
        };

        input.addEventListener("change", updateSelection);
        updateSelection();
    });

    // ==========================================
    // 2. SESSION ELEMENTS
    // ==========================================

    const slides = Array.from(
        document.querySelectorAll(".question-slide")
    );

    if (slides.length === 0) return;

    const attemptedQuestions = new Set();
    const correctQuestions = new Set();

    const attemptedCount = document.getElementById("attemptedCount");
    const progressFill = document.getElementById("practiceProgress");
    const progressCaption = document.getElementById("progressCaption");

    const sessionComplete = document.querySelector(".session-complete");
    const finalAttempted = document.getElementById("finalAttempted");
    const finalCorrect = document.getElementById("finalCorrect");
    const finalAccuracy = document.getElementById("finalAccuracy");

    let currentIndex = 0;
    let sessionFinished = false;

    // ==========================================
    // 3. READ USER ANSWERS
    // ==========================================

    function getQuestionType(slide) {
        return (slide.dataset.questionType || "")
            .trim()
            .toUpperCase();
    }

    function getSelectedOptions(slide) {
        return Array.from(
            slide.querySelectorAll(
                'input[type="radio"]:checked, input[type="checkbox"]:checked'
            )
        ).map((input) => input.value.trim().toUpperCase()).sort();
    }

    function getNumericInput(slide) {
        return slide.querySelector(
            ".nat-answer, input[type='number'], input[data-question-type='NAT']"
        );
    }

    function hasAnswer(slide) {
        if (getQuestionType(slide) === "NAT") {
            const input = getNumericInput(slide);

            if (!input || input.value.trim() === "") return false;

            return Number.isFinite(Number(input.value));
        }

        return getSelectedOptions(slide).length > 0;
    }

    function getCorrectOptions(slide) {
        const raw = (slide.dataset.correctAnswer || "").trim();

        // Expected MCQ/MSQ values: B, A,C, A,C,D or A C D.
        // Avoid guessing if the database uses an unsupported format.
        if (!/^[A-Da-d\s,;]+$/.test(raw)) return [];

        return Array.from(
            new Set((raw.match(/[A-D]/gi) || [])
                .map((value) => value.toUpperCase()))
        ).sort();
    }

    // ==========================================
    // 4. CHECK CORRECT ANSWER
    // ==========================================

    function checkCorrectness(slide) {
        const type = getQuestionType(slide);
        const rawCorrect = (slide.dataset.correctAnswer || "").trim();

        if (!rawCorrect) return null;

        if (type === "NAT") {
            const input = getNumericInput(slide);

            if (!input || input.value.trim() === "") return false;

            const userValue = Number(input.value);

            // Support exact numbers and reasonable floating-point tolerance.
            // Use a tolerance of 0.000001 for numeric comparison.
            const correctValue = Number(rawCorrect);

            if (
                !Number.isFinite(userValue) ||
                !Number.isFinite(correctValue)
            ) {
                return null;
            }

            return Math.abs(userValue - correctValue) <= 0.000001;
        }

        if (type === "MCQ" || type === "MSQ") {
            const correctOptions = getCorrectOptions(slide);
            const selectedOptions = getSelectedOptions(slide);

            if (correctOptions.length === 0) return null;

            if (type === "MCQ") {
                return selectedOptions.length === 1 &&
                    selectedOptions[0] === correctOptions[0];
            }

            return selectedOptions.length === correctOptions.length &&
                selectedOptions.every(
                    (value, index) => value === correctOptions[index]
                );
        }

        return null;
    }

    // ==========================================
    // 5. TRACK ATTEMPTS AND PROGRESS
    // ==========================================

    function updateAttempted(slide) {
        const index = Number(slide.dataset.questionIndex);

        if (!Number.isInteger(index)) return;

        if (hasAnswer(slide)) {
            attemptedQuestions.add(index);
        } else {
            attemptedQuestions.delete(index);
        }

        updateProgress();
    }

    function updateProgress() {
        const attempted = attemptedQuestions.size;
        const percentage = Math.round(
            (attempted / slides.length) * 100
        );

        if (attemptedCount) {
            attemptedCount.textContent = String(attempted);
        }

        if (progressFill) {
            progressFill.style.width = `${percentage}%`;
            progressFill.setAttribute("aria-valuenow", String(percentage));
            progressFill.setAttribute("role", "progressbar");
            progressFill.setAttribute("aria-valuemin", "0");
            progressFill.setAttribute("aria-valuemax", "100");
        }

        if (progressCaption) {
            progressCaption.textContent = `${percentage}% completed`;
        }
    }

    // ==========================================
    // 6. RESULT DISPLAY
    // ==========================================

    function clearResult(slide) {
        slide.querySelectorAll(".option-item").forEach((option) => {
            option.classList.remove("is-correct", "is-incorrect");
        });

        const result = slide.querySelector(".answer-result");

        if (result) {
            result.hidden = true;
            result.classList.remove("result-correct", "result-incorrect");
        }

        const title = slide.querySelector(".answer-result-title");
        const explanation = slide.querySelector(
            ".answer-result-explanation"
        );

        if (title) title.textContent = "";
        if (explanation) explanation.textContent = "";
    }

    function displayResult(slide, isCorrect, unavailable = false) {
        const result = slide.querySelector(".answer-result");
        const title = slide.querySelector(".answer-result-title");
        const explanation = slide.querySelector(
            ".answer-result-explanation"
        );

        if (!result || !title || !explanation) return;

        result.hidden = false;
        result.classList.toggle(
            "result-correct",
            isCorrect && !unavailable
        );
        result.classList.toggle(
            "result-incorrect",
            !isCorrect && !unavailable
        );

        if (unavailable) {
            title.textContent = "Answer key unavailable";
            explanation.textContent =
                "The correct answer is missing or uses an unsupported format. Please verify this question in the database.";
            return;
        }

        title.textContent = isCorrect
            ? "✓ Correct Answer!"
            : "✗ Incorrect Answer";

        const explanationText = (
            slide.dataset.explanation || ""
        ).trim();

        explanation.textContent = explanationText
            ? `Explanation: ${explanationText}`
            : "An explanation has not been added for this question yet.";
    }

    function highlightOptions(slide, isCorrect) {
        const correctOptions = getCorrectOptions(slide);
        const selectedOptions = getSelectedOptions(slide);

        slide.querySelectorAll(".option-item").forEach((option) => {
            const input = option.querySelector("input");

            if (!input) return;

            const value = input.value.trim().toUpperCase();

            if (correctOptions.includes(value)) {
                option.classList.add("is-correct");
            } else if (selectedOptions.includes(value)) {
                option.classList.add("is-incorrect");
            }
        });
    }

    function showFeedback(slide, message) {
        const feedback = slide.querySelector(".practice-feedback");

        if (feedback) feedback.textContent = message;
    }

    function checkAnswer(slide) {
        if (!hasAnswer(slide)) {
            showFeedback(
                slide,
                "Please select or enter a valid answer first."
            );
            return;
        }

        showFeedback(slide, "");
        clearResult(slide);

        const index = Number(slide.dataset.questionIndex);
        const correct = checkCorrectness(slide);

        updateAttempted(slide);

        // Update the score whenever the user checks an answer.
        correctQuestions.delete(index);

        if (correct === null) {
            displayResult(slide, false, true);
            return;
        }

        if (correct) {
            correctQuestions.add(index);
        }

        if (
            getQuestionType(slide) === "MCQ" ||
            getQuestionType(slide) === "MSQ"
        ) {
            highlightOptions(slide, correct);
        }

        displayResult(slide, correct);
    }

    // ==========================================
    // 7. RESET FEEDBACK WHEN ANSWER CHANGES
    // ==========================================

    slides.forEach((slide) => {
        const index = Number(slide.dataset.questionIndex);

        slide.querySelectorAll("input").forEach((input) => {
            input.addEventListener("change", () => {
                clearResult(slide);
                showFeedback(slide, "");

                correctQuestions.delete(index);
                updateAttempted(slide);
            });

            if (
                input.classList.contains("nat-answer") ||
                input.type === "number"
            ) {
                input.addEventListener("input", () => {
                    clearResult(slide);
                    showFeedback(slide, "");

                    correctQuestions.delete(index);
                    updateAttempted(slide);
                });
            }
        });

        slide.querySelector(".checkAnswerButton")?.addEventListener(
            "click",
            () => checkAnswer(slide)
        );
    });

    // ==========================================
    // 8. NAVIGATION
    // ==========================================

    function showQuestion(index) {
        if (index < 0 || index >= slides.length) return;

        slides.forEach((slide, slideIndex) => {
            slide.hidden = slideIndex !== index;
        });

        currentIndex = index;

        const slide = slides[currentIndex];
        const previous = slide.querySelector(".previousQuestion");
        const next = slide.querySelector(".nextQuestion");

        if (previous) {
            previous.disabled = currentIndex === 0;
        }

        if (next) {
            next.innerHTML = currentIndex === slides.length - 1
                ? 'Finish Session <i class="fa-solid fa-flag-checkered"></i>'
                : 'Next Question <i class="fa-solid fa-arrow-right"></i>';
        }
    }

    function finishSession() {
        sessionFinished = true;

        slides.forEach((slide) => {
            slide.hidden = true;
        });

        if (sessionComplete) {
            sessionComplete.hidden = false;
        }

        const attempted = attemptedQuestions.size;
        const correct = correctQuestions.size;
        const accuracy = attempted > 0
            ? Math.round((correct / attempted) * 100)
            : 0;

        if (finalAttempted) finalAttempted.textContent = String(attempted);
        if (finalCorrect) finalCorrect.textContent = String(correct);
        if (finalAccuracy) finalAccuracy.textContent = `${accuracy}%`;

        updateProgress();
    }

    slides.forEach((slide, index) => {
        slide.querySelector(".previousQuestion")?.addEventListener(
            "click",
            () => showQuestion(index - 1)
        );

        slide.querySelector(".nextQuestion")?.addEventListener(
            "click",
            () => {
                if (sessionFinished) return;

                if (!hasAnswer(slide)) {
                    showFeedback(
                        slide,
                        "Please select or enter an answer before continuing."
                    );
                    return;
                }

                updateAttempted(slide);

                if (index < slides.length - 1) {
                    showQuestion(index + 1);
                } else {
                    finishSession();
                }
            }
        );
    });

    // ==========================================
    // 9. INITIALIZE
    // ==========================================

    slides.forEach((slide) => {
        if (hasAnswer(slide)) {
            const index = Number(slide.dataset.questionIndex);

            if (Number.isInteger(index)) {
                attemptedQuestions.add(index);
            }
        }
    });

    showQuestion(0);
    updateProgress();
});
