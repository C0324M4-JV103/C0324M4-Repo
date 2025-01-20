document.addEventListener('DOMContentLoaded', function () {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/socket/notification', function (response) {
            const notification = JSON.parse(response.body);
            console.log(notification);//log notification to console for debugging
            showNotification(notification)

            showToast(notification);

        });
    });


    function showNotification(notification) {
        const notifContent = document.querySelector('#notif-content');
        const newNotification = `
        <div class="notif-list">
            <div class="notif-img">
                <img src="${notification.senderAvatar}" alt="user">
            </div>
            <div class="notif-detail">
                <p><b>${notification.senderName}</b> ${notification.content}</p>
                <p><small>${notification.timeDifference}</small></p>
            </div>
        </div>
    `;
        notifContent.insertAdjacentHTML('afterbegin', newNotification);
    }

    function showToast() {
        const toastContainer = document.getElementById('toast-container');
        const toastHTML = `
             <div class="toast toast-custom rounded w-auto show align-items-center" role="alert" aria-live="assertive" aria-atomic="true">
                 <div class="d-flex align-items-center px-2 py-3">
                     <i class="fas fa-bell me-2 fs-4"></i>
                     <div class="text-white fw-bold">
                            Bạn có 1 thông báo mới!
                      </div>
                      <button type="button" class="btn-close ms-3" data-bs-dismiss="toast" aria-label="Close"></button>
                 </div>
             </div>
    `;
        toastContainer.insertAdjacentHTML('beforeend', toastHTML);

        setTimeout(() => {
            const toastElement = toastContainer.querySelector('.toast');
            if (toastElement) toastElement.remove();
        }, 5000); // tắt toast sau 5s
    }
});