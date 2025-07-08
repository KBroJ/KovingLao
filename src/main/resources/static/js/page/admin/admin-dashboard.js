// src/main/resources/static/js/page/admin/dashboard.js (전체 교체)

// 페이지 로드가 완료되면 대시보드 데이터와 차트 데이터를 로드합니다.
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
    loadChartData();
});

// 대시보드의 주요 데이터(KPI, 목록)를 불러와 렌더링하는 함수
async function loadDashboardData() {
    try {
        const response = await fetch('/api/admin/dashboard');
        if (!response.ok) throw new Error('데이터 로딩 실패');
        const data = await response.json();

        // KPI 데이터 렌더링
        renderKpi(data);

        // 처리 대기 예약 목록 렌더링
        renderPendingReservations(data.recentPendingReservations);

        // 오늘의 스케줄 목록 렌더링
        renderTodaySchedule(data.todaySchedule);

    } catch (error) {
        console.error('대시보드 데이터를 불러오는 중 에러 발생:', error);
        // 에러 발생 시 사용자에게 알림을 줄 수도 있습니다.
    }
}

// KPI 카드를 업데이트하는 함수
function renderKpi(data) {
    document.getElementById('kpi-today-reservations').textContent = `${data.todayNewReservations}건`;
    document.getElementById('kpi-pending-reservations').textContent = `${data.pendingReservations}건`;
    document.getElementById('kpi-today-sales').textContent = `${data.todayExpectedSales.toLocaleString()} LAK`;
    document.getElementById('kpi-currently-rented').textContent = `${data.currentlyRentedCount}대`;
}

// 처리 대기 예약 목록을 그리는 함수
function renderPendingReservations(items) {
    const listElement = document.getElementById('pending-reservations-list');
    const noDataElement = document.getElementById('no-pending-reservations');
    listElement.innerHTML = ''; // 기존 목록 초기화

    if (!items || items.length === 0) {
        noDataElement.style.display = 'block';
        return;
    }
    noDataElement.style.display = 'none';

    items.forEach(item => {
        const li = document.createElement('li');
        li.innerHTML = `
            <div class="item-info">
                <strong>${item.customerName}</strong>
                <span>(${item.reservationCode})</span>
            </div>
            <div class="item-meta">
                <span>${item.startDate} ~ ${item.endDate}</span>
            </div>
            <a href="/admin/reservations/${item.id}" class="button button--secondary button--sm">처리하기</a>
        `;
        listElement.appendChild(li);
    });
}

// 오늘의 스케줄 목록을 그리는 함수
function renderTodaySchedule(items) {
    const listElement = document.getElementById('today-schedule-list');
    const noDataElement = document.getElementById('no-today-schedule');
    listElement.innerHTML = ''; // 기존 목록 초기화

    if (!items || items.length === 0) {
        noDataElement.style.display = 'block';
        return;
    }
    noDataElement.style.display = 'none';

    items.forEach(item => {
        const tagClass = item.type === '출고' ? 'tag-pickup' : 'tag-return';
        const li = document.createElement('li');
        li.innerHTML = `
            <div class="item-info">
                <span class="tag ${tagClass}">${item.type}</span>
                <strong>${item.time}</strong>
                <span>${item.customerName}</span>
            </div>
            <div class="item-meta">
                <span>${item.productName}</span>
            </div>
            <a href="/admin/reservations/${item.reservationId}" class="button button--secondary button--sm">상세</a>
        `;
        listElement.appendChild(li);
    });
}


// 차트 데이터를 불러와 렌더링하는 함수
async function loadChartData() {
    const ctx = document.getElementById('monthlyReservationsChart');
    if (!ctx) return;

    try {
        const response = await fetch('/api/admin/stats/monthly-reservations');
        if (!response.ok) throw new Error('차트 데이터 로딩 실패');
        const chartData = await response.json();

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: chartData.labels,
                datasets: [{
                    label: '월별 예약 건수',
                    data: chartData.data,
                    borderColor: 'rgba(0, 123, 255, 0.8)',
                    backgroundColor: 'rgba(0, 123, 255, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: { /* 옵션은 기존과 동일 */ }
        });
    } catch (error) {
        console.error('차트 데이터를 불러오는 중 에러 발생:', error);
    }
}