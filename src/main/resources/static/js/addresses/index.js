function addressDeletion(id) {
    $.ajax({
            url: "/client-addresses/" + id,
            type: "DELETE",
            success: function(response) {
            alert("Deletion successful!");
            window.location.href = "/client-addresses/index";
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
        if (confirmed) addressDeletion(id);
    });
}

function openEditModal(elem) {
    const editModal = new bootstrap.Modal("#editModal");

    console.log(elem.dataset.postalCode);
    $("#editFormId").val(elem.dataset.id);
    $("#editFormCountry").val(elem.dataset.country);
    $("#editFormPostalCode").val(elem.dataset.postal);
    $("#editFormStreet").val(elem.dataset.street);
    $("#editFormHome").val(elem.dataset.home);
    $("#editFormApartment").val(elem.dataset.apartment);

    editModal.show();
}

function create() {
    const userId = $("#createFormUserId").val();
    const country = $("#createFormCountry").val();
    const postal = $("#createFormPostalCode").val();
    const street = $("#createFormStreet").val();
    const home = $("#createFormHome").val();
    const apartment = $("#createFormApartment").val();

    const formData = {
        "client_id": userId,
        "country": country,
        "postal_code": postal,
        "street": street,
        "home": home,
        "apartment": apartment
    }

    console.log(formData);

    $.ajax({
      url: "/client-addresses",
      type: "POST",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Create successful!");
        window.location.href = "/client-addresses/index";
      },
      error: function(e) {
        alert("Create failed!");
        console.log("ERROR: ", e);
      }
    });
}

function edit() {
    const id = parseInt($("#editFormId").val());
    const userId = parseInt($("#editFormUserId").val());
    const country = $("#editFormCountry").val();
    const postal = $("#editFormPostalCode").val();
    const street = $("#editFormStreet").val();
    const home = $("#editFormHome").val();
    const apartment = $("#editFormApartment").val();

    const formData = {
        "client_id": userId,
        "country": country,
        "postal_code": postal,
        "street": street,
        "home": home,
        "apartment": apartment
    }

    console.log(formData);

    $.ajax({
      url: "/client-addresses/" + id,
      type: "PUT",
      contentType: "application/json",
      dataType: "json",
      data: JSON.stringify(formData),
      success: function(response) {
        alert("Edit successful!");
        window.location.href = "/client-addresses/index";
      },
      error: function(e) {
        alert("Edit failed!");
        console.log("ERROR: ", e);
      }
    });
}

$(window).on("load", () => {
    const table = $("#address-table").DataTable({
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