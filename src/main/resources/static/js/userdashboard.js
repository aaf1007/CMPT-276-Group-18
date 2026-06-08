//DOM elements
const elements = {
    pmax: document.getElementById('pmax'),
    voc: document.getElementById('voc'),
    isc: document.getElementById('isc'),
    series: document.getElementById('series'),
    parallel: document.getElementById('parallel'),
    totalWatts: document.getElementById('totalWatts'),
    totalVoc: document.getElementById('totalVoc'),
    maxAmps: document.getElementById('maxAmps'),
    panelSelect: document.getElementById('panelSelect'),
    city: document.getElementById('city'),
    country: document.getElementById('country'),
    region: document.getElementById('region')
};

const US_COUNTRY_TOKENS = ['US', 'USA', 'U.S.', 'U.S.A.', 'UNITED STATES', 'UNITED STATES OF AMERICA', 'AMERICA'];
const CA_COUNTRY_TOKENS = ['CA', 'CAN', 'CANADA'];
function requiresRegion(country) {
    const c = (country || '').trim().toUpperCase();
    return US_COUNTRY_TOKENS.includes(c) || CA_COUNTRY_TOKENS.includes(c);
}

const US_STATES = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'District of Columbia', 'Florida', 'Georgia', 'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island', 'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'];

const CA_PROVINCES = ['Alberta', 'British Columbia', 'Manitoba', 'New Brunswick', 'Newfoundland and Labrador', 'Northwest Territories', 'Nova Scotia', 'Nunavut', 'Ontario', 'Prince Edward Island', 'Quebec', 'Saskatchewan', 'Yukon'];

const COUNTRIES = ['Afghanistan', 'Albania', 'Algeria', 'Andorra', 'Angola', 'Antigua and Barbuda', 'Argentina', 'Armenia', 'Australia', 'Austria', 'Azerbaijan', 'Bahamas', 'Bahrain', 'Bangladesh', 'Barbados', 'Belarus', 'Belgium', 'Belize', 'Benin', 'Bhutan', 'Bolivia', 'Bosnia and Herzegovina', 'Botswana', 'Brazil', 'Brunei', 'Bulgaria', 'Burkina Faso', 'Burundi', 'Cambodia', 'Cameroon', 'Canada', 'Cape Verde', 'Central African Republic', 'Chad', 'Chile', 'China', 'Colombia', 'Comoros', 'Congo', 'Costa Rica', 'Croatia', 'Cuba', 'Cyprus', 'Czech Republic', 'Denmark', 'Djibouti', 'Dominica', 'Dominican Republic', 'Ecuador', 'Egypt', 'El Salvador', 'Equatorial Guinea', 'Eritrea', 'Estonia', 'Eswatini', 'Ethiopia', 'Fiji', 'Finland', 'France', 'Gabon', 'Gambia', 'Georgia', 'Germany', 'Ghana', 'Greece', 'Grenada', 'Guatemala', 'Guinea', 'Guinea-Bissau', 'Guyana', 'Haiti', 'Honduras', 'Hungary', 'Iceland', 'India', 'Indonesia', 'Iran', 'Iraq', 'Ireland', 'Israel', 'Italy', 'Ivory Coast', 'Jamaica', 'Japan', 'Jordan', 'Kazakhstan', 'Kenya', 'Kiribati', 'Kosovo', 'Kuwait', 'Kyrgyzstan', 'Laos', 'Latvia', 'Lebanon', 'Lesotho', 'Liberia', 'Libya', 'Liechtenstein', 'Lithuania', 'Luxembourg', 'Madagascar', 'Malawi', 'Malaysia', 'Maldives', 'Mali', 'Malta', 'Marshall Islands', 'Mauritania', 'Mauritius', 'Mexico', 'Micronesia', 'Moldova', 'Monaco', 'Mongolia', 'Montenegro', 'Morocco', 'Mozambique', 'Myanmar', 'Namibia', 'Nauru', 'Nepal', 'Netherlands', 'New Zealand', 'Nicaragua', 'Niger', 'Nigeria', 'North Korea', 'North Macedonia', 'Norway', 'Oman', 'Pakistan', 'Palau', 'Palestine', 'Panama', 'Papua New Guinea', 'Paraguay', 'Peru', 'Philippines', 'Poland', 'Portugal', 'Qatar', 'Romania', 'Russia', 'Rwanda', 'Saint Kitts and Nevis', 'Saint Lucia', 'Saint Vincent and the Grenadines', 'Samoa', 'San Marino', 'Sao Tome and Principe', 'Saudi Arabia', 'Senegal', 'Serbia', 'Seychelles', 'Sierra Leone', 'Singapore', 'Slovakia', 'Slovenia', 'Solomon Islands', 'Somalia', 'South Africa', 'South Korea', 'South Sudan', 'Spain', 'Sri Lanka', 'Sudan', 'Suriname', 'Sweden', 'Switzerland', 'Syria', 'Taiwan', 'Tajikistan', 'Tanzania', 'Thailand', 'Timor-Leste', 'Togo', 'Tonga', 'Trinidad and Tobago', 'Tunisia', 'Turkey', 'Turkmenistan', 'Tuvalu', 'Uganda', 'Ukraine', 'United Arab Emirates', 'United Kingdom', 'United States', 'Uruguay', 'Uzbekistan', 'Vanuatu', 'Vatican City', 'Venezuela', 'Vietnam', 'Yemen', 'Zambia', 'Zimbabwe'];

