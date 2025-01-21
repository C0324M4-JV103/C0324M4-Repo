// JS delete

window.onload = function () {
    var toastElList = document.querySelectorAll('.toast');
    var toastList = [...toastElList].map(toastEl => new bootstrap.Toast(toastEl));
    toastList.forEach(toast => toast.show());
};
const deleteButtons = document.querySelectorAll('[data-bs-toggle="modal"]');
deleteButtons.forEach(button => {
    button.addEventListener('click', function () {
        const studentName = button.getAttribute('data-name');
        const studentId = button.getAttribute('data-id');
        document.getElementById('studentName').textContent = studentName;
        document.getElementById('deleteForm').action = '/admin/delete-student/' + studentId;
    });
});


// JS Details
