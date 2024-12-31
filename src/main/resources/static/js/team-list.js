
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
            }
        }
    }

    function deleteTeam(id){
        stompClient.send("/app/delete-team", {}, JSON.stringify({teamId: id}));
    }
});
