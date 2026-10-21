
document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.getElementById("app-sidebar");
    const sidebarToggle = document.getElementById("sidebar-toggle");

    const searchInput = document.getElementById("sidebar-search-input");
    const navigation = document.getElementById("sidebar-navigation");
    const noResults = document.getElementById("sidebar-no-results");
    const searchEmpty = document.getElementById("sidebar-search-empty");

    if (!sidebar) {
        return;
    }

    const mobileQuery = window.matchMedia("(max-width: 767px)");

    // -----------------------------------------
    // Sidebar open / close
    // -----------------------------------------

    const updateToggleState = () => {
        if (!sidebarToggle) {
            return;
        }

        const isMobile = mobileQuery.matches;
        const isOpen = isMobile
            ? document.body.classList.contains("sidebar-open")
            : !document.body.classList.contains("sidebar-collapsed");

        sidebarToggle.setAttribute("aria-expanded", String(isOpen));
        sidebarToggle.setAttribute(
            "aria-label",
            isMobile
                ? (isOpen ? "Close sidebar" : "Open sidebar")
                : (isOpen ? "Collapse sidebar" : "Expand sidebar")
        );
    };

    const closeMobileSidebar = () => {
        document.body.classList.remove("sidebar-open");
        updateToggleState();
    };

    sidebarToggle?.addEventListener("click", () => {
        if (mobileQuery.matches) {
            document.body.classList.toggle("sidebar-open");
        } else {
            document.body.classList.toggle("sidebar-collapsed");
        }

        updateToggleState();
    });

    // Close mobile sidebar when clicking outside it.
    document.addEventListener("click", (event) => {
        if (!mobileQuery.matches) {
            return;
        }

        if (
            document.body.classList.contains("sidebar-open") &&
            !sidebar.contains(event.target) &&
            !sidebarToggle?.contains(event.target)
        ) {
            closeMobileSidebar();
        }
    });

    // Close sidebar with Escape.
    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && mobileQuery.matches) {
            closeMobileSidebar();
        }
    });

    // Close mobile sidebar after selecting a navigation link.
    sidebar.querySelectorAll(".sidebar-link[href]").forEach((link) => {
        link.addEventListener("click", () => {
            if (mobileQuery.matches) {
                closeMobileSidebar();
            }
        });
    });

    // Update state when switching between mobile and desktop.
    mobileQuery.addEventListener("change", () => {
        document.body.classList.remove("sidebar-open");
        updateToggleState();
    });

    updateToggleState();

    // -----------------------------------------
    // Sidebar menu search
    // -----------------------------------------

    if (!searchInput || !navigation) {
        return;
    }

    const menuItems = Array.from(
        navigation.querySelectorAll(".sidebar-item")
    );

    const sectionHeadings = Array.from(
        navigation.querySelectorAll(".sidebar-section-title")
    );

    const filterMenu = () => {
        const query = searchInput.value.trim().toLowerCase();
        let visibleItems = 0;

        menuItems.forEach((item) => {
            const link = item.querySelector(".sidebar-link");
            const label = link?.innerText?.trim().toLowerCase() || "";
            const matches = label.includes(query);

            item.hidden = !matches;

            if (matches) {
                visibleItems++;
            }
        });

        // Show a section heading only if a matching item follows it
        // before the next section heading.
        sectionHeadings.forEach((heading) => {
            let next = heading.nextElementSibling;
            let hasVisibleItem = false;

            while (next && !next.classList.contains("sidebar-section-title")) {
                if (
                    next.classList.contains("sidebar-item") &&
                    !next.hidden
                ) {
                    hasVisibleItem = true;
                    break;
                }

                next = next.nextElementSibling;
            }

            heading.hidden = !hasVisibleItem;
        });

        if (noResults) {
            noResults.hidden = visibleItems !== 0;
        }

        if (searchEmpty) {
            searchEmpty.hidden = visibleItems !== 0 || query === "";
        }
    };

    searchInput.addEventListener("input", filterMenu);

    searchInput.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            searchInput.value = "";
            filterMenu();
            searchInput.blur();
        }
    });
});
