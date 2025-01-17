document.addEventListener('DOMContentLoaded', function() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/socket/comment', function (response) {
            const output = JSON.parse(response.body);
            console.log(output);//log comment to console for debugging
            let isTeacher = document.getElementById('isTeacher').value;
            if(isTeacher === 'true') {
                showCommentForTeacher(output);
            }
            else {
                showCommentForStudent(output);
            }
        });

        stompClient.subscribe('/socket/reply', function (response) {
            const output = JSON.parse(response.body);
            console.log(output);//log reply to console for debugging
            showReply(output);
        });
    });


    document.querySelectorAll('.btn-send').forEach(button => {
        button.addEventListener('click', function () {
            let topicId = this.getAttribute('data-topic-id');
            let contentInput = document.querySelector('#comment-box');
            let content = contentInput.value;

            if (content.trim() !== '') {
                var comment = {
                    topicId: topicId,
                    content: content
                };

                stompClient.send("/app/add-comment", {}, JSON.stringify(comment));
                contentInput.value = '';
            }
        });
    });

    document.querySelectorAll('.btn-reply').forEach(button => {
        button.addEventListener('click', function (){
            let topicId = this.getAttribute('topic-id');
            let commentId = this.getAttribute('comment-id');
            let contentInput = document.querySelector(`#reply-box-${commentId}`);
            let content = contentInput.value;
            if(content.trim() !== '' ){
                var reply = {
                    topicId: topicId,
                    id: commentId,
                    reply: content
                };
                stompClient.send("/app/add-reply", {}, JSON.stringify(reply));
                contentInput.value = '';
                contentInput.closest('.reply-container').style.display = 'none';
            }
        })
    })

    const showCommentForTeacher = (comment) => {
        const commentContent = document.querySelector('#show-comment');
        const newComment = `
        <div class="top1" id="'comment-' + ${comment.id}">
        <div class="message-container">
            <div class="d-flex">
                <div class="me-3">
                    <img src="${comment.senderAvatar}" width="50px" height="50px" alt="Avatar" class="avatar">
                </div>
                <div class="flex-grow-1">
                    <div class="d-flex align-items-center mb-2">
                        <strong class="mb-0 me-2">${comment.senderName}</strong>
                    </div>
                    <p class="mb-2">${comment.content}</p>
                    <div class="action-buttons mb-2" >
                        <button class="btn btn-success btn-sm reply-btn">Trả lời</button><!-- role teacher -->
                    </div>
                    <div class="d-flex justify-content-between align-items-center comment-actions">
                         <span class="timestamp ms-auto" >${comment.timeDifference}</span>
                    </div>
                </div>
            </div>
        </div>
        </div>
    `;
        commentContent.insertAdjacentHTML('afterbegin', newComment);
    }

    const showCommentForStudent = (comment) => {
        const commentContent = document.querySelector('#show-comment');
        const newComment = `
        <div class="top1" id="'comment-' + ${comment.id}">
        <div class="message-container">
            <div class="d-flex">
                <div class="me-3">
                    <img src="${comment.senderAvatar}" width="50px" height="50px" alt="Avatar" class="avatar">
                </div>
                <div class="flex-grow-1">
                    <div class="d-flex align-items-center mb-2">
                        <strong class="mb-0 me-2">${comment.senderName}</strong>
                    </div>
                    <p class="mb-2">${comment.content}</p>
                    <div class="d-flex justify-content-between align-items-center comment-actions">
                         <span class="timestamp ms-auto" >${comment.timeDifference}</span>
                    </div>
                </div>
            </div>
        </div>
        </div>
    `;
        commentContent.insertAdjacentHTML('afterbegin', newComment);
    }


    const showReply = (reply) => {
        const parentComment = document.querySelector(`#comment-${reply.id}`);
        let replyHTML = `
        <div class="message-container-reply-btn">
            <div class="d-flex">
                <div class="me-3">
                    <img src="${reply.senderAvatar}"
                    width="50px" height="50px" alt="Avatar" class="avatar">
                </div>
                <div class="flex-grow-1">
                    <div class="mb-2">
                        <strong class="mb-0" >${reply.senderName}</strong>
                    </div>
                    <p class=" mb-2" >${reply.content}</p>
                    <div class="d-flex justify-content-between align-items-center comment-actions">
                    <span class="timestamp ms-auto">${reply.timeDifference}</span>
                    </div>
                </div>
            </div>
        </div>
        `
        parentComment.insertAdjacentHTML('beforeend', replyHTML);
    }
});