
document.addEventListener('DOMContentLoaded', () => {

    // --- 업로드 설정값 ---
    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 파일당 10MB
    const MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 전체 50MB

    // --- 요소 가져오기 ---
    const form = document.getElementById('product-form');
    const imageFilesInput = document.getElementById('imageFiles');
    const previewContainer = document.getElementById('image-preview-container');
    const uploadStatus = document.getElementById('upload-status');

    // 업로드할 파일들을 관리하는 배열
    let filesToUpload = [];
    let representativeImageIndex = 0; // 대표 이미지 인덱스 (기본값 0)

    if (!form || !imageFilesInput || !previewContainer) return;

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

            const currentTotalSize = filesToUpload.reduce((total, f) => total + f.size, 0);
            if (currentTotalSize + file.size > MAX_TOTAL_SIZE) {
                alert('전체 업로드 용량을 초과했습니다. (최대 50MB)');
                break;
            }
            filesToUpload.push(file);
        }

        // 첫 번째 이미지를 기본 대표 이미지로 설정
        if (filesToUpload.length > 0 && representativeImageIndex >= filesToUpload.length) {
            representativeImageIndex = 0;
        }

        renderPreviewsAndStatus();
        // input의 값을 비워줘서 같은 파일을 다시 선택할 수 있게 함
        imageFilesInput.value = "";
    });

    // 미리보기에서 이미지 제거
    previewContainer.addEventListener('click', (e) => {
        const target = e.target;

        // '제거' 버튼 클릭 시
        if (target.classList.contains('remove-btn')) {
            const indexToRemove = parseInt(target.dataset.index, 10);
            filesToUpload.splice(indexToRemove, 1); // 배열에서 파일 제거

            if (representativeImageIndex === indexToRemove) {
                representativeImageIndex = 0; // 대표 이미지가 삭제되면 0번을 기본값으로
            } else if (representativeImageIndex > indexToRemove) {
              representativeImageIndex--;
            }
            renderPreviewsAndStatus(); // 미리보기 다시 렌더링
        }

        // '대표 설정' 버튼 클릭 시
        if (target.classList.contains('star-btn')) {
            representativeImageIndex = parseInt(target.dataset.index, 10);
            renderPreviewsAndStatus(); // active 클래스를 다시 렌더링하기 위해 호출
        }
    });

    // 미리보기와 업로드 상태를 업데이트하는 통합 함수
    function renderPreviewsAndStatus() {
        previewContainer.innerHTML = '';

        filesToUpload.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const item = document.createElement('div');
                item.className = 'image-preview-item';
                item.innerHTML = `
                    <img src="${e.target.result}" alt="${file.name}">
                    <button type="button" class="remove-btn" data-index="${index}">&times;</button>
                    <button type="button" class="star-btn ${index === representativeImageIndex ? 'active' : ''}" data-index="${index}">★</button>
                `;
                previewContainer.appendChild(item);
            };
            reader.readAsDataURL(file);
        });

        updateUploadStatus();
    }

    // 업로드 상태 텍스트를 업데이트하는 함수
    function updateUploadStatus() {
        const totalSize = filesToUpload.reduce((total, f) => total + f.size, 0);
        const totalSizeMB = (totalSize / 1024 / 1024).toFixed(2);
        uploadStatus.textContent = `선택된 파일: ${filesToUpload.length}개 (${totalSizeMB}MB)`;
    }

    // 폼 제출 이벤트 처리
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 1. FormData 객체 생성
        const formData = new FormData();

        // 2. DTO에 해당하는 폼 데이터(텍스트 등)를 JSON으로 만들어 'formDto' 파트에 추가
        const formElements = form.elements;
        const dto = {
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
            initialQuantity: parseInt(formElements.initialQuantity.value, 10) || 0,
            representativeImageName: filesToUpload.length > 0 ? filesToUpload[representativeImageIndex].name : null
        };
        formData.append('formDto', new Blob([JSON.stringify(dto)], { type: "application/json" }));

        // 3. 이미지 파일들을 'imageFiles' 파트에 추가(순서 보장)
        filesToUpload.forEach(file => {
            formData.append('imageFiles', file);
        });

        // 4. fetch로 FormData 전송
        try {
            const response = await fetch('/api/admin/products', {
                method: 'POST',
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