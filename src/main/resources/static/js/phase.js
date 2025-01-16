

document.getElementById('question-btn').addEventListener('click', function () {
    let formContainer = document.querySelector('.top2');
    let button = document.getElementById('question-btn');

    formContainer.classList.toggle('show-form');

    if (formContainer.classList.contains('show-form')) {
        button.textContent = 'Ẩn thắc mắc';
    } else {
        button.textContent = 'Đăng câu hỏi thắc mắc';
    }
});

document.querySelectorAll('.btn-send').forEach(button => {
    button.addEventListener('click', function () {
        const editorContainer = button.closest('.editor-container');
        if (editorContainer) {
            editorContainer.classList.toggle('show-form');
        }
    });
});

document.querySelectorAll('.reply-btn').forEach(button => {
    button.addEventListener('click', function () {
        const replyForm = button.closest('.top1').querySelector('.reply-form');
        if (replyForm) {
            replyForm.style.display = replyForm.style.display === 'none' ? 'block' : 'none';
        } else {
            console.log('Reply form not found');
        }
    });
});

