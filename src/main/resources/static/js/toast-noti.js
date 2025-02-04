let toastElement = document.querySelector('.toast');
if (toastElement) {
    let toast = new bootstrap.Toast(toastElement);
    toast.show();
}

var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    stompClient.subscribe('/socket/notification', function (message) {
        var messageContent = message.body;
        showToast(messageContent);
    });
});

function showToast(message) {
    var toastContainer = document.querySelector('.toast-container');
    var toast = document.createElement('div');
    toast.classList.add('toast', 'show');

    toast.innerHTML = `
        <div class="toast-header bg-success text-white">
            <strong class="me-auto">Thông báo</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;

    toastContainer.appendChild(toast);
    setTimeout(function () {
        toast.classList.remove('show');
        toastContainer.removeChild(toast);
    }, 3000);
}
