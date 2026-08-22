// ===== Elements =====
const tripForm       = document.getElementById('tripForm');
const placeInput     = document.querySelector('input[name="place"]');
const suggestionGrid = document.querySelector('.suggestion-grid');

// ===== Clicking a suggestion tile fills the "Select a Place" field =====
if (suggestionGrid && placeInput) {
  suggestionGrid.addEventListener('click', (e) => {
    // don't trigger place-select when the heart/favorite icon is clicked
    if (e.target.closest('.fav-btn')) return;

    const tile = e.target.closest('.tile');
    if (!tile) return;

    const cityName = tile.querySelector('h3').textContent;
    placeInput.value = cityName;
  });

  // ===== Favorite (heart) toggle =====
  suggestionGrid.addEventListener('click', (e) => {
    const favBtn = e.target.closest('.fav-btn');
    if (!favBtn) return;
    favBtn.classList.toggle('active');
  });
}

// ===== Basic client-side date guard (server should still validate) =====
if (tripForm) {
  tripForm.addEventListener('submit', (e) => {
    const startDate = document.getElementById('startDate');
    const endDate = document.getElementById('endDate');

    if (startDate.value && endDate.value && new Date(endDate.value) < new Date(startDate.value)) {
      e.preventDefault();
      alert('End date cannot be before the start date.');
    }
  });
}