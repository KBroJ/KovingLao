// DOM이 완전히 로드된 후 스크립트 실행
document.addEventListener('DOMContentLoaded', function() {

    console.log("라오스 모토 웹사이트가 로드되었습니다.");

    // Swiper 초기화
    const swiper = new Swiper('.bike-swiper', {

        // 슬라이드 너비를 CSS로 자동 결정
        slidesPerView: 'auto',
        // 활성 슬라이드를 항상 가운데 배치
        centeredSlides: true,
        // 슬라이드 간 간격
        spaceBetween: 20,

        // 반응형 설정
//        breakpoints: {
//            // 화면 너비 640px 이상일 때
//            640: {
//              slidesPerView: 2,
//              spaceBetween: 20,
//            },
//            // 화면 너비 992px 이상일 때
//            992: {
//              slidesPerView: 3,
//              spaceBetween: 30,
//            },
//        },

        // 루프(무한 반복)
//        loop: true,
        loop: false,

        // 페이지네이션
        pagination: {
          el: '.swiper-pagination',
          clickable: true,
        },

        // 네비게이션 화살표
        navigation: {
          nextEl: '.swiper-button-next',
          prevEl: '.swiper-button-prev',
        },
    });

    // 2. Flatpickr (날짜 선택) 초기화
    const rentalDurationDisplay = document.getElementById('rental-duration-display'); // 기간 표시 엘리먼트

    const datePicker = flatpickr("#date-range", {
        mode: "range",
        dateFormat: "Y-m-d", // API와 통신할 날짜 형식
        minDate: "today",    // 과거 날짜 선택 불가
        "locale": "ko",      // 한국어 설정

        // 달력 창이 닫힐 때마다 실행되는 이벤트
        onClose: function(selectedDates) {
            if (selectedDates.length < 2) {
                rentalDurationDisplay.textContent = ''; // 날짜가 1개만 선택되면 기간 표시 안 함
                return;
            }

            const startDate = selectedDates[0];
            const endDate = selectedDates[1];

            // 날짜 차이 계산 (밀리초 -> 일) + 1일 (시작일 포함)
            const diffDays = Math.round((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;

            // 계산된 기간을 화면에 표시
            rentalDurationDisplay.textContent = formatDuration(diffDays);
        }
    });

    // [추가] 일/월 단위로 기간을 예쁘게 포맷하는 함수
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

        return result.trim(); // "1개월 " 처럼 뒤에 공백이 남는 경우 제거
    }

    const searchForm = document.getElementById('bike-search-form');
    const noResultsMessage = document.getElementById('no-results-message');

    // 3. 검색 폼 제출 이벤트 리스너
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault(); // 폼 제출 시 페이지 새로고침 방지

        const selectedDates = datePicker.selectedDates;
        if (selectedDates.length < 2) {
            alert('대여 날짜와 반납 날짜를 모두 선택해주세요.');
            return;
        }

        const startDate = formatDate(selectedDates[0]);
        const endDate = formatDate(selectedDates[1]);

        // API 호출 함수 실행
        fetchAvailableBikes(startDate, endDate);
    });

    // 4. API 호출 및 슬라이드 업데이트 함수
    async function fetchAvailableBikes(startDate, endDate) {
        try {
            const response = await fetch(`/api/products/available?startDate=${startDate}&endDate=${endDate}`);
            if (!response.ok) {
                throw new Error('데이터를 불러오는 데 실패했습니다.');
            }
            const availableModels = await response.json();
            updateBikeList(availableModels);
        } catch (error) {
            console.error(error);
            alert(error.message);
        }
    }

    // 5. Swiper 슬라이드 동적 업데이트 함수
    function updateBikeList(models) {

        swiper.removeAllSlides(); // 기존 슬라이드 모두 제거

        if (models.length === 0) {
            noResultsMessage.style.display = 'block';
            swiper.el.style.display = 'none'; // Swiper 컨테이너 숨기기
        } else {
            noResultsMessage.style.display = 'none';
            swiper.el.style.display = 'block'; // Swiper 컨테이너 보이기

            const slidesHtml = models.map(model => `
                <div class="swiper-slide">
                    <div class="bike__card">
                        <img src="${model.imageUrl}" alt="${model.name}" class="bike__img">
                        <div class="bike__card-content">
                            <h3 class="bike__name">${model.name}</h3>
                            <p class="bike__status available">
                                ${model.availableCount}대 이용 가능
                            </p>
                            <a href="/reserve?model=${model.name}" class="button button--secondary">모델 선택</a>
                        </div>
                    </div>
                </div>
            `).join('');

            swiper.appendSlide(slidesHtml);
        }
        swiper.update(); // Swiper 상태 업데이트
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
    // datePicker의 기본 날짜를 오늘 하루로 설정
    datePicker.setDate([today, today]);
    // 오늘 날짜로 첫 오토바이 목록을 불러옴
    fetchAvailableBikes(formatDate(today), formatDate(today));
    // 페이지 로드 시 초기 기간("총 1일")을 표시합니다.
    rentalDurationDisplay.textContent = formatDuration(1);


    // 예: 2. 1:1 문의 채팅 기능 초기화
    // initChat();

});