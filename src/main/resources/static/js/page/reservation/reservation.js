// 파일 위치: static/js/page/reservation/reservation.js
document.addEventListener('DOMContentLoaded', function() {
    // --- 요소 가져오기 ---
    const calendarInput = document.getElementById('booking-calendar');
    const modelListContainer = document.getElementById('model-list');
    const detailPlaceholder = document.getElementById('detail-placeholder');
    const productDetailsSection = document.getElementById('product-details');
    const finalBookingSection = document.getElementById('final-booking-section');
    const phoneInput = document.getElementById('phone');
    const reservationForm = document.getElementById('final-reservation-form');
    const reserveBtn = document.getElementById('reserve-btn');
    const formInputsForValidation = reservationForm.querySelectorAll('.form-input');

    // --- 초기 상태 설정 ---
    let selectedProductId = null;
    let selectedProductData = {};
    let productSwiper = null;

    // --- intl-tel-input(국제번호) 초기화 ---
    const iti = window.intlTelInput(phoneInput, {
        initialCountry: "auto",
        geoIpLookup: function(callback) {
            fetch("https://ipapi.co/json")
                .then(res => res.json())
                .then(data => callback(data.country_code || "us"))
                .catch(() => callback("us"));
        },
        utilsScript: "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.8/js/utils.js",
        separateDialCode: true,
    });

    // --- URL 파라미터로 초기화 ---
    const urlParams = new URLSearchParams(window.location.search);
    const initialStartDate = urlParams.get('startDate');
    const initialEndDate = urlParams.get('endDate');
    const initialModelName = urlParams.get('modelName');

    const calendar = flatpickr(calendarInput, {
        mode: "range",
        dateFormat: "Y-m-d",
        "locale": "ko",
        minDate: "today",
        inline: true,
        onChange: function(selectedDates) {
            if (selectedDates.length < 2) return;
            const startDate = formatDate(selectedDates[0]);
            const endDate = formatDate(selectedDates[1]);
            hideDetailsAndForm();
            fetchAndRenderModels(startDate, endDate);
        }
    });

    if (initialStartDate && initialEndDate) {
        calendar.setDate([initialStartDate, initialEndDate]);
        fetchAndRenderModels(initialStartDate, initialEndDate);
    }

    // --- 이벤트 리스너 ---
    modelListContainer.addEventListener('click', async function(e) {
        const card = e.target.closest('.model-card');
        if (!card || card.classList.contains('selected')) return;

        modelListContainer.querySelectorAll('.model-card.selected').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');

        selectedProductId = card.dataset.productId;
        await fetchAndRenderProductDetails(selectedProductId);

        finalBookingSection.style.display = 'block';
        updateFinalBookingForm();
        validateForm();
    });

    formInputsForValidation.forEach(input => {
        input.addEventListener('blur', () => {
            input.dataset.touched = 'true';
            validateField(input);
            validateForm();
        });
        input.addEventListener('input', () => {
            if (input.dataset.touched === 'true') {
                validateField(input);
            }
            validateForm();
        });
    });
    phoneInput.addEventListener("countrychange", () => {
        if (phoneInput.dataset.touched === 'true') {
            validateField(phoneInput);
        }
        validateForm();
    });

    reservationForm.addEventListener('submit', function(e) {
        const isAllValid = Array.from(formInputsForValidation).every(input => validateField(input));
        if (!isAllValid) {
            e.preventDefault();
            const firstInvalidField = reservationForm.querySelector('.invalid');
            if (firstInvalidField) firstInvalidField.focus();
            return;
        }
        reservationForm.querySelector('input[name="phone"]').value = iti.getNumber();

        const pickupTimeEl = document.getElementById('pickupTime');
        const returnTimeEl = document.getElementById('returnTime');
        if (pickupTimeEl && returnTimeEl) {
            reservationForm.querySelector('input[name="pickupTime"]').value = pickupTimeEl.value;
            reservationForm.querySelector('input[name="returnTime"]').value = returnTimeEl.value;
        }
    });

    // --- 함수들 ---
    async function fetchAndRenderModels(startDate, endDate) {
        try {
            const response = await fetch(`/api/products/available?startDate=${startDate}&endDate=${endDate}`);
            const models = await response.json();

            modelListContainer.innerHTML = '';
            if (models.length > 0) {
                models.forEach(model => {
                    const cardHtml = `<div class="model-card" data-product-id="${model.id}" data-model-name="${model.name}"><img src="${model.imageUrl}" alt="${model.name}" class="model-img"><div class="model-info"><h4>${model.name}</h4><p>${model.availableCount}대 가능</p></div></div>`;
                    modelListContainer.insertAdjacentHTML('beforeend', cardHtml);
                });
                if (initialModelName) {
                    const cardToSelect = modelListContainer.querySelector(`[data-model-name="${initialModelName}"]`);
                    if (cardToSelect) cardToSelect.click();
                }
            } else {
                modelListContainer.innerHTML = '<p class="placeholder">선택하신 날짜에 이용 가능한 모델이 없습니다.</p>';
            }
        } catch (error) {
            console.error("Failed to fetch models:", error);
            modelListContainer.innerHTML = '<p class="placeholder">오류가 발생했습니다.</p>';
        }
    }

    async function fetchAndRenderProductDetails(productId) {
        try {
            detailPlaceholder.style.display = 'none';
            productDetailsSection.style.display = 'block';
            const response = await fetch(`/api/products/${productId}`);
            selectedProductData = await response.json();
            document.getElementById('product-title').textContent = selectedProductData.name;
            const imageSliderWrapper = document.querySelector('.product-image-swiper .swiper-wrapper');
            if (selectedProductData.imageUrls && selectedProductData.imageUrls.length > 0) {
                imageSliderWrapper.innerHTML = selectedProductData.imageUrls.map(url => `<div class="swiper-slide"><img src="${url}" alt="${selectedProductData.name}"></div>`).join('');
            } else {
                imageSliderWrapper.innerHTML = `<div class="swiper-slide"><img src="/images/product/default-bike.png" alt="${selectedProductData.name}"></div>`;
            }
            if (productSwiper) { productSwiper.destroy(true, true); }
            productSwiper = new Swiper('.product-image-swiper', {
                loop: selectedProductData.imageUrls && selectedProductData.imageUrls.length > 1,
                navigation: { nextEl: '.product-swiper-next', prevEl: '.product-swiper-prev' },
                pagination: { el: '.product-swiper-pagination', clickable: true },
            });
            renderTabs(selectedProductData);
        } catch (error) {
            console.error("Failed to load product details:", error);
            productDetailsSection.innerHTML = '<h2>상세 정보를 불러오는데 실패했습니다.</h2>';
        }
    }

    function renderTabs(data) {
        document.getElementById('tab-content-desc').textContent = data.description;
        document.getElementById('tab-content-included').textContent = data.includedItems + '\n\n--- 불포함 사항 ---\n' + data.notIncludedItems;
        document.getElementById('tab-content-guide').textContent = data.usageGuide;
        document.getElementById('tab-content-cancellation').textContent = data.cancellationPolicy;
        document.querySelectorAll('.tab-link').forEach(tab => {
            tab.addEventListener('click', () => {
                document.querySelectorAll('.tab-link').forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
                document.getElementById(tab.dataset.tab).classList.add('active');
            });
        });
    }

    function updateFinalBookingForm() {
        const dates = calendar.selectedDates;
        if (dates.length < 2 || !selectedProductId) return;
        const startDate = formatDate(dates[0]);
        const endDate = formatDate(dates[1]);
        const rentalDays = (new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24) + 1;
        fetch(`/api/products/${selectedProductId}/availability?date=${startDate}`)
            .then(res => res.json())
            .then(data => {
                const dailyPrice = data.price;
                const totalPrice = dailyPrice * rentalDays;
                const bookingOptionsEl = document.getElementById('booking-options');
                bookingOptionsEl.innerHTML = `<p><strong>모델:</strong> ${selectedProductData.name}</p><p><strong>기간:</strong> ${startDate} ~ ${endDate} (${rentalDays}일)</p><div class="price-details"><div class="price-row"><span>1일 대여료</span><span>${dailyPrice.toLocaleString()} LAK</span></div><div class="price-row"><span>대여일</span><span>x ${rentalDays}일</span></div><div class="price-row total"><span>총 예상 금액</span><span>${totalPrice.toLocaleString()} LAK</span></div></div>`;
            });
        reservationForm.querySelector('input[name="modelName"]').value = selectedProductData.name;
        reservationForm.querySelector('input[name="startDate"]').value = startDate;
        reservationForm.querySelector('input[name="endDate"]').value = endDate;
    }

    function hideDetailsAndForm() {
        productDetailsSection.style.display = 'none';
        finalBookingSection.style.display = 'none';
        detailPlaceholder.style.display = 'flex';
    }

    function validateField(input) {
        const errorElement = document.getElementById(`${input.id}-error`);
        let message = '';
        const value = input.value.trim();
        switch (input.id) {
            case 'lastName':
            case 'firstName':
                if (value === '') message = '필수 입력 항목입니다.';
                break;
            case 'email':
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (value === '') message = '필수 입력 항목입니다.';
                else if (!emailRegex.test(value)) message = '올바른 이메일 형식이 아닙니다.';
                break;
            case 'phone':
                if (value === '') message = '필수 입력 항목입니다.';
                else if (!iti.isValidNumber()) {
                    const errorCode = iti.getValidationError();
                    const errorMap = { 1: "잘못된 국가 코드", 2: "번호가 너무 짧음", 3: "번호가 너무 김", 4: "유효하지 않은 번호" };
                    message = errorMap[errorCode] || '유효하지 않은 번호';
                }
                break;
            case 'passportNumber':
                if (value === '') message = '필수 입력 항목입니다.';
                else if (value.length < 5) message = '여권 번호가 너무 짧습니다.';
                break;
            case 'pickupTime':
            case 'returnTime':
                if (value === '') message = '시간을 선택해주세요.';
                break;
        }
        if (message) {
            if (errorElement) { errorElement.textContent = message; errorElement.classList.add('visible'); }
            input.classList.add('invalid');
            return false;
        } else {
            if (errorElement) { errorElement.classList.remove('visible'); }
            input.classList.remove('invalid');
            return true;
        }
    }

    function validateAllFields() {
        return Array.from(formInputsForValidation).every(input => validateField(input));
    }

    function validateForm() {
        const isFormValid = Array.from(formInputsForValidation).every(input => {
            return input.value.trim() !== '' && !input.classList.contains('invalid');
        });
        reserveBtn.disabled = !isFormValid;
    }

    function formatDate(date) {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = ('0' + (d.getMonth() + 1)).slice(-2);
        const day = ('0' + d.getDate()).slice(-2);
        return `${year}-${month}-${day}`;
    }
});
