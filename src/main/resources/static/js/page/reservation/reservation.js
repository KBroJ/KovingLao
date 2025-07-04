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
    const formInputsForValidation = reservationForm.querySelectorAll('#lastName, #firstName, #email, #phone, #passportNumber');

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
        input.addEventListener('input', validateForm);
    });
    phoneInput.addEventListener("countrychange", validateForm);

    reservationForm.addEventListener('submit', function(e) {
        if (!validateAllFields()) {
            e.preventDefault();
            return;
        }
        reservationForm.querySelector('input[name="phone"]').value = iti.getNumber();
    });

    // --- 함수들 ---
    async function fetchAndRenderModels(startDate, endDate) {
        try {
            const response = await fetch(`/api/products/available?startDate=${startDate}&endDate=${endDate}`);
            const models = await response.json();

            modelListContainer.innerHTML = '';
            if (models.length > 0) {
                models.forEach(model => {
                    const cardHtml = `
                        <div class="model-card" data-product-id="${model.id}" data-model-name="${model.name}">
                            <img src="${model.imageUrl}" alt="${model.name}" class="model-img">
                            <div class="model-info">
                                <h4>${model.name}</h4>
                                <p>${model.availableCount}대 가능</p>
                            </div>
                        </div>`;
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
                imageSliderWrapper.innerHTML = selectedProductData.imageUrls.map(url =>
                    `<div class="swiper-slide"><img src="${url}" alt="${selectedProductData.name}"></div>`
                ).join('');
            } else {
                imageSliderWrapper.innerHTML = `<div class="swiper-slide"><img src="/images/product/default-bike.png" alt="${selectedProductData.name}"></div>`;
            }

            if (productSwiper) {
                productSwiper.destroy(true, true);
            }
            productSwiper = new Swiper('.product-image-swiper', {
                loop: selectedProductData.imageUrls && selectedProductData.imageUrls.length > 1,
                navigation: {
                    nextEl: '.product-swiper-next',
                    prevEl: '.product-swiper-prev'
                },
                pagination: {
                    el: '.product-swiper-pagination',
                    clickable: true,
                },
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

        document.getElementById('booking-options').innerHTML = `
            <p><strong>모델:</strong> ${selectedProductData.name}</p>
            <p><strong>기간:</strong> ${startDate} ~ ${endDate}</p>
        `;

        reservationForm.querySelector('input[name="modelName"]').value = selectedProductData.name;
        reservationForm.querySelector('input[name="startDate"]').value = startDate;
        reservationForm.querySelector('input[name="endDate"]').value = endDate;
    }

    function hideDetailsAndForm() {
        productDetailsSection.style.display = 'none';
        finalBookingSection.style.display = 'none';
        detailPlaceholder.style.display = 'flex';
    }

    function validateAllFields() {
        const firstName = document.getElementById('firstName').value.trim();
        const lastName = document.getElementById('lastName').value.trim();
        const email = document.getElementById('email').value.trim();
        const passportNumber = document.getElementById('passportNumber').value.trim();

        if (!firstName || !lastName) {
            alert("이름과 성을 모두 입력해주세요.");
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert("올바른 이메일 주소 형식을 입력해주세요.");
            return false;
        }

        if (passportNumber.length < 5) {
            alert("여권 번호가 너무 짧습니다. 다시 확인해주세요.");
            return false;
        }

        if (!iti.isValidNumber()) {
            alert("유효하지 않은 전화번호입니다. 다시 확인해주세요.");
            return false;
        }
        return true;
    }

    function validateForm() {
        let isValid = true;
        // [수정] 올바른 변수 이름 사용
        formInputsForValidation.forEach(input => {
            if (!input.value.trim()) {
                isValid = false;
            }
        });

        if (!iti.isValidNumber()) {
            isValid = false;
        }
        reserveBtn.disabled = !isValid;
    }

    function formatDate(date) {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = ('0' + (d.getMonth() + 1)).slice(-2);
        const day = ('0' + d.getDate()).slice(-2);
        return `${year}-${month}-${day}`;
    }
});
