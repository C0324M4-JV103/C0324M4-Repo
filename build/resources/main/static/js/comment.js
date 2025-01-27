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
            let topicId = this.getAttribute('data-topic-id');
            let commentId = this.getAttribute('data-comment-id');
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
        <div class="d-flex gap-3 mb-3 question-container"
            id="comment-${comment.id}">
                <img src="${comment.senderAvatar}"
                    width="50px" height="50px"
                        alt="Profile" class="profile-img">
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <h6 class="mb-0 fw-bold" >${comment.senderName}</h6>
                        </div>
                        <span class="timestamp">${comment.timeDifference}</span>
                    </div>
                    <p class="mb-2" >${comment.content}</p>
                    <button class="btn btn-sm-reply" id="btn-show-reply-${comment.id}" onclick="toggleQuestion(this)">
                        <i class="fas fa-reply"></i> Trả lời
                    </button>
                    <!-- Ô trả lời bên trong khung -->
                    <div class="reply-container" style="display: none; margin-top: 10px;">
                        <div class="d-flex align-items-center gap-2">
                            <input id="reply-box-${comment.id}" class="form-control"
                                    placeholder="Nhập câu trả lời của bạn...">
                            <button class="btn btn-outline-primary btn-reply"
                                data-topic-id=${comment.topicId} data-comment-id=${comment.id}">
                                <i class="fas fa-paper-plane"></i>
                            </button>
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
        <div class="d-flex gap-3 mb-3 question-container" 
            id="comment-${comment.id}">
                <img src="${comment.senderAvatar}"
                    width="50px" height="50px"
                        alt="Profile" class="profile-img">
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <h6 class="mb-0 fw-bold">${comment.senderName}</h6>
                        </div>
                        <span class="timestamp">${comment.timeDifference}</span>
                    </div>
                    <p class="mb-2">${comment.content}</p>
            </div>
        </div>
    `;
        commentContent.insertAdjacentHTML('afterbegin', newComment);
    }


    const showReply = (reply) => {
        const parentComment = document.querySelector(`#comment-${reply.id}`);
        let insertPos = parentComment.querySelector('.flex-grow-1')
        let replyHTML = `
            <div class="divider"></div>
        <!-- Giảng viên trả lời -->
             <div class="teacher-reply" >
                  <div class="d-flex gap-3 mb-3">
                       <img src="${reply.senderAvatar}"
                            width="50px" height="50px"
                            alt="Profile" class="profile-img">
                       <div class="flex-grow-1">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                 <div>
                                      <h6 class="mb-0 fw-bold" >${reply.senderName}</h6>
                                 </div>
                                 <span class="timestamp" th:text="${reply.timeDifference}"></span>
                            </div>
                            <p class="mb-2">${reply.content}</p>
                       </div>
                  </div>
             </div>
        `
        insertPos.insertAdjacentHTML('beforeend', replyHTML);
        const replyButton = parentComment.querySelector(`#btn-show-reply-${reply.id}`);
        if (replyButton) {
            replyButton.remove();
        }
        parentComment.classList.remove('with-border');
    }
});