document.addEventListener('DOMContentLoaded', function() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/socket/notification', function (response) {
            const notification = JSON.parse(response.body);
            console.log(notification);

        });
    });

    function showNotification(notification){
        let notificationElement = `
        <div class="notif-list">
            <div class="notif-img">
                <img src="https://cellphones.com.vn/sforum/wp-content/uploads/2023/10/avatar-trang-4.jpg" alt="user">
            </div>
            <div class="notif-detail">
                <p><b>${notification.senderName}</b>${notification.content}</p>
                    <p><small>${notification.timeDifferent}</small></p>
            </div>
        </div>
        `;
    }
});