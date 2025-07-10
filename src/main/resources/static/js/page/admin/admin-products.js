document.addEventListener('DOMContentLoaded', () => {

    const tableBody = document.getElementById('product-list-body');
    if (!tableBody) return;

    // 삭제 버튼 클릭 이벤트 처리 (이벤트 위임 사용)
    tableBody.addEventListener('click', async (event) => {
        if (event.target.classList.contains('delete-button')) {
            if (!confirm('정말 이 상품 모델을 삭제하시겠습니까?\n연결된 모든 재고 정보가 함께 삭제됩니다.')) {
                return;
            }

            const row = event.target.closest('tr');
            const modelId = row.dataset.modelId;

            try {
                const response = await fetch(`/api/admin/products/${modelId}`, {
                    method: 'DELETE',
                    headers: {
                        [document.querySelector('meta[name="_csrf_header"]').getAttribute('content')]: document.querySelector('meta[name="_csrf"]').getAttribute('content')
                    }
                });

                const result = await response.json();
                if (!response.ok) {
                    throw new Error(result.message || '삭제에 실패했습니다.');
                }
                alert(result.message);
                row.remove(); // 화면에서 해당 행 즉시 삭제
            } catch (error) {
                console.error('삭제 처리 중 오류:', error);
                alert(`오류: ${error.message}`);
            }
        }
    });

    loadProducts();
});

async function loadProducts() {
    try {
        const response = await fetch('/api/admin/products');
        if (!response.ok) throw new Error('상품 목록을 불러오는 데 실패했습니다.');
        const products = await response.json();

        const tableBody = document.getElementById('product-list-body');
        const template = document.getElementById('product-row-template');
        tableBody.innerHTML = '';

        if (products.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="no-data">등록된 상품 모델이 없습니다.</td></tr>';
            return;
        }

        products.forEach(model => {
            const row = document.importNode(template.content, true);
            const tr = row.querySelector('tr');

            tr.dataset.modelId = model.id; // [추가] 행에 model-id를 데이터로 저장

            row.querySelector('.product-name').textContent = model.name;
            row.querySelector('.product-rate').textContent = `$${model.dailyRate} / $${model.monthlyRate}`;
            row.querySelector('.product-quantity').textContent = `${model.availableQuantity} / ${model.totalQuantity}`;

            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = model.active ? '판매중' : '판매중지';
            statusBadge.classList.add(model.active ? 'status-active' : 'status-inactive');

            row.querySelector('.inventory-button').href = `/admin/products/${model.id}/inventory`;
            row.querySelector('.edit-button').href = `/admin/products/${model.id}/edit`;

            tableBody.appendChild(row);
        });

    } catch (error) {
        console.error('상품 목록 로딩 중 오류:', error);
        document.getElementById('product-list-body').innerHTML =
        '<tr><td colspan="6" class="no-data">목록을 불러오는 중 오류가 발생했습니다.</td></tr>';
    }
}