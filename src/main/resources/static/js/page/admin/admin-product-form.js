
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('product-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        // 1. 폼의 기본 제출 동작(페이지 새로고침)을 막습니다.
        e.preventDefault();

        // 2. 폼 데이터를 FormData 객체로 가져온 후, 일반적인 JavaScript 객체로 변환합니다.
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        // 'isActive' 필드는 체크박스가 아니므로, 값을 boolean으로 변환해줍니다.
        data.isActive = (data.isActive === 'true');

        // 숫자 필드들도 숫자로 변환해줍니다.
        data.dailyRate = parseFloat(data.dailyRate);
        data.monthlyRate = parseFloat(data.monthlyRate);
        data.deposit = parseFloat(data.deposit);

        try {
            // 3. fetch API를 사용해 백엔드에 POST 요청을 보냅니다.
            const response = await fetch('/api/admin/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data), // JavaScript 객체를 JSON 문자열로 변환
            });

            const result = await response.json();

            if (!response.ok) {
                // 서버에서 보낸 에러 메시지가 있다면 사용하고, 없다면 일반적인 메시지를 사용합니다.
                throw new Error(result.message || '저장에 실패했습니다.');
            }

            // 4. 저장이 성공하면, 알림을 띄우고 상품 목록 페이지로 이동합니다.
            alert(result.message);
            window.location.href = '/admin/products';

        } catch (error) {
            console.error('Error:', error);
            alert(`오류가 발생했습니다: ${error.message}`);
        }
    });
});