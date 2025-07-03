// DOM이 완전히 로드된 후 스크립트 실행
document.addEventListener('DOMContentLoaded', function() {

    console.log("라오스 모토 웹사이트가 로드되었습니다.");

    // 1. 배너 슬라이더 초기화
    const bannerSwiper = new Swiper('.banner-swiper', {
        slidesPerView: 1,       // 배너는 항상 1개씩 보여줌
        centeredSlides: true,   // 슬라이드가 1개일 때도 가운데 정렬
        loop: true,             // 무한 반복
        autoplay: {
            delay: 5000, // 5초마다 다음 슬라이드로 전환
            disableOnInteraction: false, // 사용자가 조작한 후에도 자동 재생 유지
        },
        pagination: {
            el: '.banner-swiper-pagination', // 고유 클래스 지정
            clickable: true,
        },
    });

    // 2. 지점 소개 슬라이더 초기화
    const introSwiper = new Swiper('.intro-swiper', {
        slidesPerView: 1,       // 항상 1개씩 보여줌
        centeredSlides: true,
        loop: true,
        autoplay: {
            delay: 5000,
            disableOnInteraction: false,
        },
        pagination: {
            el: '.intro-swiper-pagination', // 고유 클래스 지정
            clickable: true,
        },
    });

    // 3. 상품 목록 슬라이더 초기화
    const bikeSwiper = new Swiper('.bike-swiper', {
        slidesPerView: 'auto',    // 여러 개를 보여주기 위해 auto로 설정
        centeredSlides: true,
        spaceBetween: 20,
        loop: false, // 동적 데이터 로딩 시에는 loop false 권장
        pagination: {
            el: '.bike-swiper-pagination', // 고유 클래스 지정
            clickable: true,
        },
        navigation: {
            nextEl: '.bike-swiper-button-next', // 고유 클래스 지정
            prevEl: '.bike-swiper-button-prev', // 고유 클래스 지정
        },
    });


    // Flatpickr (날짜 선택) 초기화
    const rentalDurationDisplay = document.getElementById('rental-duration-display');

    const datePicker = flatpickr("#date-range", {
        mode: "range",
        dateFormat: "Y-m-d",
        minDate: "today",
        "locale": "ko",
        onClose: function(selectedDates) {
            if (selectedDates.length < 2) {
                rentalDurationDisplay.textContent = '';
                return;
            }
            const startDate = selectedDates[0];
            const endDate = selectedDates[1];
            const diffDays = Math.round((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
            rentalDurationDisplay.textContent = formatDuration(diffDays);
        }
    });

    // 일/월 단위로 기간을 예쁘게 포맷하는 함수
    function formatDuration(days) {
        if (days <= 0) return '';
        const months = Math.floor(days / 30);
        const remainingDays = days % 30;
        let result = '총 ';
        if (months > 0) {
            result += `${months}개월 `;
        }
        if (remainingDays > 0) {
            result += `${remainingDays}일`;
        }
        return result.trim();
    }

    const searchForm = document.getElementById('bike-search-form');
    const noResultsMessage = document.getElementById('no-results-message');

    // 검색 폼 제출 이벤트 리스너
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const selectedDates = datePicker.selectedDates;
        if (selectedDates.length < 2) {
            alert('대여 날짜와 반납 날짜를 모두 선택해주세요.');
            return;
        }
        const startDate = formatDate(selectedDates[0]);
        const endDate = formatDate(selectedDates[1]);
        fetchAvailableBikes(startDate, endDate);
    });

    // API 호출 및 슬라이드 업데이트 함수
    async function fetchAvailableBikes(startDate, endDate) {

        const bikeListResult = document.getElementById('bike-list-result');
        const errorMessage = document.getElementById('error-message');

        try {
            const response = await fetch(`/api/products/available?startDate=${startDate}&endDate=${endDate}`);
            if (!response.ok) {
                // HTTP 상태 코드가 2xx가 아닌 경우
                throw new Error('서버에서 응답을 받지 못했습니다.');
            }
            const availableModels = await response.json();
            updateBikeList(availableModels);

            // 성공 시 에러 메시지 숨기기
            errorMessage.style.display = 'none';

        } catch (error) {
            console.error(error);

            bikeSwiper.el.style.display = 'none';
            noResultsMessage.style.display = 'none';
            errorMessage.style.display = 'block';

        }
    }

    // 예약가능 목록 Swiper 슬라이드 동적 업데이트 함수
    function updateBikeList(models) {

        bikeSwiper.removeAllSlides();

        if (models.length === 0) {
            noResultsMessage.style.display = 'block';
            bikeSwiper.el.style.display = 'none';
        } else {
            noResultsMessage.style.display = 'none';
            bikeSwiper.el.style.display = 'block';

            const selectedDates = datePicker.selectedDates;
            const startDate = formatDate(selectedDates[0]);
            const endDate = formatDate(selectedDates[1]);

            const slidesHtml = models.map(model => `
                <div class="swiper-slide">
                    <div class="bike__card">
                        <img src="${model.imageUrl}" alt="${model.name}" class="bike__img">
                        <div class="bike__card-content">
                            <h3 class="bike__name">${model.name}</h3>
                            <p class="bike__status available">
                                ${model.availableCount}대 이용 가능
                            </p>
                            <a href="/reserve?modelName=${encodeURIComponent(model.name)}&startDate=${startDate}&endDate=${endDate}" class="button button--secondary">모델 선택</a>
                        </div>
                    </div>
                </div>
            `).join('');

            bikeSwiper.appendSlide(slidesHtml);

        }
        bikeSwiper.update();
    }

    // 날짜를 YYYY-MM-DD 형식으로 변환하는 헬퍼 함수
    function formatDate(date) {
        const d = new Date(date),
            year = d.getFullYear();
        let month = '' + (d.getMonth() + 1),
            day = '' + d.getDate();
        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;
        return [year, month, day].join('-');
    }

    // 페이지 로드 시 오늘 하루만 선택
    const today = new Date();
    datePicker.setDate([today, today]);
    fetchAvailableBikes(formatDate(today), formatDate(today));
    rentalDurationDisplay.textContent = formatDuration(1);
});