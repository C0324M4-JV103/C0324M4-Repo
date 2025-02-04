

document.addEventListener('DOMContentLoaded', function() {

    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    let deleteTeamId = null;

    window.handleDeleteClick = function(element) {
        const teamId = element.getAttribute('data-team-id');
        const teamName = element.getAttribute('data-team-name');
        showDeleteModal(teamId, teamName);
    };

    function showDeleteModal(id, name){
        deleteTeamId = id;
        document.getElementById("teamNameToDelete").textContent = name;
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
        deleteModal.show();

        document.getElementById("confirmDeleteButton").onclick = function(){
            if (deleteTeamId !== null) {
                deleteTeam(deleteTeamId);
                deleteTeamId = null;
                const deleteModal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
                deleteModal.hide();
                // location.reload();
            }
        }
    }

    function deleteTeam(id){
        stompClient.send("/app/delete-team", {}, JSON.stringify({teamId: id}));
    }

    document.getElementById('saveDeadlineButton').addEventListener('click', function() {
        const teamId = document.getElementById('deadlineModal').getAttribute('data-team-id');
        const newDeadline = document.getElementById('newDeadline').value;
        const currentDeadline = document.getElementById('currentDeadline').value;

        // Convert to Date objects
        const newDeadlineDate = new Date(newDeadline);
        const currentDeadlineDate = new Date(currentDeadline.split('/').reverse().join('-'));

        if (newDeadlineDate <= currentDeadlineDate) {
            document.getElementById('deadlineError').textContent = "Hạn nộp mới phải sau hạn nộp hiện tại";
            return;
        }

        // Check if newDeadline is more than 1 month after currentDeadline
        const diffTime = Math.abs(newDeadlineDate - currentDeadlineDate);
        if (diffTime > 2592000000) {
            document.getElementById('deadlineError').textContent = "Hạn nộp chỉ thể cách tối đa 1 tháng so với ban đầu";
            return;
        }

        setNewDeadline(teamId, newDeadline);
        const deadlineModal = bootstrap.Modal.getInstance(document.getElementById('deadlineModal'));
        deadlineModal.hide();
    });

    function setNewDeadline(teamId, newDeadline) {
        stompClient.send("/app/set-deadline", {}, JSON.stringify({teamId: teamId, deadline: newDeadline}));
    }

});

function showDeadlineModal(element) {
    const teamId = element.getAttribute('data-team-id');
    const currentDeadline = element.getAttribute('data-current-deadline');
    console.log(`Team ID: ${teamId}, Current Deadline: ${currentDeadline}`);

    document.getElementById('currentDeadline').value = currentDeadline
    document.getElementById('deadlineModal').setAttribute('data-team-id', teamId);

    const deadlineModal = new bootstrap.Modal(document.getElementById('deadlineModal'));
    deadlineModal.show();
}


