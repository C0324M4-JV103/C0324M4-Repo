function loadStudentDetails(studentId) {
    fetch(`/student/student-details/${studentId}`)
        .then(response => response.json())
        .then(student => {
            if (!student.id) throw new Error("Dữ liệu không hợp lệ");
            document.getElementById("studentName").value = student.name;
            document.getElementById("studentEmail").value = student.email;
            document.getElementById("studentClazz").value = student.clazz.name;
            document.getElementById("studentPhoneNumber").value = student.phoneNumber;
            document.getElementById("studentDob").value = new Date(student.dob).toLocaleDateString();
            document.getElementById("studentAddress").value = student.address;
            document.getElementById("studentGender").value = student.gender === "MALE" ? "Nam" : "Nữ";
            document.getElementById("studentTeam").value = student.status;
            document.getElementById("studentAvatar").src = student.avatar ? student.avatar.url : "https://firebasestorage.googleapis.com/v0/b/cv-pdf-upload.appspot.com/o/casestudym5%2Favatar%2Fdefault-avatar.png?alt=media&token=a7b21608-1b68-45e0-9d6e-a5fcbcaac12e";
            const studentDetailModal = new bootstrap.Modal(document.getElementById('studentDetailModal'));
            studentDetailModal.show();
        })
        .catch(error => {
            console.error("Lỗi tải thông tin sinh viên:", error.message);
        });
}