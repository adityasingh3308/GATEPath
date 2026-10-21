
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    // =========================================
    // Header elements
    // =========================================
    const themeToggle = document.getElementById("theme-toggle");
    const themeMenu = document.getElementById("theme-menu");
    const themeIcon = document.getElementById("theme-current-icon");

    const notificationsToggle = document.getElementById("notifications-toggle");
    const notificationsMenu = document.getElementById("notifications-menu");

    const accountToggle = document.getElementById("account-toggle");
    const accountMenu = document.getElementById("account-menu");

    const fullscreenToggle = document.getElementById("fullscreen-toggle");
    const fullscreenIcon = document.getElementById("fullscreen-icon");

    // =========================================
    // Theme configuration
    // =========================================
    const validThemes = ["light", "dark", "system"];
    const themeStorageKey = "gatepath-theme";

    const systemTheme = window.matchMedia(
        "(prefers-color-scheme: dark)"
    );

    const getSavedTheme = () => {
        try {
            const savedTheme = localStorage.getItem(themeStorageKey);

            return validThemes.includes(savedTheme)
                ? savedTheme
                : "system";
        } catch (error) {
            return "system";
        }
    };

    const getEffectiveTheme = (choice) => {
        if (choice === "system") {
            return systemTheme.matches ? "dark" : "light";
        }

        return choice;
    };

    // =========================================
    // Apply theme
    // =========================================
    const updateTheme = (choice, savePreference = false) => {
        if (!validThemes.includes(choice)) {
            choice = "system";
        }

        const effectiveTheme = getEffectiveTheme(choice);
        const root = document.documentElement;

        root.setAttribute("data-theme", effectiveTheme);
        root.setAttribute("data-theme-choice", choice);
        root.style.colorScheme = effectiveTheme;

        // Update theme button icon
        if (themeIcon) {
            themeIcon.className =
                effectiveTheme === "dark"
                    ? "bi bi-moon-fill"
                    : "bi bi-sun-fill";
        }

        // Update selected theme option
        document
            .querySelectorAll("#theme-menu [data-theme-choice]")
            .forEach((button) => {
                const selected = button.dataset.themeChoice === choice;

                button.setAttribute("aria-checked", String(selected));
                button.classList.toggle("active", selected);

                const checkIcon = button.querySelector(
                    ".theme-option-check"
                );

                if (checkIcon) {
                    checkIcon.style.visibility = selected
                        ? "visible"
                        : "hidden";
                }
            });

        // Persist only when the user selects a theme
        if (savePreference) {
            try {
                localStorage.setItem(themeStorageKey, choice);
            } catch (error) {
                // Theme still works for the current page.
            }
        }
    };

    // Initialize saved preference without rewriting storage
    updateTheme(getSavedTheme());

    // Follow operating-system theme changes in System mode
    const handleSystemThemeChange = () => {
        if (getSavedTheme() === "system") {
            updateTheme("system");
        }
    };

    if (typeof systemTheme.addEventListener === "function") {
        systemTheme.addEventListener("change", handleSystemThemeChange);
    } else if (typeof systemTheme.addListener === "function") {
        systemTheme.addListener(handleSystemThemeChange);
    }

    // =========================================
    // Dropdown helpers
    // =========================================
    const closeDropdown = (menu, toggle) => {
        if (!menu || !toggle) return;

        menu.hidden = true;
        toggle.setAttribute("aria-expanded", "false");
    };

    const openDropdown = (menu, toggle) => {
        if (!menu || !toggle) return;

        menu.hidden = false;
        toggle.setAttribute("aria-expanded", "true");
    };

    const dropdowns = [
        { menu: themeMenu, toggle: themeToggle },
        { menu: notificationsMenu, toggle: notificationsToggle },
        { menu: accountMenu, toggle: accountToggle }
    ];

    const closeAllDropdowns = (exceptMenu = null) => {
        dropdowns.forEach(({ menu, toggle }) => {
            if (menu && menu !== exceptMenu) {
                closeDropdown(menu, toggle);
            }
        });
    };

    const toggleDropdown = (menu, toggle) => {
        if (!menu || !toggle) return;

        const shouldOpen = menu.hidden;

        closeAllDropdowns(menu);

        if (shouldOpen) {
            openDropdown(menu, toggle);
        } else {
            closeDropdown(menu, toggle);
        }
    };

    // =========================================
    // Theme dropdown
    // =========================================
    themeToggle?.addEventListener("click", (event) => {
        event.stopPropagation();
        toggleDropdown(themeMenu, themeToggle);
    });

    document
        .querySelectorAll("#theme-menu [data-theme-choice]")
        .forEach((button) => {
            button.addEventListener("click", (event) => {
                event.stopPropagation();

                updateTheme(button.dataset.themeChoice, true);
                closeDropdown(themeMenu, themeToggle);
            });
        });

    // =========================================
    // Notifications dropdown
    // =========================================
    notificationsToggle?.addEventListener("click", (event) => {
        event.stopPropagation();
        toggleDropdown(notificationsMenu, notificationsToggle);
    });

    // =========================================
    // Account dropdown
    // =========================================
    accountToggle?.addEventListener("click", (event) => {
        event.stopPropagation();
        toggleDropdown(accountMenu, accountToggle);
    });

    // =========================================
    // Close menus on outside click
    // =========================================
    document.addEventListener("click", (event) => {
        dropdowns.forEach(({ menu, toggle }) => {
            if (!menu || !toggle) return;

            if (
                !menu.contains(event.target) &&
                !toggle.contains(event.target)
            ) {
                closeDropdown(menu, toggle);
            }
        });
    });

    // =========================================
    // Keyboard accessibility
    // =========================================
    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") return;

        const openDropdownEntry = dropdowns.find(
            ({ menu }) => menu && !menu.hidden
        );

        if (openDropdownEntry) {
            closeDropdown(
                openDropdownEntry.menu,
                openDropdownEntry.toggle
            );

            openDropdownEntry.toggle?.focus();
        }
    });

    // =========================================
    // Fullscreen
    // =========================================
    const updateFullscreenButton = () => {
        if (!fullscreenIcon || !fullscreenToggle) return;

        const isFullscreen = Boolean(document.fullscreenElement);

        fullscreenIcon.className = isFullscreen
            ? "bi bi-fullscreen-exit"
            : "bi bi-arrows-fullscreen";

        fullscreenToggle.setAttribute(
            "aria-label",
            isFullscreen ? "Exit fullscreen" : "Enter fullscreen"
        );

        fullscreenToggle.setAttribute(
            "aria-pressed",
            String(isFullscreen)
        );
    };

    fullscreenToggle?.addEventListener("click", async () => {
        try {
            if (document.fullscreenElement) {
                await document.exitFullscreen();
            } else if (document.documentElement.requestFullscreen) {
                await document.documentElement.requestFullscreen();
            }
        } catch (error) {
            console.error("Could not change fullscreen mode:", error);
        }
    });

    document.addEventListener(
        "fullscreenchange",
        updateFullscreenButton
    );

    // Set the correct icon on page load
    updateFullscreenButton();
});
