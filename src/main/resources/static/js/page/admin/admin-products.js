document.addEventListener('DOMContentLoaded', () => {
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

            row.querySelector('.product-name').textContent = model.name;
            row.querySelector('.product-rate').textContent = `$${model.dailyRate} / $${model.monthlyRate}`;
            row.querySelector('.product-quantity').textContent = `${model.availableQuantity} / ${model.totalQuantity}`;

            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = model.active ? '판매중' : '판매중지';
            statusBadge.classList.add(model.active ? 'status-active' : 'status-inactive');

            // 날짜/시간 데이터에서 날짜 부분만 잘라서 표시
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