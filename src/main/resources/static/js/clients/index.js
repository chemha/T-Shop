function clientDeletion(id) {
    $.ajax({
            url: "/clients/" + id,
            type: "DELETE",
            success: function(response) {
            alert("Deletion successful!");
            window.location.href = "/clients/index";
        },
            error: function(e) {
            alert("Deletion failed!");
            console.log("ERROR: ", e);
        }
    });
}

function confirmDeletion(id) {
    swal({
        title: "¿Are you sure you want to delete this record?",
        text: "This can't be undone",
        icon: "warning",
        buttons: ["Cancel", "Delete"],
        dangerMode: true,
    }).then((confirmed) => {
        if (confirmed) clientDeletion(id);
    });
}

function openEditModal(elem) {
    const editModal = new bootstrap.Modal("#editModal");

    $("#editModalLabel").html("Changing password of: " + elem.dataset.name + " " + elem.dataset.surname);
    $("#editFormId").val(elem.dataset.id);
    $("#editFormName").val(elem.dataset.name);
    $("#editFormSurname").val(elem.dataset.surname);
    $("#editFormBirthdate").val(elem.dataset.birthdate);
    $("#editFormEmail").val(elem.dataset.email);
    $("#editFormAdmin").val(elem.dataset.admin);

    editModal.show();
}

function create() {
    const id = $("#createFormId").val();
    const name = $("#createFormName").val();
    const surname = $("#createFormSurname").val();
    const birthdate = $("#createFormBirthdate").val();
    const email = $("#createFormEmail").val();
    const password = $("#createFormPassword").val();
    const admin = $("#createFormAdmin").val();

    const formData = {
        "id": id,
        "name": name,
        "surname": surname,
        "birthdate": birthdate,
        "email": email,
        "password": password,
        "admin": admin
    }

    $.ajax({
      url: "/clients",
      type: "POST",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Create successful!");
        window.location.href = "/clients/index";
      },
      error: function(e) {
        alert("Create failed!");
        console.log("ERROR: ", e);
      }
    });
}

function edit() {
    const id = $("#editFormId").val();
    const name = $("#editFormName").val();
    const surname = $("#editFormSurname").val();
    const birthdate = $("#editFormBirthdate").val();
    const email = $("#editFormEmail").val();
    const password = $("#editFormPassword").val();
    const admin = $("#editFormAdmin").val();

    const formData = {
        "id": id,
        "name": name,
        "surname": surname,
        "birthdate": birthdate,
        "email": email,
        "password": password,
        "admin": admin
    }

    $.ajax({
      url: "/clients/" + id,
      type: "PUT",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Edit successful!");
        window.location.href = "/clients/index";
      },
      error: function(e) {
        alert("Edit failed!");
        console.log("ERROR: ", e);
      }
    });
}

$(window).on("load", () => {
    const table = $("#client-table").DataTable({
        responsive: true
    });

    $("#editForm").submit(function(event) {
        event.preventDefault();
        edit();
    });

    $("#createForm").submit(function(event) {
         event.preventDefault();
         create();
    });
});