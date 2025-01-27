document.addEventListener('DOMContentLoaded', function () {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/socket/notification', function (response) {
            const notification = JSON.parse(response.body);
            const unreadCount = notification.unreadCount || 0;
            console.log(notification);//log notification to console for debugging

            updateNotificationCount(unreadCount);
            showNotification(notification)
            showToast(notification);
        });
    });


    function showNotification(notification) {
        const notifCount = document.getElementById('notif-count');
        const url = notification.notification?.url || null;

        notifCount.textContent = notification.unreadCount > 3 ? '3+' : notification.unreadCount || 0;

        const notifContent = document.querySelector('#notif-content');
        const newNotification = url ? `
        <div class="notif-list">
            <a href="${url}" class="notif-link" style="text-decoration: none; color: inherit; display: flex; align-items: center;">
                <div class="notif-img">
                    <img src="${notification.notification.senderAvatar}" alt="user">
                </div>
                <div class="notif-detail">
                    <p><b>${notification.notification.senderName}</b> ${notification.notification.content}</p>
                    <p><small>${notification.notification.timeDifference}</small></p>
                </div>
            </a>
        </div>` :
            `<div class="notif-list">
            <a href="" class="notif-link" style="text-decoration: none; color: inherit; display: flex; align-items: center;">
                <div class="notif-img">
                    <img src="${notification.notification.senderAvatar}" alt="user">
                </div>
                <div class="notif-detail">
                    <p><b>${notification.notification.senderName}</b> ${notification.notification.content}</p>
                    <p><small>${notification.notification.timeDifference}</small></p>
                </div>
            </a>
        </div>`
        ;
        notifContent.insertAdjacentHTML('afterbegin', newNotification);
    }

    // click chuông => đã đọc
    const bellIcon = document.getElementById('navbarDropdown');
    bellIcon.addEventListener('click', function () {
        const notifCount = document.getElementById('notif-count');
        if (notifCount.textContent > 0) {
            notifCount.style.display = 'none';
        }
        fetch('/mark-read', {
            method: 'GET',
            credentials: 'same-origin',
        })
            .then(data => {
                updateNotificationCount(data.unreadCount); // update lại số lượng
            })
    });

    function updateNotificationCount(count) {
        const notifCount = document.getElementById('notif-count');
        if (count > 0) {
            notifCount.textContent = count > 3 ? '3+' : count;
            notifCount.style.display = 'inline';
        } else {
            notifCount.textContent = '';
            notifCount.style.display = 'none';
        }
    }


    function showToast() {
        const toastContainer = document.getElementById('toast-container');
        const toastHTML = `
        <div class="toast toast-custom" role="alert" aria-live="assertive" aria-atomic="true" id="successToast">
            <div class="toast-header bg-success text-white">
                <i class="fa fa-bell me-2 "></i>
                <strong class="me-auto">Thông báo</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                <strong>Bạn nhận được 1 thông báo mới!</strong>
            </div>
        </div>
    `;
        toastContainer.insertAdjacentHTML('beforeend', toastHTML);
        const toastElement = toastContainer.lastElementChild;

        const toast = new bootstrap.Toast(toastElement);
        toast.show();

        setTimeout(() => {
            if (toastElement) {
                toastElement.classList.add('fade-out');
                toastElement.addEventListener('transitionend', () => {
                    toastElement.remove();
                });
            }
        }, 4000);
    }

});