
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const copyButton = document.getElementById("gpCreditsCopyStack");
    const copyStatus = document.getElementById("gpCreditsCopyStatus");

    if (!copyButton || !copyStatus) {
        return;
    }

    const technologyStack = [
        "Java",
        "Spring Boot",
        "MySQL",
        "HTML5",
        "CSS3",
        "JavaScript",
        "Thymeleaf",
        "Bootstrap"
    ].join(", ");

    const showStatus = (message, success = true) => {
        copyStatus.textContent = message;
        copyStatus.style.color = success ? "" : "#dc3545";
    };

    const fallbackCopy = (text) => {
        const textArea = document.createElement("textarea");

        textArea.value = text;
        textArea.setAttribute("readonly", "");
        textArea.style.position = "fixed";
        textArea.style.opacity = "0";
        textArea.style.pointerEvents = "none";

        document.body.appendChild(textArea);
        textArea.select();

        let copied = false;

        try {
            copied = document.execCommand("copy");
        } catch (error) {
            copied = false;
        } finally {
            textArea.remove();
        }

        return copied;
    };

    copyButton.addEventListener("click", async () => {
        const buttonLabel = copyButton.querySelector("span");

        copyButton.disabled = true;

        try {
            if (navigator.clipboard && window.isSecureContext) {
                await navigator.clipboard.writeText(technologyStack);
            } else if (!fallbackCopy(technologyStack)) {
                throw new Error("Clipboard copy failed");
            }

            if (buttonLabel) {
                buttonLabel.textContent = "Copied!";
            }

            copyButton.querySelector("i")?.classList.replace(
                "bi-copy",
                "bi-check2"
            );

            showStatus("Technology stack copied successfully.");
        } catch (error) {
            showStatus(
                "Unable to copy automatically. Please select the technology names manually.",
                false
            );
        } finally {
            copyButton.disabled = false;

            window.setTimeout(() => {
                if (buttonLabel) {
                    buttonLabel.textContent = "Copy stack";
                }

                const icon = copyButton.querySelector("i");

                if (icon) {
                    icon.classList.replace("bi-check2", "bi-copy");
                }
            }, 2200);
        }
    });
});
