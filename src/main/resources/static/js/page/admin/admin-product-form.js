
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('product-form');
    const imageFilesInput = document.getElementById('imageFiles');
    const previewContainer = document.getElementById('image-preview-container');

    // 업로드할 파일들을 관리하는 배열
    let filesToUpload = [];

    if (!form || !imageFilesInput || !previewContainer) return;

    // '파일 선택' 라벨을 클릭하면 숨겨진 input[type=file]이 클릭되도록 함
    document.querySelector('label[for="imageFiles"]').addEventListener('click', (e) => {
        e.preventDefault();
        imageFilesInput.click();
    });

    // 파일 입력이 변경되었을 때의 이벤트 처리
    imageFilesInput.addEventListener('change', () => {
        const selectedFiles = Array.from(imageFilesInput.files);
        filesToUpload.push(...selectedFiles);
        renderImagePreviews();
        // input의 값을 비워줘서 같은 파일을 다시 선택할 수 있게 함
        imageFilesInput.value = "";
    });

    // 이미지 미리보기 렌더링 함수
    function renderImagePreviews() {
        previewContainer.innerHTML = ''; // 미리보기 영역 초기화
        filesToUpload.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const item = document.createElement('div');
                item.className = 'image-preview-item';
                item.innerHTML = `
                    <img src="${e.target.result}" alt="${file.name}">
                    <button type="button" class="remove-btn" data-index="${index}">&times;</button>
                `;
                previewContainer.appendChild(item);
            };
            reader.readAsDataURL(file);
        });
    }

    // 미리보기에서 이미지 제거
    previewContainer.addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-btn')) {
            const indexToRemove = parseInt(e.target.dataset.index, 10);
            filesToUpload.splice(indexToRemove, 1); // 배열에서 파일 제거
            renderImagePreviews(); // 미리보기 다시 렌더링
        }
    });

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
            initialQuantity: parseInt(formElements.initialQuantity.value, 10) || 0
        };
        formData.append('formDto', new Blob([JSON.stringify(dto)], { type: "application/json" }));

        // 3. 이미지 파일들을 'imageFiles' 파트에 추가
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