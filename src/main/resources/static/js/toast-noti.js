let toastElement = document.querySelector('.toast');
if (toastElement) {
    let toast = new bootstrap.Toast(toastElement);
    toast.show();
}