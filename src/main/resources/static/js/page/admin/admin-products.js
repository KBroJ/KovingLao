document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('product-list-body');
    if (!tableBody) return;

    /**
     * '더보기' 메뉴 토글 기능 (이벤트 위임)
     * 테이블의 아무 곳이나 클릭했을 때, 클릭된 요소가 .kebab-button인지 확인합니다.
     */
    tableBody.addEventListener('click', (event) => {
        const kebabButton = event.target.closest('.kebab-button');
        if (kebabButton) {
            const menu = kebabButton.nextElementSibling;
            // 다른 모든 메뉴는 닫고, 현재 클릭된 메뉴만 토글합니다.
            closeAllActionMenus(menu);
            menu.classList.toggle('show');
        }

        /**
         * '삭제' 버튼 클릭 이벤트 처리
         */
        if (event.target.classList.contains('delete-button')) {
            if (!confirm('정말 이 상품 모델을 삭제하시겠습니까?\n연결된 모든 재고 정보가 함께 삭제됩니다.')) {
                return;
            }
            const row = event.target.closest('tr');
            const modelId = row.dataset.modelId;
            deleteProduct(modelId, row);
        }
    });

    /**
     * 메뉴 영역 바깥을 클릭하면 모든 '더보기' 메뉴를 닫습니다.
     */
    window.addEventListener('click', (event) => {
        if (!event.target.closest('.action-menu-container')) {
            closeAllActionMenus();
        }
    });

    /**
     * 모든 열린 '더보기' 메뉴를 닫는 헬퍼 함수
     * @param {HTMLElement} exceptMenu 예외로 둘, 닫지 않을 메뉴
     */
    function closeAllActionMenus(exceptMenu = null) {
        document.querySelectorAll('.action-menu.show').forEach(m => {
            if (m !== exceptMenu) {
                m.classList.remove('show');
            }
        });
    }

    /**
     * 삭제 API를 호출하는 함수
     * @param {number} modelId 삭제할 모델의 ID
     * @param {HTMLElement} row 삭제할 테이블 행 요소
     */
    async function deleteProduct(modelId, row) {
        try {
            const response = await fetch(`/api/admin/products/${modelId}`, {
                method: 'DELETE',
                headers: {
                    [document.querySelector('meta[name="_csrf_header"]').getAttribute('content')]: document.querySelector('meta[name="_csrf"]').getAttribute('content')
                }
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.message || '삭제에 실패했습니다.');

            alert(result.message);
            row.remove(); // 화면에서 해당 행 즉시 삭제
        } catch (error) {
            console.error('삭제 처리 중 오류:', error);
            alert(`오류: ${error.message}`);
        }
    }

    /**
     * 페이지 로드 시 상품 목록을 불러오는 메인 함수
     */
    async function loadProducts() {
        try {
            const response = await fetch('/api/admin/products');
            if (!response.ok) throw new Error('상품 목록을 불러오는 데 실패했습니다.');
            const products = await response.json();

            const template = document.getElementById('product-row-template');
            tableBody.innerHTML = '';

            if (products.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="5" class="no-data">등록된 상품 모델이 없습니다.</td></tr>';
                return;
            }

            products.forEach(model => {
                const row = document.importNode(template.content, true);
                const tr = row.querySelector('tr');
                tr.dataset.modelId = model.id;

                row.querySelector('.product-name').textContent = model.name;
                row.querySelector('.product-rate').textContent = `$${model.dailyRate} / $${model.monthlyRate}`;
                row.querySelector('.product-quantity').textContent = `${model.availableQuantity} / ${model.totalQuantity}`;

                const statusBadge = row.querySelector('.status-badge');
                statusBadge.textContent = model.active ? '판매중' : '판매중지';
                statusBadge.classList.add(model.active ? 'status-active' : 'status-inactive');

                // 버튼들의 href 속성을 동적으로 설정합니다.
                row.querySelector('.inventory-button').href = `/admin/products/${model.id}/inventory`;
                row.querySelector('.edit-button').href = `/admin/products/${model.id}/edit`;

                tableBody.appendChild(row);
            });
        } catch (error) {
            console.error('상품 목록 로딩 중 오류:', error);
            tableBody.innerHTML = '<tr><td colspan="5" class="no-data">목록을 불러오는 중 오류가 발생했습니다.</td></tr>';
        }
    }

    // 페이지가 처음 열릴 때 상품 목록을 로드합니다.
    loadProducts();
});