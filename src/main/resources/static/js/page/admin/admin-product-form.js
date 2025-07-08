
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('product-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        // 1. 폼의 기본 제출 동작(페이지 새로고침)을 막습니다.
        e.preventDefault();

        // HTML에 숨겨둔 CSRF 토큰과 헤더 이름을 가져옵니다.
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


        /*
         2. 폼 데이터를 FormData 객체로 가져온 후, 일반적인 JavaScript 객체로 변환합니다.
            new FormData(form) :
                form 요소 안을 자동으로 스캔해서,
                name 속성을 가진 모든 입력 필드들(<input>, <textarea>, <select> 등)의
                name과 value를 Key-Value 쌍으로 수집
            formData.entries(): formData 객체 안에 정리된 데이터들을 [Key, Value] 형태의 배열 묶음으로 만듬
                ex) [ ['name', 'K-Bike'], ['dailyRate', '15'] ]
            Object.fromEntries(...):
                이 메서드는 [Key, Value] 배열들의 묶음을 받아서,
                우리가 흔히 사용하는 일반적인 JavaScript 객체 {} 로 최종 변환
                ex)
                    {
                        name: "K-Bike",
                        dailyRate: "15"
                    }
        */
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        // 'isActive' 필드는 체크박스가 아니므로, 값을 boolean으로 변환해줍니다.
        data.isActive = (data.isActive === 'true');

        // 숫자 필드들도 숫자로 변환해줍니다.(소수점 허용)
        data.dailyRate = parseFloat(data.dailyRate);
        data.monthlyRate = parseFloat(data.monthlyRate);
        data.deposit = parseFloat(data.deposit);

        // initialQuantity도 정수로 변환합니다.(소수점 불허용)
        // 사용자가 숫자를 입력하면 그 숫자를, 아무것도 입력하지 않거나 잘못된 값을 넣으면 0 세팅
        data.initialQuantity = parseInt(data.initialQuantity, 10) || 0;

        try {
            // 3. fetch API를 사용해 백엔드에 POST 요청을 보냅니다.
            const response = await fetch('/api/admin/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // 요청 헤더에 CSRF 토큰을 추가
                    [header]: token
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