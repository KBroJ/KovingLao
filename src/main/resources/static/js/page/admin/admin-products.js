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
            tableBody.innerHTML = '<tr><td colspan="6" class="no-data">등록된 상품이 없습니다.</td></tr>';
            return;
        }

        products.forEach(product => {
            const row = document.importNode(template.content, true);

            row.querySelector('.product-id').textContent = product.id;
            row.querySelector('.product-image').src = product.imageUrl || '/images/layout/default-product.png';
            row.querySelector('.product-name').textContent = product.name;

            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = product.status;
            statusBadge.classList.add(`status-${product.status.toLowerCase()}`);

            // 날짜/시간 데이터에서 날짜 부분만 잘라서 표시
            row.querySelector('.created-at').textContent = product.createdAt.split('T')[0];

            row.querySelector('.edit-button').href = `/admin/products/${product.id}/edit`;

            tableBody.appendChild(row);
        });

    } catch (error) {
        console.error('상품 목록 로딩 중 오류:', error);
        document.getElementById('product-list-body').innerHTML =
        '<tr><td colspan="6" class="no-data">목록을 불러오는 중 오류가 발생했습니다.</td></tr>';
    }
}