
document.addEventListener('DOMContentLoaded', () => {

    // --- 업로드 설정값 ---
    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 파일당 10MB
    const MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 전체 50MB

    // --- 요소 가져오기 ---
    const form = document.getElementById('product-form');
    const modelIdInput = document.getElementById('modelId'); // 수정 모드 확인용
    const imageFilesInput = document.getElementById('imageFiles');
    const previewContainer = document.getElementById('image-preview-container');
    const uploadStatus = document.getElementById('upload-status');

    // 업로드할 파일(File 객체)과 기존 이미지(URL 객체)를 함께 관리하는 배열
    let imageList = [];

    if (!form || !imageFilesInput || !previewContainer) return;

    /**
     * 수정 모드일 때, 기존 이미지 데이터를 불러오는 함수
     */
    async function loadExistingImages() {
        const modelId = modelIdInput.value;
        console.log("loadExistingImages|modelId : " + modelId);

        if (!modelId) return; // id가 없으면(등록 모드) 함수 종료

        try {
            const response = await fetch(`/api/products/${modelId}`);
            if (!response.ok) throw new Error('이미지 정보 로딩 실패');
            const data = await response.json();

            // 불러온 이미지 URL들을 imageList 배열에 특별한 형태로 저장
            imageList = data.imageUrls.map(url => ({
                isExisting: true, // 기존 이미지임을 표시
                name: url.substring(url.lastIndexOf('/') + 1), // 파일명 추출
                url: url
            }));

            renderPreviewsAndStatus();
        } catch (error) {
            console.error('기존 이미지 로딩 중 에러:', error);
        }
    }

    // 페이지가 로드되면, 수정 모드인지 확인하고 기존 이미지를 불러옴
    loadExistingImages();

    // '파일 선택' 라벨을 클릭하면 숨겨진 input[type=file]이 클릭되도록 함
    document.querySelector('label[for="imageFiles"]').addEventListener('click', (e) => {
        e.preventDefault();
        imageFilesInput.click();
    });

    // 파일 입력이 변경되었을 때의 이벤트 처리
    imageFilesInput.addEventListener('change', () => {
        const selectedFiles = Array.from(imageFilesInput.files);

        // 용량 검사
        for (const file of selectedFiles) {
            if (file.size > MAX_FILE_SIZE) {
                alert(`'${file.name}' 파일의 용량이 너무 큽니다. (최대 10MB)`);
                continue; // 이 파일은 건너뛰기
            }

            const currentTotalSize = imageList.reduce((total, item) => total + (item.size || 0), 0);
            if (currentTotalSize + file.size > MAX_TOTAL_SIZE) {
                alert('전체 업로드 용량을 초과했습니다. (최대 50MB)');
                break;
            }

            // 새로 선택된 파일은 isExisting 플래그 없이 File 객체 그대로 추가
            imageList.push(file);
        }

        renderPreviewsAndStatus();
        imageFilesInput.value = ""; // input의 값을 비워줘서 같은 파일을 다시 선택할 수 있게 함
    });

    // 미리보기 이미지 제거
    previewContainer.addEventListener('click', (e) => {

        // '제거' 버튼 클릭 시
        if (e.target.classList.contains('remove-btn')) {
            const indexToRemove = parseInt(e.target.dataset.index, 10);
            imageList.splice(indexToRemove, 1); // 배열에서 파일 제거

            renderPreviewsAndStatus(); // 미리보기 다시 렌더링
        }

    });

    /**
     * SortableJS 초기화
     * 미리보기 컨테이너에 드래그 앤 드롭 기능을 적용합니다.
     */
    new Sortable(previewContainer, {
        animation: 150, // 애니메이션 속도
        ghostClass: 'sortable-ghost', // 드래그 시 나타날 placeholder의 CSS 클래스
        // 드래그가 끝났을 때 실행될 함수
        onEnd: (evt) => {
            // 1. imageList 배열에서 이동한 파일을 잘라내서 가져옵니다.
            const movedItem = imageList.splice(evt.oldIndex, 1)[0];
            // 2. 새로운 위치에 잘라냈던 파일을 다시 삽입합니다.
            imageList.splice(evt.newIndex, 0, movedItem);

            // 3. 순서가 변경되었으므로, 미리보기를 다시 렌더링하여 번호 배지를 업데이트합니다.
            renderPreviewsAndStatus();
        }
    });

    // 미리보기와 업로드 상태를 업데이트하는 통합 함수
    function renderPreviewsAndStatus() {
        previewContainer.innerHTML = '';

        imageList.forEach((item, index) => {

            const previewItem = document.createElement('div');
            previewItem.className = 'image-preview-item';

            const isRepresentative = index === 0;
            const badgeText = isRepresentative ? '대표' : index + 1;
            const badgeClass = isRepresentative ? 'order-badge is-rep' : 'order-badge';

            // 순서 배지 UI 추가
            previewItem.innerHTML = `
                <img id="preview-img-${index}" src="" alt="${item.name}">
                <div class="${badgeClass}">${badgeText}</div>
                <button type="button" class="remove-btn" data-index="${index}">&times;</button>
            `;
            previewContainer.appendChild(previewItem);

            const imgElement = document.getElementById(`preview-img-${index}`);

            // 기존 이미지는 URL을 직접 src에 넣고, 새 파일은 FileReader로 읽음
            if (item.isExisting) {
                imgElement.src = item.url;
            } else { // 새로 추가된 파일(File 객체)
                const reader = new FileReader();
                reader.onload = (e) => {
                    imgElement.src = e.target.result;
                };
                reader.readAsDataURL(item);
            }
        });
        updateUploadStatus();
    }

    // 업로드 상태 텍스트를 업데이트하는 함수
    function updateUploadStatus() {
        const newFilesSize = imageList.filter(item => !item.isExisting).reduce((total, f) => total + f.size, 0);
        const totalSizeMB = (newFilesSize / 1024 / 1024).toFixed(2);
        uploadStatus.textContent = `총 이미지: ${imageList.length}개 (새 파일: ${totalSizeMB}MB)`;
    }

    // 폼 제출 이벤트 처리
    form.addEventListener('submit', async (e) => {
        e.preventDefault(); // 기본 submit 제출 동작 방지 > fetch로 FormData 전송

        const modelId = form.elements.id.value; // <input type="hidden" id="modelId" th:field="*{id}">의 value 값 가져옴
        const isEditMode = !!modelId;           // !!modelId : modelId가 존재하면 true, 아니면 false

        // 수정 모드일때와 등록 모드일때 요청 URL과 방식을 다르게 설정
        const url = isEditMode ? `/api/admin/products/${modelId}` : '/api/admin/products'; // is
        const method = isEditMode ? 'PUT' : 'POST'; // PUT : 수정, POST :  등록

        // 1. FormData 객체 생성
        const formData = new FormData();

        // 2. DTO에 해당하는 폼 데이터(텍스트 등)를 JSON으로 만들어 'formDto' 파트에 추가
        const formElements = form.elements;

        // 미리보기에 '남아있는 기존 이미지 URL 목록'을 추가
        const existingImageUrls = imageList     // imageList 배열에서
            .filter(item => item.isExisting)    // 기존 이미지인 경우만 필터링
            .map(item => item.url);             // URL만 추출

        const dto = {
            id: isEditMode ? modelId : null, // 수정 모드일 때만 ID 포함
            name: formElements.name.value,
            description: formElements.description.value,
            dailyRate: parseFloat(formElements.dailyRate.value),
            monthlyRate: parseFloat(formElements.monthlyRate.value),
            deposit: parseFloat(formElements.deposit.value),
            isActive: formElements.isActive.value === 'true',
            includedItems: formElements.includedItems.value,
            notIncludedItems: formElements.notIncludedItems.value,
            usageGuide: formElements.usageGuide.value,
            cancellationPolicy: formElements.cancellationPolicy.value,
            initialQuantity: isEditMode ? 0 : (parseInt(formElements.initialQuantity.value, 10) || 0), // 초기 재고 수량
            existingImageUrls: existingImageUrls
        };

        // '수정'(isEditMode:true)시에는 초기 재고를 변경하지 않으므로 DTO에서 제외
        if (isEditMode) {
            delete dto.initialQuantity;
        }

        formData.append('formDto', new Blob([JSON.stringify(dto)], { type: "application/json" }));

        // 3. 새로 추가된 파일들만 골라서 formData에 추가
        const newFiles = imageList.filter(item => !item.isExisting);
        newFiles.forEach(file => {
            formData.append('imageFiles', file);
        });

        // 4. fetch로 FormData 전송
        try {
            const response = await fetch( url, {    // url : 등록('/api/admin/products') / 수정('/api/admin/products/{id}')
                method: method,                     // POST :  등록, PUT : 수정
                headers: {
                    // 'Content-Type': 'multipart/form-data'는 브라우저가 자동으로 설정하므로 생략
                    // CSRF 토큰은 추가해야 함
                    [document.querySelector('meta[name="_csrf_header"]').getAttribute('content')]: document.querySelector('meta[name="_csrf"]').getAttribute('content')
                },
                body: formData,
            });

            const result = await response.json();
            if (!response.ok) throw new Error(result.message || '저장에 실패했습니다.');

            alert(result.message);
            window.location.href = '/admin/products';

        } catch (error) {
            console.error('Error:', error);
            alert(`오류가 발생했습니다: ${error.message}`);
        }
    });
});