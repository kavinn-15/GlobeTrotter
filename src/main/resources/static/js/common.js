/* ===================================================================
   common.js — GlobalTrotter shared interactions
   Progressive-enhancement only: never blocks or replaces the real
   Thymeleaf form submissions / server navigation already wired up
   in the templates. Safe to include on every page.
   =================================================================== */
(function () {
    "use strict";

    /* ---------- 0. inject animated sky decor (once per page) ---------- */
    function injectSky() {
        if (document.querySelector(".sky-decor")) return;
        var wrap = document.createElement("div");
        wrap.className = "sky-decor";
        wrap.setAttribute("aria-hidden", "true");
        wrap.innerHTML =
            '<svg class="cloud c1" viewBox="0 0 100 34" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M20 26c-8 0-14-6-14-13S12 0 20 0c3 0 6 1 8 3 3-4 8-6 13-6 9 0 16 6 18 14 6 1 11 6 11 12 0 7-6 13-13 13H20z" opacity="0.5" style="color:#dfeeff"/></svg>' +
            '<svg class="cloud c2" viewBox="0 0 100 34" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M20 26c-8 0-14-6-14-13S12 0 20 0c3 0 6 1 8 3 3-4 8-6 13-6 9 0 16 6 18 14 6 1 11 6 11 12 0 7-6 13-13 13H20z" opacity="0.4" style="color:#dfeeff"/></svg>' +
            '<svg class="cloud c3" viewBox="0 0 100 34" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M20 26c-8 0-14-6-14-13S12 0 20 0c3 0 6 1 8 3 3-4 8-6 13-6 9 0 16 6 18 14 6 1 11 6 11 12 0 7-6 13-13 13H20z" opacity="0.3" style="color:#dfeeff"/></svg>' +
            '<svg class="cloud c4" viewBox="0 0 100 34" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M20 26c-8 0-14-6-14-13S12 0 20 0c3 0 6 1 8 3 3-4 8-6 13-6 9 0 16 6 18 14 6 1 11 6 11 12 0 7-6 13-13 13H20z" opacity="0.4" style="color:#dfeeff"/></svg>' +
            '<svg class="plane" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" fill="currentColor"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2.5 1.8V22l3.5-1 3.5 1v-1.2L13 19v-5.5l8 2.5z"/></svg>' +
            '<div class="stamp s1"></div><div class="stamp s2"></div>' +
            '<svg class="mountains" viewBox="0 0 400 60" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M0 60 L60 15 L100 40 L150 5 L210 45 L260 20 L320 50 L400 25 L400 60 Z"/></svg>';
        document.body.insertBefore(wrap, document.body.firstChild);
    }

    /* ---------- 1. theme (dark/light) + motion toggle ---------- */
    function initToggles() {
        if (document.querySelector(".fx-toggle-group")) return;
        var group = document.createElement("div");
        group.className = "fx-toggle-group";

        var savedTheme = localStorage.getItem("gt-theme") || "dark";
        document.documentElement.setAttribute("data-theme", savedTheme);

        var themeBtn = document.createElement("button");
        themeBtn.type = "button";
        themeBtn.className = "fx-toggle";
        themeBtn.title = "Toggle day / night theme";
        themeBtn.setAttribute("aria-label", "Toggle day / night theme");
        themeBtn.textContent = savedTheme === "light" ? "\u2600\uFE0F" : "\uD83C\uDF19";

        themeBtn.addEventListener("click", function () {
            var cur = document.documentElement.getAttribute("data-theme") === "light" ? "dark" : "light";
            document.documentElement.setAttribute("data-theme", cur);
            localStorage.setItem("gt-theme", cur);
            themeBtn.textContent = cur === "light" ? "\u2600\uFE0F" : "\uD83C\uDF19";
            toast(cur === "light" ? "Daytime mode on \u2600\uFE0F" : "Night flight mode on \uD83C\uDF19");
        });

        group.appendChild(themeBtn);
        document.body.appendChild(group);
    }

    /* ---------- 2. toast helper (exposed globally) ---------- */
    function toast(msg) {
        var stack = document.querySelector(".toast-stack");
        if (!stack) {
            stack = document.createElement("div");
            stack.className = "toast-stack";
            document.body.appendChild(stack);
        }
        var t = document.createElement("div");
        t.className = "toast";
        t.textContent = msg;
        stack.appendChild(t);
        setTimeout(function () {
            t.classList.add("leaving");
            setTimeout(function () { t.remove(); }, 300);
        }, 2400);
    }
    window.gtToast = toast;

    /* ---------- 3. ripple effect on buttons ---------- */
    function initRipples() {
        document.addEventListener("click", function (e) {
            var btn = e.target.closest(".btn, .btn-outline");
            if (!btn) return;
            var rect = btn.getBoundingClientRect();
            var size = Math.max(rect.width, rect.height);
            var ripple = document.createElement("span");
            ripple.className = "ripple";
            ripple.style.width = ripple.style.height = size + "px";
            ripple.style.left = (e.clientX - rect.left - size / 2) + "px";
            ripple.style.top = (e.clientY - rect.top - size / 2) + "px";
            btn.style.position = btn.style.position || "relative";
            btn.appendChild(ripple);
            setTimeout(function () { ripple.remove(); }, 650);
        });
    }

    /* ---------- 4. 3D pointer tilt on cards / tiles ---------- */
    function initTilt() {
        var selector = ".tile, .card, .result-card, .trip-card, .mini-trip-card, " +
            ".community-card, .admin-stat, .itinerary-section, .calendar-cell-trip";
        document.querySelectorAll(selector).forEach(function (el) {
            el.classList.add("tilt");
            var maxTilt = 8;
            el.addEventListener("mousemove", function (e) {
                var r = el.getBoundingClientRect();
                var px = (e.clientX - r.left) / r.width - 0.5;
                var py = (e.clientY - r.top) / r.height - 0.5;
                el.style.transform =
                    "perspective(700px) rotateX(" + (-py * maxTilt) + "deg) rotateY(" + (px * maxTilt) + "deg) translateY(-4px)";
            });
            el.addEventListener("mouseleave", function () {
                el.style.transform = "";
            });
        });
    }

    /* ---------- 5. reveal-on-scroll ---------- */
    function initReveal() {
        var targets = document.querySelectorAll(
            ".tile, .card, .result-card, .trip-card, .mini-trip-card, .community-card, " +
            ".admin-stat, .bar-row, .day-block, .itinerary-section, .calendar-card, .form-panel"
        );
        if (!("IntersectionObserver" in window) || !targets.length) {
            targets.forEach(function (t) { t.classList.add("in-view"); });
            return;
        }
        var io = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add("in-view");
                    io.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });
        targets.forEach(function (t) {
            t.classList.add("reveal");
            io.observe(t);
        });
    }

    /* ---------- 6. favorite / bookmark star toggles ---------- */
    function favKey(el) {
        return "gt-fav-" + (el.dataset.favKey || el.textContent.trim().slice(0, 40));
    }
    function initFavorites() {
        document.querySelectorAll(".trip-card, .result-card, .community-card").forEach(function (card) {
            if (card.querySelector(".fav-btn")) return;
            var btn = document.createElement("button");
            btn.type = "button";
            btn.className = "fav-btn";
            btn.title = "Save to favorites";
            btn.setAttribute("aria-label", "Save to favorites");
            btn.innerHTML = "\u2605";

            var key = "gt-fav-" + btoa(unescape(encodeURIComponent(
                (card.dataset.favKey || card.textContent || "").trim().slice(0, 60)
            ))).slice(0, 40);

            if (localStorage.getItem(key) === "1") btn.classList.add("is-fav");

            btn.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();
                var nowFav = !btn.classList.contains("is-fav");
                btn.classList.toggle("is-fav", nowFav);
                btn.classList.remove("pulse");
                void btn.offsetWidth;
                btn.classList.add("pulse");
                localStorage.setItem(key, nowFav ? "1" : "0");
                toast(nowFav ? "Saved to your favorites \u2605" : "Removed from favorites");
            });

            if (card.tagName === "A") {
                card.style.display = "flex";
                card.style.alignItems = "center";
                card.style.justifyContent = "space-between";
                card.style.gap = "10px";
                var span = document.createElement("span");
                span.style.flex = "1";
                span.innerHTML = card.innerHTML;
                card.innerHTML = "";
                card.appendChild(span);
                card.appendChild(btn);
            } else {
                card.appendChild(btn);
            }
        });
    }

    /* ---------- 7. live client-side filter for search toolbars ---------- */
    function initLiveFilter() {
        document.querySelectorAll(".toolbar .search-field").forEach(function (input) {
            var frame = input.closest(".app-frame");
            if (!frame) return;
            var cardSelector = ".tile, .result-card, .trip-card, .community-card, .mini-trip-card";
            input.addEventListener("input", function () {
                var q = input.value.trim().toLowerCase();
                var cards = frame.querySelectorAll(cardSelector);
                var anyHidden = false;
                cards.forEach(function (c) {
                    if (!q) { c.style.display = ""; return; }
                    var match = c.textContent.toLowerCase().indexOf(q) !== -1;
                    c.style.display = match ? "" : "none";
                    if (!match) anyHidden = true;
                });
                void anyHidden;
            });
        });
    }

    /* ---------- 8. password visibility toggle ---------- */
    function initPasswordToggles() {
        document.querySelectorAll('input[type="password"]').forEach(function (input) {
            if (input.parentElement.classList.contains("field-wrap")) return;
            var wrap = document.createElement("div");
            wrap.className = "field-wrap";
            input.parentNode.insertBefore(wrap, input);
            wrap.appendChild(input);

            var toggle = document.createElement("button");
            toggle.type = "button";
            toggle.className = "field-toggle";
            toggle.textContent = "\uD83D\uDC41";
            toggle.setAttribute("aria-label", "Show password");
            toggle.addEventListener("click", function () {
                var showing = input.type === "text";
                input.type = showing ? "password" : "text";
                toggle.textContent = showing ? "\uD83D\uDC41" : "\uD83D\uDE48";
            });
            wrap.appendChild(toggle);
        });
    }

    /* ---------- 9. animate admin bar / trend charts on scroll into view ---------- */
    function initChartAnimation() {
        var bars = document.querySelectorAll(".bar-fill, .trend-bar");
        if (!bars.length) return;
        bars.forEach(function (bar) {
            var target = bar.style.width || bar.style.height;
            if (!target) return;
            var isHeight = !!bar.style.height;
            bar.style[isHeight ? "height" : "width"] = "0%";
            bar.style.transition = "width 0.9s cubic-bezier(.2,.8,.3,1), height 0.9s cubic-bezier(.2,.8,.3,1)";
            var io = new IntersectionObserver(function (entries, obs) {
                entries.forEach(function (entry) {
                    if (entry.isIntersecting) {
                        setTimeout(function () {
                            bar.style[isHeight ? "height" : "width"] = target;
                        }, 80);
                        obs.disconnect();
                    }
                });
            }, { threshold: 0.2 });
            io.observe(bar);
        });
    }

    /* ---------- 10. auto-resize share textarea + char counter ---------- */
    function initTextareas() {
        document.querySelectorAll("textarea.field").forEach(function (ta) {
            ta.addEventListener("input", function () {
                ta.style.height = "auto";
                ta.style.height = Math.min(ta.scrollHeight, 240) + "px";
            });
        });
    }

    /* ---------- 11. back-to-top button ---------- */
    function initBackToTop() {
        var btn = document.createElement("button");
        btn.type = "button";
        btn.className = "back-to-top";
        btn.innerHTML = "\u2191";
        btn.setAttribute("aria-label", "Back to top");
        btn.addEventListener("click", function () {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
        document.body.appendChild(btn);
        window.addEventListener("scroll", function () {
            btn.classList.toggle("show", window.scrollY > 400);
        });
    }

    /* ---------- 12. copy-link buttons ---------- */
    function initCopyLinks() {
        document.querySelectorAll("[data-copy-link]").forEach(function (btn) {
            btn.addEventListener("click", function (e) {
                e.preventDefault();
                var url = window.location.href;
                if (navigator.clipboard) {
                    navigator.clipboard.writeText(url).then(function () {
                        toast("Trip link copied \uD83D\uDD17");
                    });
                } else {
                    toast("Copy this page's URL to share your trip");
                }
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        injectSky();
        initToggles();
        initRipples();
        initTilt();
        initReveal();
        initFavorites();
        initLiveFilter();
        initPasswordToggles();
        initChartAnimation();
        initTextareas();
        initBackToTop();
        initCopyLinks();
    });
})();