function regionOptionsFor(country) {
    const c = (country || '').trim().toUpperCase();
    if (US_COUNTRY_TOKENS.includes(c)) return US_STATES;
    if (CA_COUNTRY_TOKENS.includes(c)) return CA_PROVINCES;
    return [];
}

function setupAutocomplete(input, listEl, getItems) {
    let activeIndex = -1;

    function currentMatches() {
        const q = input.value.trim().toLowerCase();
        if (!q) return [];
        const items = getItems();
        const starts = items.filter(i => i.toLowerCase().startsWith(q));
        const contains = items.filter(i => !i.toLowerCase().startsWith(q) && i.toLowerCase().includes(q));
        return starts.concat(contains).slice(0, 5);
    }

    function render() {
        const matches = currentMatches();
        listEl.innerHTML = matches.map(m => '<li class="xan-ac-item" data-value="' + m + '">' + m + '</li>').join('');
        activeIndex = -1;
        listEl.classList.toggle('show', matches.length > 0);
    }

    function choose(value) {
        input.value = value;
        listEl.classList.remove('show');
        input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    input.addEventListener('input', render);
    input.addEventListener('keydown', function (e) {
        const items = Array.from(listEl.querySelectorAll('.xan-ac-item'));
        if (!items.length) return;
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeIndex = (activeIndex + 1) % items.length;
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeIndex = (activeIndex - 1 + items.length) % items.length;
        } else if (e.key === 'Enter' && activeIndex >= 0) {
            e.preventDefault();
            choose(items[activeIndex].dataset.value);
            return;
        } else if (e.key === 'Escape') {
            listEl.classList.remove('show');
            return;
        } else {
            return;
        }
        items.forEach((it, i) => it.classList.toggle('active', i === activeIndex));
    });
    listEl.addEventListener('mousedown', function (e) {
        const item = e.target.closest('.xan-ac-item');
        if (item) {
            e.preventDefault();
            choose(item.dataset.value);
        }
    });
    document.addEventListener('click', function (e) {
        if (e.target !== input && !listEl.contains(e.target)) {
            listEl.classList.remove('show');
        }
    });
}

setupAutocomplete(elements.country, document.getElementById('countryAc'), () => COUNTRIES);
setupAutocomplete(elements.region, document.getElementById('regionAc'), () => regionOptionsFor(elements.country.value));

