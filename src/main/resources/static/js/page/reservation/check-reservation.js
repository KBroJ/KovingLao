// 파일 위치: static/js/page/reservation/check-reservation.js
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('check-reservation-form');
    const resultDetails = document.getElementById('reservation-result-details');
    const errorBox = document.getElementById('error-message-box');
    const submitButton = form.querySelector('button[type="submit"]');
    const buttonText = submitButton.querySelector('.btn-text');
    const spinner = submitButton.querySelector('.spinner');
    const reservationCodeInput = document.getElementById('reservationCode');
    const customerEmailInput = document.getElementById('customerEmail');
    const formInputsForValidation = form.querySelectorAll('.form-input');

    const statusMap = {
        'PENDING': '확인 대기중',
        'CONFIRMED': '예약 확정',
        'CANCELED': '취소됨',
        'COMPLETED': '이용 완료'
    };

    /**
     * 폼 제출 이벤트를 처리합니다.
     */
    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const isAllValid = Array.from(formInputsForValidation).every(input => validateField(input));
        if (!isAllValid) {
            const firstInvalidField = form.querySelector('.invalid');
            if (firstInvalidField) firstInvalidField.focus();
            return;
        }

        setLoading(true);
        hideError();
        resultDetails.style.display = 'none';

        try {
            const response = await fetch('/api/reservations/lookup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    reservationCode: reservationCodeInput.value.trim(),
                    customerEmail: customerEmailInput.value.trim()
                })
            });

            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.message || '예약 정보를 찾을 수 없습니다.');
            }

            renderResult(data);

        } catch (error) {
            showError(error.message);
        } finally {
            setLoading(false);
        }
    });

    /**
     * 각 입력 필드에 실시간 유효성 검사 이벤트를 추가합니다.
     */
    formInputsForValidation.forEach(input => {
        input.dataset.touched = 'false';
        input.addEventListener('blur', () => {
            input.dataset.touched = 'true';
            validateField(input);
        });
        input.addEventListener('input', () => {
            if (input.dataset.touched === 'true') {
                validateField(input);
            }
        });
    });

    /**
     * 개별 입력 필드의 유효성을 검사하고 에러 메시지를 표시/숨김 처리합니다.
     */
    function validateField(input) {
        const errorElement = document.getElementById(`${input.id}-error`);
        let message = '';
        const value = input.value.trim();

        if (value === '') {
            message = '필수 입력 항목입니다.';
        } else if (input.type === 'email') {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(value)) {
                message = '올바른 이메일 형식이 아닙니다.';
            }
        }

        if (message) {
            if (errorElement) {
                errorElement.textContent = message;
                errorElement.classList.add('visible');
            }
            input.classList.add('invalid');
            return false;
        } else {
            if (errorElement) {
                errorElement.classList.remove('visible');
            }
            input.classList.remove('invalid');
            return true;
        }
    }

    /**
     * 조회 결과를 화면에 렌더링합니다.
     */
    function renderResult(data) {
        const statusKorean = statusMap[data.status] || data.status;
        const statusClass = data.status ? data.status.toLowerCase() : 'unknown';

        // [추가] 대여일수 계산 로직
        const startDate = new Date(data.startDate);
        const endDate = new Date(data.endDate);
        const rentalDays = (endDate - startDate) / (1000 * 60 * 60 * 24) + 1;

        // [수정] 대여일수와 총금액을 포함한 결과 카드 HTML
        resultDetails.innerHTML = `
            <div class="reservation-result-card">
                <div class="result-header">
                    <h3>예약 상세 내역</h3>
                    <span class="status-badge status-${statusClass}">${statusKorean}</span>
                </div>
                <div class="result-body">
                    <div class="detail-item"><strong>예약 번호</strong><span>${data.reservationCode}</span></div>
                    <div class="detail-item"><strong>예약자명</strong><span>${data.customerName}</span></div>
                    <div class="detail-item"><strong>예약 모델</strong><span>${data.productName}</span></div>
                    <div class="detail-item"><strong>대여 기간</strong><span>${data.startDate} ~ ${data.endDate} (${rentalDays}일)</span></div>
                    <div class="detail-item"><strong>픽업/반납 시간</strong><span>${data.pickupTime} / ${data.returnTime}</span></div>
                    <div class="detail-item"><strong>총 금액</strong><span>${data.totalPrice.toLocaleString()} LAK</span></div>
                </div>
            </div>`;
        resultDetails.style.display = 'block';
    }

    /**
     * 로딩 및 에러 처리 UI 함수들
     */
    function setLoading(isLoading) {
        submitButton.disabled = isLoading;
        buttonText.style.display = isLoading ? 'none' : 'inline';
        spinner.style.display = isLoading ? 'block' : 'none';
    }
    function showError(message) {
        errorBox.textContent = message;
        errorBox.style.display = 'block';
    }
    function hideError() {
        errorBox.style.display = 'none';
    }
});
