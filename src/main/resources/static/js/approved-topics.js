document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

    const handleApproveAction = (topicId) => {
        $.ajax({
            url: `/teacher/topics/${topicId}/approve`,
            type: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            success: function (response) {
                location.reload();
            },
            error: function () {
                location.reload();
            }
        });
    };

    const handleRejectAction = (topicId, reason) => {
        fetch(`/teacher/topics/${topicId}/reject`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ reason })
        })
            .then(response => {
                if (response.ok) {
                    location.reload();
                } else {
                    location.reload();
                }
            })
            .catch(error => {
                location.reload();
            });
    };

    // Xử lý modal approve
    const approveModal = document.getElementById('approveModal');
    const approveButton = document.getElementById('confirmApprove');
    const approveTopicName = document.getElementById('approveTopicName');

    approveModal.addEventListener('show.bs.modal', (event) => {
        const button = event.relatedTarget;
        const topicId = button.getAttribute('data-id');
        const topicName = button.getAttribute('data-name');

        approveButton.setAttribute('data-id', topicId);
        approveTopicName.textContent = topicName;
    });

    approveButton.addEventListener('click', () => {
        const topicId = approveButton.getAttribute('data-id');
        handleApproveAction(topicId);
    });

    // Xử lý modal reject
    const rejectModal = document.getElementById('rejectModal');
    const rejectButton = document.getElementById('confirmReject');
    const rejectTopicName = document.getElementById('rejectTopicName');

    rejectModal.addEventListener('show.bs.modal', (event) => {
        const button = event.relatedTarget;
        const topicId = button.getAttribute('data-id');
        const topicName = button.getAttribute('data-name');

        rejectButton.setAttribute('data-id', topicId);
        rejectTopicName.textContent = topicName;
    });

    rejectButton.addEventListener('click', () => {
        const reasonTextarea = document.getElementById('reasonTextarea');
        const reasonError = document.getElementById('reasonError');
        const reason = reasonTextarea.value.trim();

        if (!reason) {
            reasonError.style.display = 'block';
            reasonTextarea.classList.add('is-invalid');
        } else {
            reasonError.style.display = 'none';
            reasonTextarea.classList.remove('is-invalid');

            const topicId = rejectButton.getAttribute('data-id');
            handleRejectAction(topicId, reason);
        }
    });
});
