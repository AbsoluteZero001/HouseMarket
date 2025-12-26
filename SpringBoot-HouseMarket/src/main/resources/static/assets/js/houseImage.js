async function addHouse() {
    try {
        const form = document.getElementById('addHouseForm');
        const formData = new FormData(form);
        const token = localStorage.getItem('token'); // 获取用户token

        // 获取图片文件
        const imageFile = document.getElementById('houseImage').files[0];
        let imageUrl = '';

        // 如果有图片文件，上传到服务器
        if (imageFile) {
            const uploadResponse = await uploadImage(imageFile, token);
            if (!uploadResponse.ok) {
                throw new Error('图片上传失败');
            }
            const uploadResult = await uploadResponse.json();
            imageUrl = uploadResult.data.imageUrl; // 假设返回的图片 URL
        }

        // 发送房源信息到后端
        const houseData = {
            title: formData.get('title'),
            type: formData.get('type'),
            area: parseFloat(formData.get('area')),
            price: parseFloat(formData.get('price')),
            address: formData.get('address'),
            description: formData.get('description'),
            image: imageUrl  // 设置上传的图片 URL
        };

        const response = await fetch('/api/houses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // 将 token 加入请求头
            },
            body: JSON.stringify(houseData)
        });

        if (!response.ok) {
            throw new Error('发布房源失败');
        }

        alert('发布房源成功');
        form.reset(); // 重置表单
    } catch (error) {
        console.error('发布房源失败:', error);
        alert('发布房源失败');
    }
}

// 上传图片到服务器
async function uploadImage(file, token) {
    const formData = new FormData();
    formData.append('image', file);

    return fetch('/api/upload-image', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}` // 认证token
        },
        body: formData
    });
}
