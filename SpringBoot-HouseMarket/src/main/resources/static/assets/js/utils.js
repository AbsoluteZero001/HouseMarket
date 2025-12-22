// 验证码生成函数
function generateCaptcha() {
    const canvas = document.getElementById('captchaCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    // 清空画布
    ctx.fillStyle = '#f5f5f5';
    ctx.fillRect(0, 0, width, height);

    // 生成随机验证码
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
    let captcha = '';
    for (let i = 0; i < 4; i++) {
        captcha += chars.charAt(Math.floor(Math.random() * chars.length));
    }

    // 绘制验证码
    ctx.font = '30px Arial';
    ctx.fillStyle = '#333';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    // 添加干扰线
    for (let i = 0; i < 5; i++) {
        ctx.strokeStyle = `rgb(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255})`;
        ctx.beginPath();
        ctx.moveTo(Math.random() * width, Math.random() * height);
        ctx.lineTo(Math.random() * width, Math.random() * height);
        ctx.stroke();
    }

    // 添加干扰点
    for (let i = 0; i < 30; i++) {
        ctx.fillStyle = `rgb(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255})`;
        ctx.beginPath();
        ctx.arc(Math.random() * width, Math.random() * height, 1, 0, Math.PI * 2);
        ctx.fill();
    }

    // 绘制验证码文本
    for (let i = 0; i < captcha.length; i++) {
        const x = (width / 4) * (i + 0.5);
        const y = height / 2 + (Math.random() - 0.5) * 10;
        const rotation = (Math.random() - 0.5) * 0.5;

        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(rotation);
        ctx.fillText(captcha.charAt(i), 0, 0);
        ctx.restore();
    }

    // 保存验证码到sessionStorage
    sessionStorage.setItem('captcha', captcha.toLowerCase());
}

// 表单验证函数
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;

    const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.value.trim()) {
            isValid = false;
            input.style.borderColor = '#dc3545';
            input.addEventListener('input', () => {
                input.style.borderColor = '#ddd';
            });
        }
    });

    return isValid;
}

// 显示消息函数
function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `alert alert-${type}`;
    messageDiv.textContent = message;

    // 添加到页面顶部
    document.body.insertBefore(messageDiv, document.body.firstChild);

    // 3秒后移除
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// 图片预览功能
function initImagePreview(inputId, previewId) {
    const input = document.getElementById(inputId);
    const preview = document.getElementById(previewId);

    if (input && preview) {
        input.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    preview.src = event.target.result;
                    preview.style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        });
    }
}

// 模态框控制
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
    }
}

function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// 登出函数
function logout() {
    // 清除用户数据
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    showMessage('已成功登出', 'success');

    // 跳转到登录页
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 1500);
}

// 显示指定section
function showSection(sectionId) {
    // 隐藏所有section
    document.querySelectorAll('.section').forEach(section => {
        section.style.display = 'none';
    });

    // 显示指定section
    document.getElementById(sectionId).style.display = 'block';

    // 更新导航激活状态
    document.querySelectorAll('.nav-links a').forEach(link => {
        link.classList.remove('active');
    });

    // 如果是导航链接，添加激活状态
    const activeLink = document.querySelector(`.nav-links a[onclick="showSection('${sectionId}')"]`);
    if (activeLink) {
        activeLink.classList.add('active');
    }
}

// 初始化模态框
function initModals() {
    // 为所有模态框添加关闭事件
    document.querySelectorAll('.modal .close').forEach(closeBtn => {
        closeBtn.addEventListener('click', () => {
            const modal = closeBtn.closest('.modal');
            if (modal) {
                modal.style.display = 'none';
            }
        });
    });

    // 点击模态框外部关闭
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
        }
    });
}