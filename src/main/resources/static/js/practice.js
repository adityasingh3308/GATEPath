
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

    const attemptedQuestions = new Set();
    const checkedQuestions = new Set();
    const correctQuestions = new Set();

    let currentIndex = 0;
    let sessionFinished = false;

    // Allows integers, decimals, negative numbers and leading decimal points.
    // Examples: 12, -3, 0.27, .5, 12.50
    function parseNumericAnswer(value) {
        const text = String(value ?? "").trim();

        if (!text || !/^[+-]?(?:\d+(?:\.\d*)?|\.\d+)$/.test(text)) {
            return null;
        }

        const number = Number(text);
        return Number.isFinite(number) ? number : null;
    }

    function formatNumber(value) {
        if (!Number.isFinite(value)) return "";

        // Avoid displaying unnecessary zeros such as 0.270000.
        return String(Number(value.toPrecision(12)));
    }

    function getQuestionIndex(slide) {
        const index = Number(slide.dataset.questionIndex);

        return Number.isInteger(index) && index >= 0
            ? index
            : slides.indexOf(slide);
    }

    function getQuestionType(slide) {
        return (slide.dataset.questionType || "").trim().toUpperCase();
    }

    function getCorrectOptions(slide) {
        const raw = (slide.dataset.correctAnswer || "").trim();

        if (!raw || !/^[A-Da-d\s,;]+$/.test(raw)) return [];

        return [...new Set(
            (raw.match(/[A-D]/gi) || []).map(value => value.toUpperCase())
        )].sort();
    }

    function getSelectedOptions(slide) {
        return Array.from(
            slide.querySelectorAll(
                'input[type="radio"]:checked, input[type="checkbox"]:checked'
            )
        ).map(input => input.value.trim().toUpperCase()).sort();
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

        // Backward compatibility: older NAT records may only have
        // their numerical answer stored in correct_answer.
        const raw = (slide.dataset.correctAnswer || "").trim();
        const expected = parseNumericAnswer(raw);

        if (expected !== null) {
            return { min: expected, max: expected };
        }

        return null;
    }

    function getNatExpectedText(slide) {
        const range = getNatRange(slide);

        if (range) {
            if (range.min === range.max) {
                return formatNumber(range.min);
            }

            return `${formatNumber(range.min)} – ${formatNumber(range.max)}`;
        }

        return "";
    }

    function hasAnswer(slide) {
        if (getQuestionType(slide) === "NAT") {
            const input = getNatInput(slide);
            return !!input && parseNumericAnswer(input.value) !== null;
        }

        return getSelectedOptions(slide).length > 0;
    }

    function isAnswerLocked(slide) {
        return slide.dataset.answerLocked === "true";
    }

    function getExplanation(slide) {
        return (slide.dataset.explanation || "").trim();
    }

    function checkCorrectness(slide) {
        const type = getQuestionType(slide);

        // NAT answers come from nat_min / nat_max first.
        // Do not reject a NAT question just because correct_answer is blank.
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

        const raw = (slide.dataset.correctAnswer || "").trim();
        if (!raw) return null;

        if (type === "MCQ" || type === "MSQ") {
            const correct = getCorrectOptions(slide);
            const selected = getSelectedOptions(slide);

            if (!correct.length) return null;

            if (type === "MCQ") {
                return selected.length === 1 && selected[0] === correct[0];
            }

            return selected.length === correct.length &&
                selected.every((value, index) => value === correct[index]);
        }

        return null;
    }

    function showFeedback(slide, message) {
        const feedback = slide.querySelector(".practice-feedback");
        if (feedback) feedback.textContent = message;
    }

    function clearResult(slide) {
        slide.querySelectorAll(".option-item").forEach(option => {
            option.classList.remove("is-correct", "is-incorrect");
        });

        const result = slide.querySelector(".answer-result");

        if (result) {
            result.hidden = true;
            result.classList.remove("result-correct", "result-incorrect");
        }

        const title = slide.querySelector(".answer-result-title");
        const explanation = slide.querySelector(".answer-result-explanation");

        if (title) title.textContent = "";
        if (explanation) explanation.textContent = "";
    }

    function displayResult(slide, isCorrect, unavailable = false) {
        const result = slide.querySelector(".answer-result");
        const title = slide.querySelector(".answer-result-title");
        const explanation = slide.querySelector(".answer-result-explanation");

        if (!result || !title || !explanation) return;

        result.hidden = false;
        result.classList.remove("result-correct", "result-incorrect");

        if (unavailable) {
            title.textContent = "Answer key unavailable";
            explanation.textContent =
                "Correct answer available nahi hai. Question bank mein correct_answer aur NAT answer range verify karein.";
            return;
        }

        result.classList.add(isCorrect ? "result-correct" : "result-incorrect");

        const type = getQuestionType(slide);
        let answerText;

        if (type === "MCQ" || type === "MSQ") {
            answerText = getCorrectOptions(slide).join(", ");
        } else if (type === "NAT") {
            answerText = getNatExpectedText(slide);
        } else {
            answerText = (slide.dataset.correctAnswer || "").trim();
        }

        title.textContent = isCorrect
            ? `✓ Correct Answer! (${answerText})`
            : `✗ Incorrect Answer. Correct Answer: ${answerText}`;

        const explanationText = getExplanation(slide);

        explanation.textContent = explanationText
            ? `Explanation: ${explanationText}`
            : "Is question ki explanation available nahi hai.";
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
            button.textContent = "Answer Checked";
        }

        slide.dataset.answerLocked = "true";
    }

    function updateProgress() {
        const attempted = attemptedQuestions.size;
        const percentage = Math.round((attempted / slides.length) * 100);

        if (attemptedCount) attemptedCount.textContent = String(attempted);
        if (progressFill) progressFill.style.width = `${percentage}%`;

        if (progressTrack) {
            progressTrack.setAttribute("aria-valuenow", String(percentage));
        }

        if (progressCaption) {
            progressCaption.textContent =
                `${attempted} of ${slides.length} questions attempted (${percentage}%)`;
        }

        if (sidebarCorrect) {
            sidebarCorrect.textContent = String(correctQuestions.size);
        }

        if (sidebarIncorrect) {
            sidebarIncorrect.textContent = String(
                [...checkedQuestions].filter(
                    index => !correctQuestions.has(index)
                ).length
            );
        }

        paletteButtons.forEach(button => {
            const index = Number(button.dataset.jumpIndex);

            button.classList.toggle("is-attempted", attemptedQuestions.has(index));
            button.classList.toggle("is-correct", correctQuestions.has(index));
            button.classList.toggle(
                "is-incorrect",
                checkedQuestions.has(index) && !correctQuestions.has(index)
            );
            button.classList.toggle("is-current", index === currentIndex);

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

        if (hasAnswer(slide)) {
            attemptedQuestions.add(index);
        } else {
            attemptedQuestions.delete(index);
        }

        updateProgress();
    }

    function gradeAnswer(slide, index) {
        const result = checkCorrectness(slide);

        attemptedQuestions.add(index);
        checkedQuestions.add(index);
        correctQuestions.delete(index);

        if (result === true) {
            correctQuestions.add(index);
        }

        if (result === null) {
            displayResult(slide, false, true);
        } else {
            if (getQuestionType(slide) === "MCQ" ||
                getQuestionType(slide) === "MSQ") {
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

    function showQuestion(index) {
        if (sessionFinished || !Number.isInteger(index)) return;
        if (index < 0 || index >= slides.length) return;

        slides.forEach((slide, slideIndex) => {
            slide.hidden = slideIndex !== index;
        });

        currentIndex = index;

        const activeSlide = slides[currentIndex];
        const previousButton = activeSlide.querySelector(".previousQuestion");
        const nextButton = activeSlide.querySelector(".nextQuestion");
        const checkButton = activeSlide.querySelector(".checkAnswerButton");

        if (previousButton) previousButton.disabled = currentIndex === 0;

        if (nextButton) {
            nextButton.innerHTML = currentIndex === slides.length - 1
                ? 'Finish Session <i class="fa-solid fa-flag-checkered" aria-hidden="true"></i>'
                : 'Next Question <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>';
        }

        if (checkButton && isAnswerLocked(activeSlide)) {
            checkButton.disabled = true;
            checkButton.textContent = "Answer Checked";
        }

        updateProgress();

        activeSlide.scrollIntoView({
            behavior: "auto",
            block: "start"
        });
    }

    function finishSession() {
        if (sessionFinished) return;

        // Automatically grade answered questions that have not been checked.
        slides.forEach(slide => {
            const index = getQuestionIndex(slide);

            if (index >= 0 && !isAnswerLocked(slide) && hasAnswer(slide)) {
                gradeAnswer(slide, index);
            }
        });

        sessionFinished = true;

        const attempted = checkedQuestions.size;
        const correct = correctQuestions.size;
        const incorrect = [...checkedQuestions].filter(
            index => !correctQuestions.has(index)
        ).length;
        const unanswered = slides.length - attempted;
        const accuracy = attempted > 0
            ? Math.round((correct / attempted) * 100)
            : 0;

        slides.forEach(slide => {
            slide.hidden = true;
        });

        if (finalAttempted) finalAttempted.textContent = String(attempted);
        if (finalCorrect) finalCorrect.textContent = String(correct);
        if (finalIncorrect) finalIncorrect.textContent = String(incorrect);
        if (finalUnanswered) finalUnanswered.textContent = String(unanswered);
        if (finalAccuracy) finalAccuracy.textContent = `${accuracy}%`;

        if (practiceSidebar) practiceSidebar.hidden = true;
        if (sessionHeader) sessionHeader.hidden = true;

        if (practiceLayout) {
            practiceLayout.classList.add("session-finished");
        }

        if (sessionComplete) {
            sessionComplete.hidden = false;
            sessionComplete.style.removeProperty("display");
            sessionComplete.setAttribute("aria-hidden", "false");
        } else {
            console.error("Missing #sessionComplete element in practice.html.");
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

    // Answer changes
    slides.forEach(slide => {
        slide.querySelectorAll("input").forEach(input => {
            function handleChange() {
                if (sessionFinished || isAnswerLocked(slide)) return;

                clearResult(slide);
                showFeedback(slide, "");

                const index = getQuestionIndex(slide);
                checkedQuestions.delete(index);
                correctQuestions.delete(index);

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

    // Previous / Next
    slides.forEach((slide, index) => {
        const previousButton = slide.querySelector(".previousQuestion");
        const nextButton = slide.querySelector(".nextQuestion");

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

                if (!hasAnswer(slide)) {
                    showFeedback(
                        slide,
                        getQuestionType(slide) === "NAT"
                            ? "Enter a valid numerical answer before continuing."
                            : "Please select an answer before continuing."
                    );
                    return;
                }

                updateAttempted(slide);

                if (index < slides.length - 1) {
                    showQuestion(index + 1);
                } else {
                    finishSession();
                }
            });
        }
    });

    // Question palette
    paletteButtons.forEach(button => {
        button.addEventListener("click", event => {
            event.preventDefault();
            if (sessionFinished) return;

            const targetIndex = Number(button.dataset.jumpIndex);

            if (Number.isInteger(targetIndex)) {
                showQuestion(targetIndex);
            }
        });
    });

    // End session
    endSessionButtons.forEach(button => {
        button.addEventListener("click", event => {
            event.preventDefault();

            if (sessionFinished) return;

            const shouldEnd = window.confirm(
                "End this practice session? Answered questions will be evaluated automatically."
            );

            if (shouldEnd) finishSession();
        });
    });

    showQuestion(0);
    updateProgress();
});
