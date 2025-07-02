// DOM이 완전히 로드된 후 스크립트 실행
document.addEventListener('DOMContentLoaded', function() {

    console.log("라오스 모토 웹사이트가 로드되었습니다.");

    // Swiper 초기화
    const swiper = new Swiper('.bike-swiper', {
        // 한 번에 보여줄 슬라이드 수
        slidesPerView: 1,
        // 슬라이드 간 간격
        spaceBetween: 20,

        // 반응형 설정
        breakpoints: {
            // 화면 너비 640px 이상일 때
            640: {
              slidesPerView: 2,
              spaceBetween: 20,
            },
            // 화면 너비 992px 이상일 때
            992: {
              slidesPerView: 3,
              spaceBetween: 30,
            },
        },

        // 루프(무한 반복)
        loop: true,

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

    // [향후 기능 추가 영역]

    // 예: 1. API를 통해 실시간 오토바이 상태 업데이트
    // fetch('/api/bikes')
    //     .then(response => response.json())
    //     .then(data => {
    //         // 바이크 목록 렌더링 로직
    //     });

    // 예: 2. 1:1 문의 채팅 기능 초기화
    // initChat();

});