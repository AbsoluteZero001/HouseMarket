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
                reader.onerror = (event) => {
                    console.error('图片读取失败:', event.target.error);
                    alert('图片读取失败，请重试');
                };
                reader.readAsDataURL(file);
            } else {
                // 用户取消选择图片时，隐藏预览
                preview.style.display = 'none';
                preview.src = '';
            }
        });

        // 添加点击事件，确保每次点击都能触发change事件
        input.addEventListener('click', () => {
            // 重置input值，确保同一文件也能触发change事件
            input.value = '';
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

// 页面加载动画
function initPageAnimations() {
    // 为所有可动画的元素添加进入动画
    const animatedElements = document.querySelectorAll('.house-card, .card, .stat-card, .form-container');

    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                entry.target.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            }
        });
    }, observerOptions);

    animatedElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        observer.observe(el);
    });
}

// 启动页面动画
document.addEventListener('DOMContentLoaded', initPageAnimations);

// 为按钮添加点击波纹效果
function addRippleEffect(button) {
    const ripple = document.createElement('span');
    ripple.classList.add('ripple');

    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);

    ripple.style.width = ripple.style.height = `${size}px`;

    // 计算点击位置
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;

    ripple.style.left = `${x}px`;
    ripple.style.top = `${y}px`;

    button.appendChild(ripple);

    setTimeout(() => {
        ripple.remove();
    }, 600);
}

// 为所有按钮添加波纹效果
function initRippleEffects() {
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function (e) {
            addRippleEffect(this);
        });
    });
}

// 初始化波纹效果
document.addEventListener('DOMContentLoaded', initRippleEffects);

// 添加波纹效果的CSS
const rippleStyle = document.createElement('style');
rippleStyle.textContent = `
    .btn {
        position: relative;
        overflow: hidden;
    }
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.7);
        transform: scale(0);
        animation: ripple-animation 0.6s linear;
        pointer-events: none;
    }
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
`;
document.head.appendChild(rippleStyle);

// 进度条功能
let progressInterval = null;

function showProgressBar() {
    // 检查是否已存在进度条
    let progressBar = document.getElementById('progress-bar');

    if (!progressBar) {
        progressBar = document.createElement('div');
        progressBar.id = 'progress-bar';
        progressBar.className = 'progress-bar';
        document.body.insertBefore(progressBar, document.body.firstChild);
    }

    // 清除可能存在的旧interval
    if (progressInterval) {
        clearInterval(progressInterval);
        progressInterval = null;
    }

    // 重置进度条动画
    progressBar.style.width = '0%';
    progressBar.style.display = 'block';

    // 模拟进度
    let width = 0;
    progressInterval = setInterval(() => {
        if (width >= 100) {
            clearInterval(progressInterval);
            progressInterval = null;
            setTimeout(() => {
                progressBar.style.display = 'none';
            }, 300);
        } else {
            width += Math.random() * 15;
            if (width > 100) width = 100;
            progressBar.style.width = width + '%';
        }
    }, 100);
}

// 隐藏进度条
function hideProgressBar() {
    const progressBar = document.getElementById('progress-bar');
    if (progressBar) {
        progressBar.style.display = 'none';
    }
    
    // 清除interval
    if (progressInterval) {
        clearInterval(progressInterval);
        progressInterval = null;
    }
}

// 模拟数据加载
function simulateDataLoading() {
    showProgressBar();

    return new Promise(resolve => {
        setTimeout(() => {
            hideProgressBar();
            resolve();
        }, 1000);
    });
}
