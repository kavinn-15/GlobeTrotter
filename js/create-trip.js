document.addEventListener("DOMContentLoaded", function () {


    /* =====================================================
       ELEMENTS
    ====================================================== */

    const tripForm =
        document.getElementById("tripForm");

    const startDate =
        document.getElementById("startDate");

    const endDate =
        document.getElementById("endDate");

    const placeInput =
        document.getElementById("place");

    const suggestionGrid =
        document.getElementById("suggestionGrid");

    const createTripBtn =
        document.getElementById("createTripBtn");


    /* =====================================================
       DATE PLACEHOLDER HANDLING
    ====================================================== */

    function updateDatePlaceholder(input) {

        const placeholder =
            input.parentElement.querySelector(".input-placeholder");

        if (!placeholder) {
            return;
        }

        if (input.value) {

            placeholder.style.display = "none";

        } else {

            placeholder.style.display = "block";

        }

    }


    startDate.addEventListener("change", function () {

        updateDatePlaceholder(startDate);

    });


    endDate.addEventListener("change", function () {

        updateDatePlaceholder(endDate);

    });


    /* =====================================================
       START DATE
    ====================================================== */

    startDate.addEventListener("change", function () {

        if (startDate.value) {

            endDate.min = startDate.value;

        }

    });


    /* =====================================================
       SUGGESTION CLICK
    ====================================================== */

    if (suggestionGrid) {

        suggestionGrid.addEventListener(
            "click",
            function (event) {


                /* -----------------------------------------
                   FAVORITE BUTTON
                ------------------------------------------ */

                const favoriteButton =
                    event.target.closest(".fav-btn");


                if (favoriteButton) {

                    favoriteButton.classList.toggle("active");

                    return;

                }


                /* -----------------------------------------
                   DESTINATION TILE
                ------------------------------------------ */

                const tile =
                    event.target.closest(".tile");


                if (!tile) {
                    return;
                }


                const place =
                    tile.dataset.place;


                if (placeInput && place) {

                    placeInput.value = place;

                    placeInput.focus();

                }

            }
        );

    }


    /* =====================================================
       FORM SUBMIT
    ====================================================== */

    if (tripForm) {

        tripForm.addEventListener(
            "submit",
            function (event) {

                event.preventDefault();


                /* -----------------------------------------
                   VALIDATION
                ------------------------------------------ */

                if (!startDate.value) {

                    alert("Please select a start date.");

                    return;

                }


                if (!placeInput.value.trim()) {

                    alert("Please select a destination.");

                    return;

                }


                if (!endDate.value) {

                    alert("Please select an end date.");

                    return;

                }


                /* -----------------------------------------
                   DATE VALIDATION
                ------------------------------------------ */

                const start =
                    new Date(startDate.value);

                const end =
                    new Date(endDate.value);


                if (end < start) {

                    alert(
                        "End date cannot be before the start date."
                    );

                    return;

                }


                /* -----------------------------------------
                   FORM DATA
                ------------------------------------------ */

                const tripData = {

                    startDate:
                        startDate.value,

                    place:
                        placeInput.value.trim(),

                    endDate:
                        endDate.value

                };


                console.log(
                    "Trip Data:",
                    tripData
                );


                /* -----------------------------------------
                   TEMPORARY DEMO
                ------------------------------------------ */

                alert(
                    "Trip details are ready!\n\n" +
                    "Destination: " +
                    tripData.place +
                    "\nStart: " +
                    tripData.startDate +
                    "\nEnd: " +
                    tripData.endDate
                );


                /*
                 * Later replace the alert with your API:
                 *
                 * fetch("/api/trips", {
                 *
                 *     method: "POST",
                 *
                 *     headers: {
                 *         "Content-Type": "application/json"
                 *     },
                 *
                 *     body: JSON.stringify(tripData)
                 *
                 * })
                 *
                 * .then(response => response.json())
                 *
                 * .then(data => {
                 *
                 *     window.location.href =
                 *         "/itinerary-builder.html";
                 *
                 * });
                 */

            }
        );

    }


    /* =====================================================
       CREATE BUTTON HOVER / CLICK
    ====================================================== */

    if (createTripBtn) {

        createTripBtn.addEventListener(
            "click",
            function () {

                console.log(
                    "Create Trip button clicked"
                );

            }
        );

    }


});