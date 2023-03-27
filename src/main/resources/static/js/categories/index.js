function productDeletion(id) {
    $.ajax({
            url: "/categories/" + id,
            type: "DELETE",
            success: function(response) {
            alert("Deletion successful!");
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
        if (confirmed) productDeletion(id);
    });
}

function openEditModal(elem) {
    const editModal = new bootstrap.Modal("#editModal");
    const editModalLabel = $("#editModalLabel");
    const editFormId = $("#editFormId");
    const editFormTitle = $("#editFormTitle");

    const id = elem.dataset.id;
    const title = elem.dataset.title;

    editModalLabel.html("Editing: " + title);
    editFormId.val(id);
    editFormTitle.val(title);

    editModal.show();
}

function create() {
    const title = $("#createFormTitle").val();

    const formData = {
        "title": title
    }

    $.ajax({
      url: "/categories",
      type: "POST",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Create successful!");
        window.location.href = "/categories/index";
      },
      error: function(e) {
        alert("Create failed!");
        console.log("ERROR: ", e);
      }
    });
}

function edit() {
    const id = $("#editFormId").val();
    const title = $("#editFormTitle").val();

    const formData = {
        "title": title
    }

    $.ajax({
      url: "/categories/" + id,
      type: "PUT",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Edit successful!");
        window.location.href = "/categories/index";
      },
      error: function(e) {
        alert("Edit failed!");
        console.log("ERROR: ", e);
      }
    });
}

$(window).on("load", () => {
    const table = $("#category-table").DataTable({
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