function setupCityAutocomplete(input, listEl) {
    let activeIndex = -1;
    let debounceTimer = null;

    function choose(value) {
        input.value = value;
        listEl.classList.remove('show');
        input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    async function fetchSuggestions() {
        const city = input.value.trim();
        if (city.length < 2) {
            listEl.classList.remove('show');
            return;
        }
        const country = elements.country.value.trim();
        const region = elements.region.value.trim();
        const url = '/api/weather/cities?city=' + encodeURIComponent(city)
            + '&country=' + encodeURIComponent(country)
            + '&region=' + encodeURIComponent(region);
        try {
            const res = await fetch(url);
            if (!res.ok) {
                listEl.classList.remove('show');
                return;
            }
            const data = await res.json();
            const suggestions = data.slice(0, 5);
            listEl.innerHTML = suggestions.map(s => {
                const label = [s.name, s.admin1, s.country].filter(Boolean).join(', ');
                return '<li class="xan-ac-item" data-value="' + s.name + '">' + label + '</li>';
            }).join('');
            activeIndex = -1;
            listEl.classList.toggle('show', suggestions.length > 0);
        } catch (e) {
            listEl.classList.remove('show');
        }
    }

    input.addEventListener('input', function () {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(fetchSuggestions, 250);
    });
    input.addEventListener('keydown', function (e) {
        const items = Array.from(listEl.querySelectorAll('.xan-ac-item'));
        if (!items.length) return;
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeIndex = (activeIndex + 1) % items.length;
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeIndex = (activeIndex - 1 + items.length) % items.length;
        } else if (e.key === 'Enter' && activeIndex >= 0) {
            e.preventDefault();
            choose(items[activeIndex].dataset.value);
            return;
        } else if (e.key === 'Escape') {
            listEl.classList.remove('show');
            return;
        } else {
            return;
        }
        items.forEach((it, i) => it.classList.toggle('active', i === activeIndex));
    });
    listEl.addEventListener('mousedown', function (e) {
        const item = e.target.closest('.xan-ac-item');
        if (item) {
            e.preventDefault();
            choose(item.dataset.value);
        }
    });
    document.addEventListener('click', function (e) {
        if (e.target !== input && !listEl.contains(e.target)) {
            listEl.classList.remove('show');
        }
    });
}

setupCityAutocomplete(elements.city, document.getElementById('cityAc'));

function updateArrayImage() {
    const series = parseInt(elements.series.value) || 1;
    const parallel = parseInt(elements.parallel.value) || 1;
    const arrayImage = document.getElementById('arrayImage');

    const arrayDisclaimer = document.getElementById('arrayDisclaimer');
    if (series >= 1 && series <= 6 && parallel >= 1 && parallel <= 4) {
        arrayImage.src = `/images/panels/${series}S-${parallel}P-Solar-Panel-Array-Series-Parallel.webp`;
        arrayImage.style.display = 'block';
        arrayDisclaimer.style.display = 'block';
    } else {
        arrayImage.style.display = 'none';
        arrayDisclaimer.style.display = 'none';
    }
}

//calculation function
let tempFactor = 1.2; // default temp factor for corrected Voc

// Calculation function
function updateSummary() {
    const pmax = parseFloat(elements.pmax.value) || 0;
    const voc = parseFloat(elements.voc.value) || 19.83;
    const isc = parseFloat(elements.isc.value) || 10;
    const series = parseInt(elements.series.value) || 1;
    const parallel = parseInt(elements.parallel.value) || 1;
    const battV = document.querySelector('input[name="battV"]:checked').value;

    const totalW = pmax * series * parallel;
    const totalV = (voc * series * tempFactor).toFixed(1);
    const chargeA = (totalW / (battV == 12 ? 14.7 : battV == 24 ? 29.4 : battV == 36 ? 44.1 : 58.8)).toFixed(1);

    elements.totalWatts.innerText = totalW + " W";
    elements.totalVoc.innerText = totalV + " V";
    elements.maxAmps.innerText = chargeA + " A";

    updateArrayImage();

}

// Input listeners
document.querySelectorAll('input').forEach(input => {
    input.addEventListener('input', updateSummary);
});

