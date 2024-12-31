document.addEventListener('DOMContentLoaded', function() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/socket/notification', function (response) {
            const notification = JSON.parse(response.body);
            console.log(notification);
            showNotification(notification);
        });
    });

    const notifDropdown = document.querySelector('.notif-dd');
    const userEmail = notifDropdown.getAttribute('user-email')
    fetch('/notifications', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email: userEmail })
    })
        .then(response => response.json())
        .then(notifications => {
            const notifContent = document.querySelector('#notif-content');
            notifContent.innerHTML = ''; // Clear existing notifications
            notifications.forEach(notification => {
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
                notifContent.insertAdjacentHTML('beforeend', newNotification);
            });
        });

    function showNotification(notification){
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
});