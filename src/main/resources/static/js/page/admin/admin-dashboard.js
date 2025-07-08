// [수정] 차트 인스턴스를 저장할 전용 변수를 파일 최상단에 선언합니다.
if (typeof monthlyReservationsChart === 'undefined') {
  var monthlyReservationsChart;
}

// 스크립트 중복 실행을 막는 '가드(Guard)' 변수를 사용
// window 객체는 전역으로 접근 가능하므로 스크립트가 다시 로드되어도 값을 유지합니다.
if (!window.isDashboardInitialized) {

    document.addEventListener('DOMContentLoaded', () => {
        loadDashboardData();
        loadChartData();
    });

    // 초기화 로직이 등록되었음을 표시
    window.isDashboardInitialized = true;
}

// 대시보드의 주요 데이터(KPI, 목록)를 불러와 렌더링하는 함수
async function loadDashboardData() {
    try {
        const response = await fetch('/api/admin/dashboard');
        if (!response.ok) throw new Error('데이터 로딩 실패');
        const data = await response.json();

        renderKpi(data);
        renderPendingReservations(data.recentPendingReservations);
        renderTodaySchedule(data.todaySchedule);

    } catch (error) {
        console.error('대시보드 데이터를 불러오는 중 에러 발생:', error);
    }
}

function renderKpi(data) {
    document.getElementById('kpi-today-reservations').textContent = `${data.todayNewReservations}건`;
    document.getElementById('kpi-pending-reservations').textContent = `${data.pendingReservations}건`;
    document.getElementById('kpi-today-sales').textContent = `${data.todayExpectedSales.toLocaleString()} LAK`;
    document.getElementById('kpi-currently-rented').textContent = `${data.currentlyRentedCount}대`;
}

function renderPendingReservations(items) {
    const listElement = document.getElementById('pending-reservations-list');
    const noDataElement = document.getElementById('no-pending-reservations');
    listElement.innerHTML = '';
    if (!items || items.length === 0) {
        noDataElement.style.display = 'block';
        return;
    }
    noDataElement.style.display = 'none';
    items.forEach(item => {
        const li = document.createElement('li');
        li.innerHTML = `
            <div class="item-info"><strong>${item.customerName}</strong><span>(${item.reservationCode})</span></div>
            <div class="item-meta"><span>${item.startDate} ~ ${item.endDate}</span></div>
            <a href="/admin/reservations/${item.id}" class="button button--secondary button--sm">처리하기</a>
        `;
        listElement.appendChild(li);
    });
}

function renderTodaySchedule(items) {
    const listElement = document.getElementById('today-schedule-list');
    const noDataElement = document.getElementById('no-today-schedule');
    listElement.innerHTML = '';
    if (!items || items.length === 0) {
        noDataElement.style.display = 'block';
        return;
    }
    noDataElement.style.display = 'none';
    items.forEach(item => {
        const tagClass = item.type === '출고' ? 'tag-pickup' : 'tag-return';
        const li = document.createElement('li');
        li.innerHTML = `
            <div class="item-info"><span class="tag ${tagClass}">${item.type}</span><strong>${item.time}</strong><span>${item.customerName}</span></div>
            <div class="item-meta"><span>${item.productName}</span></div>
            <a href="/admin/reservations/${item.reservationId}" class="button button--secondary button--sm">상세</a>
        `;
        listElement.appendChild(li);
    });
}


// 월별 예약 현황 차트 데이터를 불러와 렌더링하는 함수
async function loadChartData() {
    try {

        console.log("loadChartData|monthlyReservationsChart : " + monthlyReservationsChart)

        // [수정] 기존 차트가 변수에 할당되어 있다면 파괴합니다.
        if (monthlyReservationsChart) {
            console.log("loadChartData|monthlyReservationsChart.destroy()")
            monthlyReservationsChart.destroy();
        }

        const response = await fetch('/api/admin/stats/monthly-reservations');
        if (!response.ok) throw new Error('차트 데이터 로딩 실패');
        const chartData = await response.json();

        const canvas = document.getElementById('monthlyReservationsChart');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');

        // [수정] 새 차트 인스턴스를 파일 상단에 선언한 변수에 할당합니다.
        monthlyReservationsChart = new Chart(ctx, {
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
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            // y축 눈금이 정수로만 표시되도록 설정
                            callback: function(value) {
                                if (Number.isInteger(value)) {
                                    return value;
                                }
                            },
                            stepSize: 1
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('차트 데이터를 불러오는 중 에러 발생:', error);
    }
}