function resetSubmitButton() {
    const submitBtn = document.getElementById('submitBtn');
    const submitText = document.getElementById('submitText');
    const submitSpinner = document.getElementById('submitSpinner');
    if (submitBtn) submitBtn.disabled = false;
    if (submitText) submitText.classList.remove('d-none');
    if (submitSpinner) submitSpinner.classList.add('d-none');
}

// Form validation before submission
document.querySelector('form[action*="calculator"]').addEventListener('submit', function(e) {
    const pmaxValue = elements.pmax.value.trim();
    const vocValue = elements.voc.value.trim();
    const iscValue = elements.isc.value.trim();
    const cityValue = elements.city.value.trim();
    const countryValue = elements.country.value.trim();
    const regionValue = elements.region.value.trim();
    const manualTempValue = document.getElementById('manualTemp').value.trim();
    const tempUnit = document.getElementById('tempUnit').value;
    const locationInputError = document.getElementById('locationInputError');

    let isValid = true;
    let errorMessage = '';

    if (!pmaxValue || isNaN(parseFloat(pmaxValue)) || parseFloat(pmaxValue) <= 0) {
        isValid = false;
        errorMessage += 'Maximum Power (Pmax) must be a positive number. ';
        elements.pmax.classList.add('is-invalid');
    } else {
        elements.pmax.classList.remove('is-invalid');
    }

    if (!vocValue || isNaN(parseFloat(vocValue)) || parseFloat(vocValue) <= 0) {
        isValid = false;
        errorMessage += 'Open Circuit Voltage (Voc) must be a positive number. ';
        elements.voc.classList.add('is-invalid');
    } else {
        elements.voc.classList.remove('is-invalid');
    }

    if (!iscValue || isNaN(parseFloat(iscValue)) || parseFloat(iscValue) <= 0) {
        isValid = false;
        errorMessage += 'Short Circuit Current (Isc) must be a positive number. ';
        elements.isc.classList.add('is-invalid');
    } else {
        elements.isc.classList.remove('is-invalid');
    }

    // Location: must fill city+country OR manual temp
    const hasLocation = cityValue && countryValue;
    const hasManualTemp = manualTempValue !== '' && !isNaN(parseFloat(manualTempValue));

    if (!hasLocation && !hasManualTemp) {
        isValid = false;
        locationInputError.classList.remove('d-none');
    } else {
        locationInputError.classList.add('d-none');
    }

    if (hasLocation && requiresRegion(countryValue) && !regionValue) {
        isValid = false;
        errorMessage += 'State/Province/Region is required for US and Canada locations. ';
        elements.region.classList.add('is-invalid');
    } else {
        elements.region.classList.remove('is-invalid');
    }

    if (!isValid) {
        e.preventDefault();
        const existingError = document.querySelector('.alert-danger:not(#locationError):not(#locationInputError)');
        if (existingError) existingError.remove();
        if (errorMessage) {
            const errorContainer = document.createElement('div');
            errorContainer.className = 'alert alert-danger mb-4';
            errorContainer.innerHTML = '<strong>Please correct the following errors:</strong><ul>' +
                errorMessage.split('. ').filter(msg => msg.trim()).map(msg => '<li>' + msg + '.</li>').join('') + '</ul>';
            const form = document.querySelector('form');
            form.insertBefore(errorContainer, form.firstChild);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
        return false;
    }

    // Convert manual temp to Celsius and set hidden field
    if (hasManualTemp) {
        let tempC = parseFloat(manualTempValue);
        if (tempUnit === 'F') {
            tempC = (tempC - 32) * 5 / 9;
        }
        document.getElementById('manualTempCelsius').value = tempC.toFixed(2);
    }

    // Show loading spinner
    const submitBtn = document.getElementById('submitBtn');
    const submitText = document.getElementById('submitText');
    const submitSpinner = document.getElementById('submitSpinner');
    submitBtn.disabled = true;
    submitText.classList.add('d-none');
    submitSpinner.classList.remove('d-none');

    // If validation passes, ensure numeric fields have valid values
    elements.pmax.value = parseFloat(pmaxValue);
    elements.voc.value = parseFloat(vocValue);
    elements.isc.value = parseFloat(iscValue);
});

// Function to lock/unlock fields based on panel selection
function lockUnlockFields(isLocked) {
    elements.pmax.readOnly = isLocked;
    elements.voc.readOnly = isLocked;
    elements.isc.readOnly = isLocked;
    
    // Add visual feedback
    if (isLocked) {
        elements.pmax.classList.add('bg-light');
        elements.voc.classList.add('bg-light');
        elements.isc.classList.add('bg-light');
    } else {
        elements.pmax.classList.remove('bg-light');
        elements.voc.classList.remove('bg-light');
        elements.isc.classList.remove('bg-light');
    }
}

// Dropdown auto-fill logic
elements.panelSelect.addEventListener('change', function () {
    const selectedOption = this.options[this.selectedIndex];
    const selectedValue = this.value;
    const pmax = selectedOption.getAttribute('data-pmax');
    const voc = selectedOption.getAttribute('data-voc');
    const isc = selectedOption.getAttribute('data-isc');

    if (pmax && voc && isc) {
        elements.pmax.value = pmax;
        elements.voc.value = voc;
        elements.isc.value = isc;
        
        if (pmax == 0 && voc == 0 && isc == 0) {
            lockUnlockFields(false); // Unlock fields for custom
        } else {
            lockUnlockFields(true); // Lock fields for Xantrex panels
        }
        updateSummary();
    } else if (selectedValue === '') {
        lockUnlockFields(false);  // Unlock fields for empty selection
    }
    
    const image = selectedOption.getAttribute('data-image');
    const panelImage = document.getElementById('panelImage');
    if (image) {
        panelImage.src = image;
        panelImage.style.display = 'block';
    } else {
        panelImage.style.display = 'none';
    }
    updateArrayImage()
});

// Fetch minimum temperature from Open-Meteo via our backend.
// Open-Meteo only needs city name — country is passed through for the form
// but the backend geocoding step resolves lat/lon from the city alone.
async function fetchTemperature() {
    const city = elements.city.value.trim();
    const country = elements.country.value.trim();
    const region = elements.region.value.trim();

    if (!city || !country) return;

    try {
        const response = await fetch(
            `/api/weather/min-temp?city=${encodeURIComponent(city)}&country=${encodeURIComponent(country)}&region=${encodeURIComponent(region)}`
        );

        if (!response.ok) {
            console.error("Weather API error:", response.status, await response.text());
            return;
        }

        const rawText = await response.text();
        console.log("Min Temp raw:", rawText);
        const minTemp = parseFloat(rawText);
 
        if (isNaN(minTemp)) {
            console.error("Could not parse temperature:", rawText);
            return;
        }
 
        if (minTemp < -25) tempFactor = 1.2;
        else if (minTemp < -10) tempFactor = 1.16;
        else if (minTemp < 0) tempFactor = 1.12;
        else tempFactor = 1.2;

        updateSummary();
    } catch (error) {
        console.error("Temperature fetch error:", error);
    }
}

elements.city.addEventListener('change', fetchTemperature);
elements.country.addEventListener('change', fetchTemperature);
elements.region.addEventListener('change', fetchTemperature);


fetchTemperature();
updateSummary();

// Error handling for location 
const cityInput    = document.getElementById('city');
    const countryInput = document.getElementById('country');
    const errorBox     = document.getElementById('locationError');
    const form         = document.querySelector('form[action*="calculator"]');
 
    function showLocationError(message) {
        errorBox.textContent = message;
        errorBox.classList.remove('d-none');
        cityInput.classList.add('is-invalid');
        countryInput.classList.add('is-invalid');
    }
 
    function clearLocationError() {
        errorBox.classList.add('d-none');
        errorBox.textContent = '';
        cityInput.classList.remove('is-invalid');
        countryInput.classList.remove('is-invalid');
    }

    async function validateLocation() {
        const city    = cityInput.value.trim();
        const country = countryInput.value.trim();
        const region  = elements.region.value.trim();
        if (!city || !country) return; 
 
        try {
            const res = await fetch(`/api/weather/min-temp?city=${encodeURIComponent(city)}&country=${encodeURIComponent(country)}&region=${encodeURIComponent(region)}`);
            if (res.ok) {
                clearLocationError();
            } else {
                const msg = await res.text();
                showLocationError(msg);
            }
        } catch (err) {
            clearLocationError();
        }
    }
 
    cityInput.addEventListener('blur', validateLocation);
    countryInput.addEventListener('blur', validateLocation);
 
    // Also block form submission if location is currently invalid
    form.addEventListener('submit', async function (e) {
        const city    = cityInput.value.trim();
        const country = countryInput.value.trim();
        const region  = elements.region.value.trim();
        if (!city || !country) return; 
 
        if (requiresRegion(country) && !region) {
            e.preventDefault();
            resetSubmitButton();
            showLocationError('State/Province/Region is required for US and Canada locations.');
            elements.region.classList.add('is-invalid');
            errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return;
        }

        e.preventDefault();

        try {
            const res = await fetch(`/api/weather/min-temp?city=${encodeURIComponent(city)}&country=${encodeURIComponent(country)}&region=${encodeURIComponent(region)}`);
            if (res.ok) {
                clearLocationError();
                form.submit();
            } else {
                const msg = await res.text();
                resetSubmitButton();
                showLocationError(msg);

                errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } catch (err) {
            form.submit();
        }
    });

const tempUnitSelect = document.getElementById('tempUnit');
const manualTempInput = document.getElementById('manualTemp');
if (tempUnitSelect) {
    tempUnitSelect.addEventListener('change', function () {
        const val = parseFloat(manualTempInput.value);
        if (!isNaN(val)) {
            if (this.value === 'F') {
                manualTempInput.value = ((val * 9 / 5) + 32).toFixed(1);
            } else {
                manualTempInput.value = ((val - 32) * 5 / 9).toFixed(1);
            }
        }
    });
}

document.querySelectorAll('[data-clear]').forEach(btn => {
    btn.addEventListener('click', function () {
        const box = this.getAttribute('data-clear');
        if (box === 'panel') {
            elements.pmax.value = '';
            elements.voc.value = '';
            elements.isc.value = '';
            elements.panelSelect.value = '';
            lockUnlockFields(false);
            const panelImage = document.getElementById('panelImage');
            if (panelImage) panelImage.style.display = 'none';
            elements.pmax.classList.remove('is-invalid');
            elements.voc.classList.remove('is-invalid');
            elements.isc.classList.remove('is-invalid');
        } else if (box === 'array') {
            elements.series.value = 1;
            elements.parallel.value = 1;
            const batt12 = document.getElementById('batt12');
            if (batt12) batt12.checked = true;
        } else if (box === 'location') {
            elements.city.value = '';
            elements.country.value = '';
            elements.region.value = '';
            const manualTemp = document.getElementById('manualTemp');
            const manualTempCelsius = document.getElementById('manualTempCelsius');
            if (manualTemp) manualTemp.value = '';
            if (manualTempCelsius) manualTempCelsius.value = '';
            elements.city.classList.remove('is-invalid');
            elements.country.classList.remove('is-invalid');
            elements.region.classList.remove('is-invalid');
            const locationError = document.getElementById('locationError');
            const locationInputError = document.getElementById('locationInputError');
            if (locationError) locationError.classList.add('d-none');
            if (locationInputError) locationInputError.classList.add('d-none');
            tempFactor = 1.2;
        }
        updateSummary();
    });
});

const resultsSection = document.querySelector('.xan-controller-card');
if (resultsSection) {
    resultsSection.closest('.container').scrollIntoView({ behavior: 'smooth', block: 'start' });
}