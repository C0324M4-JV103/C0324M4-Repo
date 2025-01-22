function toggleQuestion(button) {
    const replyContainer = button.parentElement.querySelector('.reply-container');
    const questionContainer = button.closest('.question-container');
    const textarea = replyContainer.querySelector('input');

    if (replyContainer.style.display === 'none') {
        replyContainer.style.display = 'block';
        textarea.focus();
        questionContainer.classList.add('with-border');
    } else {
        replyContainer.style.display = 'none';
        questionContainer.classList.remove('with-border');
    }
}