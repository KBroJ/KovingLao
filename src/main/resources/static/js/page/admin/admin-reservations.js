// 페이지 로드가 완료되면 예약 목록을 불러옵니다.
document.addEventListener('DOMContentLoaded', () => {
    loadReservations();
});

// API를 호출하여 예약 목록을 가져와 테이블을 렌더링하는 함수
async function loadReservations() {
    try {
        const response = await fetch('/api/admin/reservations');
        if (!response.ok) throw new Error('예약 목록을 불러오는 데 실패했습니다.');
        const reservations = await response.json();

        const tableBody = document.getElementById('reservation-list-body');
        const template = document.getElementById('reservation-row-template');
        tableBody.innerHTML = ''; // 기존 목록 초기화

        if (reservations.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="no-data">예약 내역이 없습니다.</td></tr>';
            return;
        }

        reservations.forEach(res => {
            // 템플릿 복제
            const row = document.importNode(template.content, true);

            // 데이터 채우기
            row.querySelector('.reservation-code').textContent = res.reservationCode;
            row.querySelector('.customer-name').textContent = res.customerName;
            row.querySelector('.customer-phone').textContent = res.customerPhone;
            row.querySelector('.product-name').textContent = res.productName;
            row.querySelector('.period').textContent = `${res.startDate} ~ ${res.endDate}`;

            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = res.status;
            statusBadge.classList.add(`status-${res.status.toLowerCase()}`);

            row.querySelector('.detail-button').href = `/admin/reservations/${res.id}`;

            // 테이블에 행 추가
            tableBody.appendChild(row);
        });

    } catch (error) {
        console.error('예약 목록 로딩 중 오류:', error);
        const tableBody = document.getElementById('reservation-list-body');
        tableBody.innerHTML = '<tr><td colspan="7" class="no-data">목록을 불러오는 중 오류가 발생했습니다.</td></tr>';
    }
}