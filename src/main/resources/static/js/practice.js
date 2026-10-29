
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const container = document.getElementById("practiceQuestionContainer");
    if (!container) return;

    const slides = Array.from(container.querySelectorAll(".question-slide"));
    if (!slides.length) return;

    const paletteButtons = Array.from(
        document.querySelectorAll(".gp-palette-button[data-jump-index]")
    );

    const attemptedCount = document.getElementById("attemptedCount");
    const progressFill = document.getElementById("practiceProgress");
    const progressCaption = document.getElementById("progressCaption");
    const progressTrack = document.getElementById("practiceProgressTrack");

    const sidebarCorrect = document.getElementById("sidebarCorrect");
    const sidebarIncorrect = document.getElementById("sidebarIncorrect");

    const sessionComplete = document.getElementById("sessionComplete");
    const finalAttempted = document.getElementById("finalAttempted");
    const finalCorrect = document.getElementById("finalCorrect");
    const finalIncorrect = document.getElementById("finalIncorrect");
    const finalUnanswered = document.getElementById("finalUnanswered");
    const finalAccuracy = document.getElementById("finalAccuracy");

    const practiceLayout = document.querySelector(".practice-layout");
    const practiceSidebar = document.getElementById("practiceSidebar");
    const sessionHeader = document.querySelector(".gp-active-session-header");

    const endSessionButtons = Array.from(
        document.querySelectorAll(".end-session")
    );

    // Each question is identified by its zero-based index.
    const attemptedQuestions = new Set();
    const checkedQuestions = new Set();
    const correctQuestions = new Set();
    const unavailableQuestions = new Set();

    let currentIndex = 0;
    let sessionFinished = false;

    /* =====================================================
       NUMERICAL ANSWERS
    ===================================================== */

    function parseNumericAnswer(value) {
        const text = String(value ?? "").trim();

        if (!/^[+-]?(?:\d+(?:\.\d*)?|\.\d+)$/.test(text)) {
            return null;
        }

        const number = Number(text);

        return Number.isFinite(number) ? number : null;
    }

    function formatNumber(value) {
        if (!Number.isFinite(value)) return "";

        return String(Number(value.toPrecision(12)));
    }

    /* =====================================================
       QUESTION HELPERS
    ===================================================== */

    function getQuestionIndex(slide) {
        const index = Number(slide.dataset.questionIndex);

        return Number.isInteger(index) && index >= 0
            ? index
            : slides.indexOf(slide);
    }

    function getQuestionType(slide) {
        return (slide.dataset.questionType || "")
            .trim()
            .toUpperCase();
    }

    function getCorrectOptions(slide) {
        const raw = (slide.dataset.correctAnswer || "").trim();

        if (!raw || !/^[A-Da-d\s,;]+$/.test(raw)) {
            return [];
        }

        return [...new Set(
            (raw.match(/[A-D]/gi) || [])
                .map(value => value.toUpperCase())
        )].sort();
    }

    function getSelectedOptions(slide) {
        return Array.from(
            slide.querySelectorAll(
                'input[type="radio"]:checked, input[type="checkbox"]:checked'
            )
        )
            .map(input => input.value.trim().toUpperCase())
            .sort();
    }

    function getNatInput(slide) {
        return slide.querySelector(".nat-answer");
    }

    function getNatRange(slide) {
        const minText = (slide.dataset.natMin || "").trim();
        const maxText = (slide.dataset.natMax || "").trim();

        const min = minText ? parseNumericAnswer(minText) : null;
        const max = maxText ? parseNumericAnswer(maxText) : null;

        if (min !== null && max !== null && min <= max) {
            return { min, max };
        }

        // Compatibility with older NAT records that only have
        // correct_answer populated.
        const expected = parseNumericAnswer(slide.dataset.correctAnswer);

        if (expected !== null) {
            return { min: expected, max: expected };
        }

        return null;
    }

    function getNatExpectedText(slide) {
        const range = getNatRange(slide);

        if (!range) return "";

        if (range.min === range.max) {
            return formatNumber(range.min);
        }

        return `${formatNumber(range.min)} – ${formatNumber(range.max)}`;
    }

    function getExplanation(slide) {
        return (slide.dataset.explanation || "").trim();
    }

    function hasAnswer(slide) {
        if (getQuestionType(slide) === "NAT") {
            const input = getNatInput(slide);

            return !!input &&
                parseNumericAnswer(input.value) !== null;
        }

        return getSelectedOptions(slide).length > 0;
    }

    function isAnswerLocked(slide) {
        return slide.dataset.answerLocked === "true";
    }

    /* =====================================================
       ANSWER VALIDATION
    ===================================================== */

    function checkCorrectness(slide) {
        const type = getQuestionType(slide);

        if (type === "NAT") {
            const input = getNatInput(slide);
            if (!input) return null;

            const entered = parseNumericAnswer(input.value);

            if (entered === null) return false;

            const range = getNatRange(slide);

            if (!range) return null;

            const tolerance = 0.000001;

            return entered >= range.min - tolerance &&
                entered <= range.max + tolerance;
        }

        if (type === "MCQ" || type === "MSQ") {
            const correct = getCorrectOptions(slide);
            const selected = getSelectedOptions(slide);

            if (!correct.length) return null;

            if (type === "MCQ") {
                return selected.length === 1 &&
                    selected[0] === correct[0];
            }

            // MSQ requires an exact match, including every
            // correct option and no extra incorrect options.
            return selected.length === correct.length &&
                selected.every(
                    (value, index) => value === correct[index]
                );
        }

        return null;
    }

    /* =====================================================
       FEEDBACK AND RESULT DISPLAY
    ===================================================== */

    function showFeedback(slide, message) {
        const feedback = slide.querySelector(".practice-feedback");

        if (feedback) {
            feedback.textContent = message;
        }
    }

    function clearResult(slide) {
        slide.querySelectorAll(".option-item").forEach(option => {
            option.classList.remove("is-correct", "is-incorrect");
        });

        const result = slide.querySelector(".answer-result");

        if (result) {
            result.hidden = true;
            result.classList.remove(
                "result-correct",
                "result-incorrect"
            );
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
        result.classList.remove(
            "result-correct",
            "result-incorrect"
        );

        if (unavailable) {
            title.textContent = "Answer key unavailable";

            explanation.textContent =
                "This question cannot be evaluated because its answer key " +
                "is missing or invalid. Please verify the question bank.";

            return;
        }

        result.classList.add(
            isCorrect ? "result-correct" : "result-incorrect"
        );

        const type = getQuestionType(slide);
        let answerText = "";

        if (type === "MCQ" || type === "MSQ") {
            answerText = getCorrectOptions(slide).join(", ");
        } else if (type === "NAT") {
            answerText = getNatExpectedText(slide);
        }

        title.textContent = isCorrect
            ? `✓ Correct Answer! (${answerText})`
            : `✗ Incorrect Answer. Correct Answer: ${answerText}`;

        const explanationText = getExplanation(slide);

        explanation.textContent = explanationText
            ? `Explanation: ${explanationText}`
            : "Explanation is not available for this question.";
    }

    function highlightOptions(slide) {
        const correct = getCorrectOptions(slide);
        const selected = getSelectedOptions(slide);

        slide.querySelectorAll(".option-item").forEach(option => {
            const input = option.querySelector("input");
            if (!input) return;

            const value = input.value.trim().toUpperCase();

            option.classList.remove("is-correct", "is-incorrect");

            if (correct.includes(value)) {
                option.classList.add("is-correct");
            } else if (selected.includes(value)) {
                option.classList.add("is-incorrect");
            }
        });
    }

    function lockAnswer(slide) {
        slide.querySelectorAll(
            'input[type="radio"], input[type="checkbox"], .nat-answer'
        ).forEach(input => {
            input.disabled = true;
        });

        const button = slide.querySelector(".checkAnswerButton");

        if (button) {
            button.disabled = true;
            button.innerHTML =
                '<i class="fa-solid fa-check" aria-hidden="true"></i> ' +
                "Answer Checked";
        }

        slide.dataset.answerLocked = "true";
    }

    /* =====================================================
       PROGRESS AND PALETTE
    ===================================================== */

    function getIncorrectCount() {
        return [...checkedQuestions].filter(index =>
            !correctQuestions.has(index) &&
            !unavailableQuestions.has(index)
        ).length;
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
        }

        if (progressTrack) {
            progressTrack.setAttribute(
                "aria-valuenow",
                String(percentage)
            );
        }

        if (progressCaption) {
            progressCaption.textContent =
                `${attempted} of ${slides.length} questions attempted ` +
                `(${percentage}%)`;
        }

        if (sidebarCorrect) {
            sidebarCorrect.textContent =
                String(correctQuestions.size);
        }

        if (sidebarIncorrect) {
            sidebarIncorrect.textContent =
                String(getIncorrectCount());
        }

        paletteButtons.forEach(button => {
            const index = Number(button.dataset.jumpIndex);

            button.classList.toggle(
                "is-attempted",
                attemptedQuestions.has(index)
            );

            button.classList.toggle(
                "is-correct",
                correctQuestions.has(index)
            );

            button.classList.toggle(
                "is-incorrect",
                checkedQuestions.has(index) &&
                !correctQuestions.has(index) &&
                !unavailableQuestions.has(index)
            );

            button.classList.toggle(
                "is-current",
                index === currentIndex
            );

            if (index === currentIndex) {
                button.setAttribute("aria-current", "step");
            } else {
                button.removeAttribute("aria-current");
            }
        });
    }

    function updateAttempted(slide) {
        const index = getQuestionIndex(slide);
        if (index < 0) return;

        if (hasAnswer(slide) || isAnswerLocked(slide)) {
            attemptedQuestions.add(index);
        } else {
            attemptedQuestions.delete(index);
        }

        updateProgress();
    }

    /* =====================================================
       GRADING
    ===================================================== */

    function gradeAnswer(slide, index) {
        if (isAnswerLocked(slide)) return;

        const result = checkCorrectness(slide);

        attemptedQuestions.add(index);
        checkedQuestions.add(index);

        correctQuestions.delete(index);
        unavailableQuestions.delete(index);

        if (result === true) {
            correctQuestions.add(index);
        } else if (result === null) {
            unavailableQuestions.add(index);
        }

        if (result === null) {
            displayResult(slide, false, true);
        } else {
            const type = getQuestionType(slide);

            if (type === "MCQ" || type === "MSQ") {
                highlightOptions(slide);
            }

            displayResult(slide, result);
        }

        lockAnswer(slide);
    }

    function checkAnswer(slide) {
        if (sessionFinished || isAnswerLocked(slide)) return;

        if (!hasAnswer(slide)) {
            showFeedback(
                slide,
                getQuestionType(slide) === "NAT"
                    ? "Enter a valid number, for example 12 or 0.27."
                    : "Please select an answer first."
            );

            return;
        }

        showFeedback(slide, "");

        const index = getQuestionIndex(slide);
        if (index < 0) return;

        gradeAnswer(slide, index);
        updateProgress();
    }

    /* =====================================================
       QUESTION NAVIGATION
    ===================================================== */

    function showQuestion(index) {
        if (sessionFinished || !Number.isInteger(index)) return;

        if (index < 0 || index >= slides.length) return;

        slides.forEach((slide, slideIndex) => {
            slide.hidden = slideIndex !== index;
        });

        currentIndex = index;

        const activeSlide = slides[currentIndex];

        const previousButton = activeSlide.querySelector(
            ".previousQuestion"
        );

        const nextButton = activeSlide.querySelector(
            ".nextQuestion"
        );

        const checkButton = activeSlide.querySelector(
            ".checkAnswerButton"
        );

        if (previousButton) {
            previousButton.disabled = currentIndex === 0;
        }

        if (nextButton) {
            nextButton.innerHTML =
                currentIndex === slides.length - 1
                    ? 'Finish Session <i class="fa-solid fa-flag-checkered" aria-hidden="true"></i>'
                    : 'Next Question <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>';
        }

        if (checkButton && isAnswerLocked(activeSlide)) {
            checkButton.disabled = true;
            checkButton.innerHTML =
                '<i class="fa-solid fa-check" aria-hidden="true"></i> ' +
                "Answer Checked";
        }

        updateProgress();

        activeSlide.scrollIntoView({
            behavior: "auto",
            block: "start"
        });
    }

    /* =====================================================
       FINAL RESULTS
    ===================================================== */

    function updateUnavailableResultRow(count) {
        const breakdown = document.querySelector(
            ".gp-result-breakdown"
        );

        if (!breakdown) return;

        let row = document.getElementById("finalUnavailableRow");

        if (count <= 0) {
            if (row) row.remove();
            return;
        }

        if (!row) {
            row = document.createElement("div");
            row.id = "finalUnavailableRow";
            row.className = "gp-result-row";

            const label = document.createElement("span");
            const icon = document.createElement("i");
            const text = document.createElement("span");
            const value = document.createElement("strong");

            icon.className = "fa-solid fa-circle-info";
            icon.setAttribute("aria-hidden", "true");
            icon.style.color = "var(--practice-amber)";

            text.textContent = "Answer key unavailable";
            value.id = "finalUnavailable";
            value.textContent = "0";

            label.append(icon, text);
            row.append(label, value);
            breakdown.appendChild(row);
        }

        const value = row.querySelector("#finalUnavailable");

        if (value) {
            value.textContent = String(count);
        }
    }

    function finishSession() {
        if (sessionFinished) return;

        // Automatically grade answers entered but not checked.
        // Questions without an answer remain unanswered.
        slides.forEach(slide => {
            const index = getQuestionIndex(slide);

            if (
                index >= 0 &&
                !isAnswerLocked(slide) &&
                hasAnswer(slide)
            ) {
                gradeAnswer(slide, index);
            }
        });

        sessionFinished = true;

        const attempted = checkedQuestions.size;
        const correct = correctQuestions.size;
        const incorrect = getIncorrectCount();
        const unavailable = unavailableQuestions.size;

        const unanswered = Math.max(
            0,
            slides.length - attempted
        );

        // Missing/invalid answer keys are excluded from the
        // accuracy denominator.
        const evaluated = correct + incorrect;

        const accuracy = evaluated > 0
            ? Math.round((correct / evaluated) * 100)
            : 0;

        slides.forEach(slide => {
            slide.hidden = true;
        });

        if (finalAttempted) {
            finalAttempted.textContent = String(attempted);
        }

        if (finalCorrect) {
            finalCorrect.textContent = String(correct);
        }

        if (finalIncorrect) {
            finalIncorrect.textContent = String(incorrect);
        }

        if (finalUnanswered) {
            finalUnanswered.textContent = String(unanswered);
        }

        if (finalAccuracy) {
            finalAccuracy.textContent = `${accuracy}%`;
        }

        // Update the caption to reflect the actual count.
        const attemptedCaption = document.querySelector(
            ".gp-result-attempted .gp-result-stat-caption"
        );

        if (attemptedCaption) {
            attemptedCaption.textContent = "Questions answered";
        }

        updateUnavailableResultRow(unavailable);

        if (practiceSidebar) {
            practiceSidebar.hidden = true;
        }

        if (sessionHeader) {
            sessionHeader.hidden = true;
        }

        if (practiceLayout) {
            practiceLayout.classList.add("session-finished");
        }

        if (sessionComplete) {
            sessionComplete.hidden = false;
            sessionComplete.style.removeProperty("display");
            sessionComplete.setAttribute("aria-hidden", "false");
        } else {
            console.error(
                "Missing #sessionComplete element in practice.html."
            );
        }

        container.querySelectorAll(
            ".previousQuestion, .nextQuestion, .checkAnswerButton"
        ).forEach(button => {
            button.disabled = true;
        });

        updateProgress();

        if (sessionComplete) {
            sessionComplete.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        }
    }

    /* =====================================================
       ANSWER INPUT EVENTS
    ===================================================== */

    slides.forEach(slide => {
        slide.querySelectorAll("input").forEach(input => {
            function handleChange() {
                if (sessionFinished || isAnswerLocked(slide)) return;

                clearResult(slide);
                showFeedback(slide, "");

                const index = getQuestionIndex(slide);

                checkedQuestions.delete(index);
                correctQuestions.delete(index);
                unavailableQuestions.delete(index);

                updateAttempted(slide);
            }

            input.addEventListener("change", handleChange);

            if (input.classList.contains("nat-answer")) {
                input.addEventListener("input", handleChange);
            }
        });

        const checkButton = slide.querySelector(".checkAnswerButton");

        if (checkButton) {
            checkButton.addEventListener("click", event => {
                event.preventDefault();
                checkAnswer(slide);
            });
        }
    });

    /* =====================================================
       PREVIOUS / NEXT
    ===================================================== */

    slides.forEach((slide, index) => {
        const previousButton = slide.querySelector(
            ".previousQuestion"
        );

        const nextButton = slide.querySelector(
            ".nextQuestion"
        );

        if (previousButton) {
            previousButton.addEventListener("click", event => {
                event.preventDefault();
                showQuestion(index - 1);
            });
        }

        if (nextButton) {
            nextButton.addEventListener("click", event => {
                event.preventDefault();

                if (sessionFinished) return;

                // No answer is required to move forward.
                // If an answer was entered, retain it and mark
                // the question as attempted.
                if (hasAnswer(slide)) {
                    updateAttempted(slide);
                }

                if (index < slides.length - 1) {
                    showQuestion(index + 1);
                } else {
                    finishSession();
                }
            });
        }
    });

    /* =====================================================
       QUESTION PALETTE
    ===================================================== */

    paletteButtons.forEach(button => {
        button.addEventListener("click", event => {
            event.preventDefault();

            if (sessionFinished) return;

            const targetIndex = Number(button.dataset.jumpIndex);

            if (
                Number.isInteger(targetIndex) &&
                targetIndex >= 0 &&
                targetIndex < slides.length
            ) {
                showQuestion(targetIndex);
            }
        });
    });

    /* =====================================================
       END SESSION
    ===================================================== */

    endSessionButtons.forEach(button => {
        button.addEventListener("click", event => {
            event.preventDefault();

            if (sessionFinished) return;

            const shouldEnd = window.confirm(
                "End this practice session? Answers you entered " +
                "will be evaluated automatically. Unanswered " +
                "questions will remain unanswered."
            );

            if (shouldEnd) {
                finishSession();
            }
        });
    });

    /* =====================================================
       INITIALIZE
    ===================================================== */

    showQuestion(0);
    updateProgress();
});
