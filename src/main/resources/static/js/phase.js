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

document.addEventListener('DOMContentLoaded', function () {
    var myTab = document.getElementById('myTabContent');
    var tabPanes = myTab.querySelectorAll('.tab-pane');

    // Initially hide all tab panes except the active one
    tabPanes.forEach(function (tabPane) {
        if (!tabPane.classList.contains('show') || !tabPane.classList.contains('active')) {
            tabPane.classList.add('d-none');
        }
    });

    var tabLinks = document.querySelectorAll('#myTab a[data-bs-toggle="tab"]');
    tabLinks.forEach(function (tabLink) {
        tabLink.addEventListener('shown.bs.tab', function (event) {
            tabPanes.forEach(function (tabPane) {
                tabPane.classList.add('d-none');
            });
            var activeTabPane = document.querySelector(event.target.getAttribute('href'));
            activeTabPane.classList.remove('d-none');
        });
    });
});