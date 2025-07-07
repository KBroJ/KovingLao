// 파일 위치: static/js/page/admin/dashboard.js
document.addEventListener('DOMContentLoaded', function () {
    const ctx = document.getElementById('monthlyReservationsChart');
    if (!ctx) return;

    // TODO: 실제로는 API를 통해 월별 예약 데이터를 가져와야 합니다.
    // 임시 데이터
    const labels = ['1월', '2월', '3월', '4월', '5월', '6월', '7월'];
    const data = {
        labels: labels,
        datasets: [{
            label: '월별 예약 건수',
            data: [12, 19, 3, 5, 2, 3, 20], // 임시 데이터
            borderColor: 'rgba(0, 123, 255, 0.8)',
            backgroundColor: 'rgba(0, 123, 255, 0.1)',
            fill: true,
            tension: 0.3
        }]
    };

    new Chart(ctx, {
        type: 'line',
        data: data,
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
});
