document.addEventListener('DOMContentLoaded', function() {
    // --- 초기화 및 요소 가져오기 ---
    const productId = window.location.pathname.split('/products/')[1];
    if (!productId) return;

    const imageSliderWrapper = document.querySelector('#product-image-slider .swiper-wrapper');
    const productTitle = document.getElementById('product-title');
    const productDescription = document.getElementById('product-description');
    const descriptionMoreBtn = document.getElementById('description-more-btn');
    const tabs = document.querySelectorAll('.tab-link');
    const tabContents = document.querySelectorAll('.tab-content');
    const bookingOptions = document.getElementById('booking-options');
    const totalPriceEl = document.getElementById('total-price');
    const reserveBtn = document.getElementById('reserve-btn');

    let imageSwiper;
    let selectedDatePrice = 0;

    // --- 데이터 로딩 및 렌더링 ---
    async function loadProductDetails() {
        try {
            const response = await fetch(`/api/products/${productId}`);
            const data = await response.json();
            renderProductDetails(data);
        } catch (error) {
            console.error("Failed to load product details:", error);
            document.querySelector('.product-content').innerHTML = '<h2>상품 정보를 불러오는데 실패했습니다.</h2>';
        }
    }

    function renderProductDetails(data) {
        // 기본 정보
        productTitle.textContent = data.name;
        productDescription.textContent = data.description;

        // 이미지 슬라이더
        imageSliderWrapper.innerHTML = data.imageUrls.map(url => `
            <div class="swiper-slide"><img src="${url}" alt="${data.name}"></div>
        `).join('');
        imageSwiper = new Swiper('.product-image-swiper', {
            loop: data.imageUrls.length > 1,
            pagination: { el: '.swiper-pagination', clickable: true },
            navigation: { nextEl: '.swiper-button-next', prevEl: '.swiper-button-prev' },
        });

        // 설명 더보기 버튼
        if (productDescription.scrollHeight > productDescription.clientHeight) {
            descriptionMoreBtn.style.display = 'block';
        }

        // 탭 콘텐츠
        document.getElementById('tab-content-included').textContent = data.includedItems + '\n\n--- 불포함 사항 ---\n' + data.notIncludedItems;
        document.getElementById('tab-content-guide').textContent = data.usageGuide;
        document.getElementById('tab-content-cancellation').textContent = data.cancellationPolicy;
    }

    // --- 이벤트 리스너 ---
    // 설명 더보기/접기
    descriptionMoreBtn.addEventListener('click', () => {
        productDescription.classList.toggle('expanded');
        descriptionMoreBtn.textContent = productDescription.classList.contains('expanded') ? '접기' : '더보기';
    });

    // 탭 메뉴
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            tabContents.forEach(c => c.classList.remove('active'));
            document.getElementById(tab.dataset.tab).classList.add('active');
        });
    });

    // 날짜 선택(Flatpickr)
    const calendar = flatpickr("#booking-calendar", {
        "locale": "ko",
        minDate: "today",
        onChange: async function(selectedDates) {
            if (selectedDates.length === 0) return;
            const date = formatDate(selectedDates[0]);
            try {
                const response = await fetch(`/api/products/${productId}/availability?date=${date}`);
                const data = await response.json();
                renderBookingOptions(data);
            } catch (error) {
                console.error("Failed to fetch availability:", error);
                bookingOptions.innerHTML = '<p>선택하신 날짜의 정보를 불러올 수 없습니다.</p>';
            }
        }
    });

    function renderBookingOptions(data) {
        if (data.availableCount <= 0) {
            bookingOptions.innerHTML = '<p class="placeholder">선택하신 날짜는 예약이 마감되었습니다.</p>';
            reserveBtn.disabled = true;
            return;
        }
        selectedDatePrice = data.price;
        bookingOptions.innerHTML = `
            <div class="booking-option-item">
                <span>${productTitle.textContent}</span>
                <strong>${data.price.toLocaleString()} LAK / 1일</strong>
            </div>
            <p><strong>잔여 수량:</strong> ${data.availableCount}대</p>
        `;
        totalPriceEl.textContent = `${data.price.toLocaleString()} LAK`;
        reserveBtn.disabled = false;
    }

    // --- 헬퍼 함수 ---
    function formatDate(date) {
        const d = new Date(date),
              year = d.getFullYear(),
              month = ('0' + (d.getMonth() + 1)).slice(-2),
              day = ('0' + d.getDate()).slice(-2);
        return [year, month, day].join('-');
    }

    // --- 초기 실행 ---
    loadProductDetails();
});