/* ===================================================================
   verify-otp.js — Screen: Verify OTP
   Six separate digit boxes that behave like one field: auto-advances
   on input, supports backspace-to-previous and pasting a full code,
   and combines the digits into the hidden #otpCombined input the real
   Thymeleaf form posts. Also runs the "resend code" cooldown timer.
   Progressive enhancement only — the form still works with JS off,
   just without the nice auto-advance behaviour.
   =================================================================== */
(function () {
    "use strict";

    function initOtpDigits() {
        var wrap = document.getElementById("otpDigits");
        var form = document.getElementById("otpForm");
        var hidden = document.getElementById("otpCombined");
        if (!wrap || !form || !hidden) return;

        var digits = Array.prototype.slice.call(wrap.querySelectorAll(".otp-digit"));

        function syncHidden() {
            hidden.value = digits.map(function (d) { return d.value; }).join("");
        }

        digits.forEach(function (input, idx) {
            input.addEventListener("input", function () {
                input.value = input.value.replace(/[^0-9]/g, "").slice(0, 1);
                input.classList.toggle("filled", input.value.length === 1);
                if (input.value && idx < digits.length - 1) {
                    digits[idx + 1].focus();
                }
                syncHidden();
            });

            input.addEventListener("keydown", function (e) {
                if (e.key === "Backspace" && !input.value && idx > 0) {
                    digits[idx - 1].focus();
                }
            });

            input.addEventListener("paste", function (e) {
                var text = (e.clipboardData || window.clipboardData).getData("text").replace(/[^0-9]/g, "");
                if (!text) return;
                e.preventDefault();
                text.slice(0, digits.length).split("").forEach(function (ch, i) {
                    if (digits[i]) {
                        digits[i].value = ch;
                        digits[i].classList.add("filled");
                    }
                });
                syncHidden();
                var next = digits[Math.min(text.length, digits.length - 1)];
                if (next) next.focus();
            });
        });

        form.addEventListener("submit", function () {
            syncHidden();
        });

        if (digits[0]) digits[0].focus();
    }

    function initResendCountdown() {
        var seconds = document.getElementById("resendSeconds");
        var countdownWrap = document.getElementById("resendCountdown");
        var resendBtn = document.getElementById("resendBtn");
        if (!seconds || !resendBtn) return;

        var remaining = parseInt(seconds.textContent, 10) || 30;

        var timer = setInterval(function () {
            remaining -= 1;
            if (remaining <= 0) {
                clearInterval(timer);
                if (countdownWrap) countdownWrap.style.display = "none";
                resendBtn.style.display = "inline";
                resendBtn.disabled = false;
                return;
            }
            seconds.textContent = remaining;
        }, 1000);
    }

    document.addEventListener("DOMContentLoaded", function () {
        initOtpDigits();
        initResendCountdown();
    });
})();
