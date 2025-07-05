
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('check-reservation-form');
    const resultDetails = document.getElementById('reservation-result-details');

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        const reservationCode = document.getElementById('reservationCode').value;
        const customerEmail = document.getElementById('customerEmail').value;

        try {
            const response = await fetch('/api/reservations/lookup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ reservationCode, customerEmail })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '예약 정보를 찾을 수 없습니다.');
            }

            const data = await response.json();
            // 성공 시, 받은 데이터로 상세 정보 HTML을 만들어 표시
            resultDetails.innerHTML = `
                <h3>예약 상세 내역</h3>
                <p><strong>예약 번호:</strong> ${data.reservationCode}</p>
                <p><strong>예약자:</strong> ${data.customerName}</p>
                <p><strong>모델:</strong> ${data.productName}</p>
                <p><strong>기간:</strong> ${data.startDate} ~ ${data.endDate}</p>
                <p><strong>상태:</strong> ${data.status}</p>
            `;
            resultDetails.style.display = 'block';

        } catch (error) {
            alert(error.message);
            resultDetails.style.display = 'none';
        }
    });
});
