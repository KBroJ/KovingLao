// 예약 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log("예약 페이지가 로드되었습니다.");
    
    // 모델 선택 버튼 이벤트
    const modelSelectBtns = document.querySelectorAll('.model-select-btn');
    const hiddenModelInput = document.getElementById('hiddenModelName');
    
    modelSelectBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const selectedModel = this.getAttribute('data-model');
            
            // URL 파라미터로 리다이렉트
            window.location.href = `/reserve?model=${encodeURIComponent(selectedModel)}`;
        });
    });
    
    // 날짜 입력 제한 (오늘 이후 날짜만 선택 가능)
    const rentalDateInput = document.getElementById('rentalDate');
    if (rentalDateInput) {
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        
        const minDate = tomorrow.toISOString().split('T')[0];
        rentalDateInput.setAttribute('min', minDate);
    }
    
    // 폼 유효성 검사
    const reservationForm = document.querySelector('.reservation-form');
    if (reservationForm) {
        reservationForm.addEventListener('submit', function(e) {
            const selectedModel = hiddenModelInput ? hiddenModelInput.value : '';
            
            if (!selectedModel) {
                e.preventDefault();
                alert('오토바이 모델을 선택해주세요.');
                return false;
            }
            

            
            // 여권 번호 형식 간단 검증
            const passportNumber = document.getElementById('passportNumber').value;
            if (passportNumber && passportNumber.length < 5) {
                e.preventDefault();
                alert('올바른 여권 번호를 입력해주세요.');
                return false;
            }
            
            return true;
        });
    }
    
    // 전화번호 입력 포맷팅
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^\d+\-\s]/g, '');
            e.target.value = value;
        });
    }
    
    // 여권 번호 입력 포맷팅 (영문 대문자와 숫자만)
    const passportInput = document.getElementById('passportNumber');
    if (passportInput) {
        passportInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^A-Z0-9]/g, '');
            e.target.value = value;
        });
        
        passportInput.addEventListener('keypress', function(e) {
            const char = e.key.toUpperCase();
            if (!/[A-Z0-9]/.test(char) && !['Backspace', 'Delete', 'Tab', 'Enter'].includes(e.key)) {
                e.preventDefault();
            }
        });
    }
    
    // 이름 입력 필드 (영문만 허용)
    const nameInputs = document.querySelectorAll('#firstName, #lastName');
    nameInputs.forEach(input => {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^A-Za-z\s]/g, '');
            e.target.value = value;
        });
    });
    
    // 가격 계산 기능
    function calculatePrice() {
        const duration = document.getElementById('duration').value;
        
        let basePrice = 0;
        switch(duration) {
            case '2': basePrice = 20000; break;
            case '4': basePrice = 35000; break;
            case '8': basePrice = 60000; break;
            case '24': basePrice = 100000; break;
        }
        
        let totalPrice = basePrice;
        
        // 가격 표시 영역이 있다면 업데이트
        const priceDisplay = document.getElementById('priceDisplay');
        if (priceDisplay && totalPrice > 0) {
            priceDisplay.textContent = `예상 총 비용: ${totalPrice.toLocaleString()} LAK`;
        }
    }
    
    // 대여 시간 변경 시 가격 재계산
    const durationSelect = document.getElementById('duration');
    
    if (durationSelect) {
        durationSelect.addEventListener('change', calculatePrice);
    }
    
    // 초기 가격 계산
    calculatePrice();
    
    // 선택된 모델 하이라이트
    const urlParams = new URLSearchParams(window.location.search);
    const selectedModelFromUrl = urlParams.get('model');
    
    if (selectedModelFromUrl) {
        const modelCards = document.querySelectorAll('.model-card');
        modelCards.forEach(card => {
            const btn = card.querySelector('.model-select-btn');
            if (btn && btn.getAttribute('data-model') === selectedModelFromUrl) {
                card.classList.add('selected');
            }
        });
    }
}); 