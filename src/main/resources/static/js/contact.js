
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const form = document.getElementById("gpContactForm");
    const messageInput = document.getElementById("contactMessage");
    const characterCount = document.getElementById("contactCharCount");
    const status = document.getElementById("contactFormStatus");

    if (!form || !messageInput || !characterCount || !status) {
        return;
    }

    const showStatus = (message, type) => {
        status.textContent = message;
        status.className = `gp-contact-form-status is-visible is-${type}`;
    };

    messageInput.addEventListener("input", () => {
        characterCount.textContent = messageInput.value.length;
    });

    form.addEventListener("submit", (event) => {
        event.preventDefault();

        if (!form.reportValidity()) {
            return;
        }

        const name = document.getElementById("contactName").value.trim();
        const email = document.getElementById("contactEmail").value.trim();
        const subject = document.getElementById("contactSubject").value;
        const message = messageInput.value.trim();

        if (!name || !email || !subject || !message) {
            showStatus("Please complete all fields before continuing.", "error");
            return;
        }

        /*
         * Replace this placeholder with the real GATEPath support
         * email address before enabling email delivery.
         */
        const supportEmail = "support.gatepath@gmail.com";

        if (!supportEmail) {
            showStatus(
                "The contact form design is ready, but email delivery is not configured yet. Add your official support email or connect a backend endpoint.",
                "error"
            );
            return;
        }

        const subjectLine = `[GATEPath] ${subject}`;
        const body = [
            `Name: ${name}`,
            `Email: ${email}`,
            "",
            "Message:",
            message
        ].join("\n");

      const gmailUrl =
    "https://mail.google.com/mail/?view=cm&fs=1" +
    `&to=${encodeURIComponent(supportEmail)}` +
    `&su=${encodeURIComponent(subjectLine)}` +
    `&body=${encodeURIComponent(body)}`;

const gmailWindow = window.open(
    gmailUrl,
    "_blank",
    "noopener,noreferrer"
);

if (gmailWindow) {
    form.reset();
    characterCount.textContent = "0";

    showStatus(
        "Gmail opened with your message. You can send another message using the form.",
        "success"
    );
} else {
    showStatus(
        "Gmail could not open. Allow pop-ups for this site and try again.",
        "error"
    );
}
        showStatus(
            "Your email application should open with your message prepared. Please send it there.",
            "success"
        );
    });
});
